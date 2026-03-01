package com.example.practice.service.crops;

import com.example.practice.dto.crops.CropStandardResponse;
import com.example.practice.repository.crops.CropGrowthStandardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CropStandardService {

    private final CropGrowthStandardRepository cropGrowthStandardRepository;

    @Transactional(readOnly = true)
    public List<CropStandardResponse> getAllStandards() {
        return cropGrowthStandardRepository.findAllByOrderByCropCodeAsc()
                .stream()
                .map(row -> new CropStandardResponse(
                        row.getCropCode(),
                        row.getCropName(),
                        row.getBaseTemp(),
                        row.getTargetGdd()
                ))
                .toList();
    }
}

