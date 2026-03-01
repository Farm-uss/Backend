package com.example.practice.repository.device;

import com.example.practice.entity.device.EnvData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvDataRepository extends JpaRepository<EnvData, Long> {

    Page<EnvData> findAllByDevice_DeviceIdOrderByCreatedAtDesc(Long deviceId, Pageable pageable);
}

