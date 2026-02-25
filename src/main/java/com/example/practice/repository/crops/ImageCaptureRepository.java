package com.example.practice.repository.crops;

import com.example.practice.entity.crops.ImageCapture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageCaptureRepository extends JpaRepository<ImageCapture, Long> {
}
