package com.example.practice.entity.device;

import com.example.practice.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "camera",
        indexes = {
                @Index(name = "idx_camera_device_id", columnList = "device_id"),
                @Index(name = "idx_camera_stream_key", columnList = "stream_key", unique = true)
        }
)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"cameraId", "name", "streamKey", "status"})
public class Camera extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "camera_id")
    private Long cameraId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "stream_key", nullable = false, unique = true, length = 150)
    private String streamKey;

    @Column(name = "stream_protocol", nullable = false, length = 20)
    private String streamProtocol;

    @Column(name = "capture_endpoint", length = 255)
    private String captureEndpoint;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CameraStatus status;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    @Column(name = "last_seen_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime lastSeenAt;

    @Column(name = "last_captured_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime lastCapturedAt;
}
