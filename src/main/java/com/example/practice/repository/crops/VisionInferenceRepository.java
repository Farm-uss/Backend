package com.example.practice.repository.crops;

import com.example.practice.entity.crops.VisionInference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisionInferenceRepository extends JpaRepository<VisionInference, Long> {

    void deleteAllByCapture_CameraIdIn(List<Long> cameraIds);
}
