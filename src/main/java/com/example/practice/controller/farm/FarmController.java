package com.example.practice.controller.farm;

import com.example.practice.dto.farm.*;
import com.example.practice.entity.farm.Farm;
import com.example.practice.entity.farm.FarmRole;
import com.example.practice.exception.FarmException;
import com.example.practice.service.farm.FarmService;
import com.example.practice.common.config.TokenAuthFilter.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Farm", description = "농장 API")
@SecurityRequirement(name = "JWT")
@RestController
@RequestMapping("/farms")
@RequiredArgsConstructor
public class FarmController {
    private final FarmService farmService;

    @Operation(summary = "농장 추가", description = "이름, 면적(m^2), 주소, 작물 순으로 입력하면 됩니다")
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FarmResponse> createFarm(
            @RequestParam("name") String name,
            @RequestParam("area") Double area,
            @RequestParam("address") String address,
            @RequestParam("cropName") String cropName,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        FarmRequest req = new FarmRequest();
        req.setName(name);
        req.setArea(area);
        req.setAddress(address);
        req.setCropName(cropName);

        Farm farm = farmService.createFarm(req, image, user.id());
        return ResponseEntity.ok(FarmResponse.from(farm, FarmRole.OWNER));
    }


    @Operation(summary = "농장 조회", description = "농장 정보 및 내 역할 조회입니다.")
    @GetMapping("/my")
    public List<FarmSummary> getMyFarms(
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return farmService.getMyFarms(user.id());
        }

    @Operation(summary = "농장 요약 정보", description = "총 농장 수 및 역할별 농장 수를 조회합니다.")
    @GetMapping("/my/summary")
    public ResponseEntity<FarmSummaryResponse> getMyFarmSummary(
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return ResponseEntity.ok(farmService.getFarmSummary(user.id()));
    }


    @Operation(summary = "농장 삭제", description = "농장을 삭제합니다. OWNER만 삭제 가능")
    @DeleteMapping("/delete/{farmId}")
    public ResponseEntity<String> deleteFarm(
            @PathVariable Long farmId,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        farmService.deleteFarm(farmId, user.id());
        return ResponseEntity.ok("농장이 성공적으로 삭제되었습니다.");
    }

    @PostMapping("/{farmId}/members")
    public ResponseEntity<Void> addMemberByEmail(
            @PathVariable Long farmId,
            @RequestBody InviteRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        farmService.addMemberByEmail(farmId, user.id(), request.getEmail());
        return ResponseEntity.ok().build();
    }

    // FarmController.java에 추가
    @Operation(summary = "농장 멤버 내보내기", description = "OWNER만 멤버 내보낼 수 있음")
    @DeleteMapping("/{farmId}/members/{memberUserId}")
    public ResponseEntity<String> removeMember(
            @PathVariable Long farmId,
            @PathVariable Long memberUserId,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        farmService.removeMember(farmId, memberUserId, user.id());
        return ResponseEntity.ok("멤버가 성공적으로 내보내졌습니다.");
    }



    @ExceptionHandler(FarmException.class)
    public ResponseEntity<String> handleFarmException(FarmException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

}
