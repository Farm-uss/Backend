package com.example.practice.service.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AwsS3Service {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile file, String dirName) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            return upload(file.getBytes(), file.getOriginalFilename(), file.getContentType(), dirName);

        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }
    }

    public String upload(byte[] bytes, String originalFilename, String contentType, String dirName) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        String safeOriginalName = (originalFilename == null || originalFilename.isBlank()) ? "file" : originalFilename;
        String fileName = UUID.randomUUID() + "_" + safeOriginalName;
        String key = dirName + "/" + fileName;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        metadata.setContentType(contentType);

        amazonS3.putObject(new PutObjectRequest(bucket, key, new ByteArrayInputStream(bytes), metadata));
        return amazonS3.getUrl(bucket, key).toString();
    }
}
