package com.example.practice.repository.device;

import com.example.practice.entity.device.Camera;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CameraRepository extends JpaRepository<Camera, Long> {

    Optional<Camera> findFirstByDevice_FarmIdAndPrimaryTrueOrderByCameraIdAsc(Long farmId);

    Optional<Camera> findFirstByDevice_FarmIdOrderByPrimaryDescCameraIdAsc(Long farmId);

    Optional<Camera> findByCameraIdAndDevice_FarmId(Long cameraId, Long farmId);

    List<Camera> findAllByDevice_FarmId(Long farmId);

    void deleteAllByDevice_FarmId(Long farmId);
}
