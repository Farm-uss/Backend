package com.example.practice.entity.crops;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "image_capture")
public class ImageCapture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "capture_id")
    private Long captureId;

    @Column(name = "image_path", length = 255)
    private String imagePath;

    @Column(name = "captured_at")
    private OffsetDateTime capturedAt;

    @Column(name = "camera_id")
    private Long cameraId;
}
