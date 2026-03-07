package com.example.practice.service.farm;

import com.example.practice.dto.farm.*;
import com.example.practice.entity.crops.Crops;
import com.example.practice.entity.farm.*;
import com.example.practice.entity.location.Location;
import com.example.practice.exception.FarmException;
import com.example.practice.repository.crops.CropsRepository;
import com.example.practice.repository.farm.FarmMemberRepository;
import com.example.practice.repository.farm.FarmRepository;
import com.example.practice.repository.location.LocationRepository;
import com.example.practice.repository.user.UserRepository;
import com.example.practice.service.aws.AwsS3Service;
import lombok.RequiredArgsConstructor;
import com.example.practice.entity.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor

public class FarmService {
    private final UserRepository userRepo;
    private final FarmRepository farmRepo;
    private final FarmMemberRepository farmMemberRepo;
    private final LocationService locationService; // 주입 필요
    private final LocationRepository locationRepo; // 주입 필요
    private final CropsRepository cropsRepo;  // SecurityContext에서 userId 가져옴 가정
    private final AwsS3Service awsS3Service;


    @Value("${file.default_img}")
    private String defaultImg;


    // #1 농장 생성 + #3 자동 멤버 등록
    public Farm createFarm(FarmRequest req, MultipartFile image, Long currentUserId) {
        // 1. 농장 정보 저장
        Farm farm = new Farm();

        //이미지 저장
        String imagePath;
        if (image != null && !image.isEmpty()) {
            imagePath = awsS3Service.upload(image, "farm");
        } else {
            imagePath = defaultImg;  // 기본이미지 미리 올려
        }
        farm.setImagePath(imagePath);

        System.out.println("name = " + req.getName());
        farm.setImagePath(imagePath);

        farm.setName(req.getName());
        farm.setArea(req.getArea());
        farm.setCreatedAt(OffsetDateTime.now());
        Farm savedFarm = farmRepo.save(farm);

        // 2. 구글 API를 통한 위치 정보 저장
        Location location = locationService.geocodeAddress(req.getAddress());
        location.setFarmId(savedFarm.getId()); //
        locationRepo.save(location);




        // 3. 작물 저장
        Crops crop = new Crops();
        crop.setName(req.getCropName());
        crop.setFarm(savedFarm);
        cropsRepo.save(crop);

        // 4. 멤버 등록 (소유자)
        FarmMember member = new FarmMember();
        member.setUserId(currentUserId);
        member.setFarm(savedFarm);
        member.setRole(FarmRole.OWNER);
        member.setJoinedAt(OffsetDateTime.now());
        farmMemberRepo.save(member);

        savedFarm.setLocation(location);

        return savedFarm;
    }

