package com.example.practice.service.farm;

import com.example.practice.dto.farm.*;
import com.example.practice.entity.crops.Crops;
import com.example.practice.entity.farm.*;
import com.example.practice.entity.location.Location;
import com.example.practice.exception.FarmException;
import com.example.practice.repository.crops.CropsRepository;
import com.example.practice.repository.farm.FarmInvitationRepository;
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
    private final FarmInvitationRepository invitationRepo;
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
    public void inviteMember(Long farmId, Long inviterId, Long invitedUserId) {


        FarmMember inviter = farmMemberRepo
                .findByFarmIdAndUserId(farmId, inviterId)
                .orElseThrow(() -> new RuntimeException("농장 멤버가 아닙니다."));


        if (inviter.getRole() != FarmRole.OWNER) {
            throw new FarmException("OWNER만 초대할 수 있습니다.");  // ← 변경
        }
        if (inviterId.equals(invitedUserId)) {
            throw new FarmException("자기 자신은 초대할 수 없습니다.");
        }
        // 이미 멤버인지 확인
        if (farmMemberRepo.existsByFarmIdAndUserId(farmId, invitedUserId)) {
            throw new FarmException("이미 해당 농장의 멤버입니다.");
        }

        // 이미 초대가 있는지 확인
        if (invitationRepo.existsByFarmIdAndInvitedUserId(farmId, invitedUserId)) {
            throw new FarmException("이미 초대가 발송되었습니다.");
        }

        FarmInvitation invitation = new FarmInvitation();
        invitation.setFarmId(farmId);
        invitation.setInviterId(inviterId);
        invitation.setInvitedUserId(invitedUserId);
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setCreatedAt(OffsetDateTime.now());

        invitationRepo.save(invitation);
    }

    @Transactional(readOnly = true)
    public List<InvitationResponse> getMyInvitations(Long userId) {

        return invitationRepo
                .findAllByInvitedUserIdAndStatus(userId, InvitationStatus.PENDING)  // ← 기존 메서드 사용
                .stream()
                .map(inv -> {
                    // farm 연관 필드가 없으니, farmId로 직접 조회
                    Farm farm = farmRepo.findById(inv.getFarmId()).orElse(null);

                    return InvitationResponse.builder()
                            .invitationId(inv.getId())
                            .farmId(inv.getFarmId())
                            .farmName(farm != null ? farm.getName() : "삭제된 농장")
                            .status(inv.getStatus())
                            .createdAt(inv.getCreatedAt())
                            .build();
                })
                .toList();
    }



    @Transactional
    public void acceptInvitation(Long invitationId, Long currentUserId) {

        FarmInvitation invitation = invitationRepo.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("초대가 존재하지 않습니다."));

        // 초대 받은 사람만 가능
        if (!invitation.getInvitedUserId().equals(currentUserId)) {
            throw new RuntimeException("본인의 초대만 수락할 수 있습니다.");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("이미 처리된 초대입니다.");
        }

        Farm farm = farmRepo.findById(invitation.getFarmId())
                .orElseThrow(() -> new RuntimeException("농장이 존재하지 않습니다."));

        FarmMember member = new FarmMember();
        member.setFarm(farm);
        member.setUserId(currentUserId);
        member.setRole(FarmRole.MEMBER);
        member.setJoinedAt(OffsetDateTime.now());

        farmMemberRepo.save(member);

        invitation.setStatus(InvitationStatus.ACCEPTED);
    }




}