    // #2 내 농장 목록 (farm_member 통해 소유 농장)
    public List<FarmSummary> getMyFarms(Long userId) {
        // 1단계: 기존 안전 메서드 사용 (새 메서드 의존성 제거)
        List<FarmMember> farmMembers = farmMemberRepo.findAllByMemberIdWithFarm(userId);  // 기존 메서드!

        return farmMembers.stream()
                .map(fm -> {
                    Farm farm = fm.getFarm();  // fm.getFarm() 안전

                    // OWNER 이름 (기존 메서드 + userRepo)
                    String ownerName = farmMemberRepo.findByFarmIdAndRole(farm.getId(), FarmRole.OWNER)
                            .flatMap(owner -> userRepo.findById(owner.getUserId()))
                            .map(User::getNickname)
                            .orElse("알 수 없음");

                    // 멤버 목록 (기존 메서드 + userRepo)
                    List<FarmMember> allMembers = farmMemberRepo.findAllByFarmId(farm.getId());
                    List<FarmMemberInfo> members = allMembers.stream()
                            .map(member -> {
                                User user = userRepo.findById(member.getUserId())
                                        .orElse(new User());  // Null 안전
                                return new FarmMemberInfo(
                                        user.getId(),
                                        user.getNickname(),
                                        member.getRole()
                                );
                            })
                            .toList();

                    // MEMBER 수 계산
                    int memberCount = (int) members.stream()
                            .filter(m -> m.getRole() == FarmRole.MEMBER)  // ← getRole() 안전
                            .count();

                    // 작물 (기존 그대로)
                    List<String> cropNames = cropsRepo.findAllByFarm_Id(farm.getId())
                            .stream().map(Crops::getName).toList();

                    // 위치 Null 안전
                    String location = locationRepo.findByFarmId(farm.getId())
                            .map(Location::getAddress)
                            .orElse("주소 정보 없음");

                    // DTO 빌드 (기존 그대로)
                    return FarmSummary.builder()
                            .farmId(farm.getId())
                            .name(farm.getName())
                            .location(location)
                            .area(farm.getArea())
                            .ownerName(ownerName)
                            .memberCount(memberCount)
                            .crops(cropNames)
                            .members(members)
                            .role(fm.getRole())
                            .img(farm.getImagePath())
                            .createdDate(farm.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());  // ← 명시적 타입 변환
    }


    @Transactional(readOnly = true)
    public FarmSummaryResponse getFarmSummary(Long userId) {
        long total = farmMemberRepo.countByUserId(userId);
        long owned = farmMemberRepo.countByUserIdAndRole(userId, FarmRole.OWNER);
        long joined = farmMemberRepo.countByUserIdAndRole(userId, FarmRole.MEMBER);

        return FarmSummaryResponse.builder()
                .totalFarmCount(total)
                .ownedFarmCount(owned)
                .joinedFarmCount(joined)
                .build();
    }

    @Transactional
    public void deleteFarm(Long farmId, Long userId) {
        // 1. 권한 체크 (주인만 삭제 가능)
        FarmMember member = farmMemberRepo.findByFarmIdAndUserId(farmId, userId)
                .orElseThrow(() -> new RuntimeException("해당 농장에 대한 권한이 없습니다."));

        if (member.getRole() != FarmRole.OWNER) {
            throw new RuntimeException("농장주만 농장을 삭제할 수 있습니다.");
        }
        // 2. 연관 데이터 삭제 (순서 중요: 자식부터 지우기)
        locationRepo.deleteByFarmId(farmId);
        cropsRepo.deleteAllByFarm_Id(farmId);
        farmMemberRepo.deleteAllByFarmId(farmId);

        // 3. 농장 본체 삭제
        farmRepo.deleteById(farmId);

    }

    @Transactional
    public void addMemberByEmail(Long farmId, Long inviterId, String email) {

        // 1. 초대자가 OWNER인지 체크
        FarmMember inviter = farmMemberRepo
                .findByFarmIdAndUserId(farmId, inviterId)
                .orElseThrow(() -> new FarmException("농장 멤버가 아닙니다."));

        if (inviter.getRole() != FarmRole.OWNER) {
            throw new FarmException("OWNER만 멤버를 추가할 수 있습니다.");
        }

        // 2. 이메일로 유저 조회
        User targetUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new FarmException("해당 이메일을 가진 유저가 존재하지 않습니다."));

        Long invitedUserId = targetUser.getId();

        // 3. 이미 멤버인지 확인
        if (farmMemberRepo.existsByFarmIdAndUserId(farmId, invitedUserId)) {
            throw new FarmException("이미 해당 농장의 멤버입니다.");
        }

        // 4. 멤버 추가
        Farm farm = farmRepo.findById(farmId)
                .orElseThrow(() -> new FarmException("농장이 존재하지 않습니다."));

        FarmMember member = new FarmMember();
        member.setFarm(farm);
        member.setUserId(invitedUserId);
        member.setRole(FarmRole.MEMBER);
        member.setJoinedAt(OffsetDateTime.now());

        farmMemberRepo.save(member);
    }


}

