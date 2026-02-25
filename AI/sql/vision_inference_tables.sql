CREATE TABLE IF NOT EXISTS image_capture (
    capture_id BIGSERIAL PRIMARY KEY,
    image_path VARCHAR(255),
    captured_at TIMESTAMP,
    camera_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS vision_inference (
    inference_id BIGSERIAL PRIMARY KEY,
    model_name VARCHAR(255),
    task_type VARCHAR(255),
    label VARCHAR(255),
    confidence NUMERIC,
    bbox_json JSONB,
    inferred_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    is_abnormal BOOLEAN,
    abnormal_reason TEXT,
    capture_id BIGINT NOT NULL,
    CONSTRAINT fk_vision_inference_capture
        FOREIGN KEY (capture_id) REFERENCES image_capture(capture_id)
);

CREATE INDEX IF NOT EXISTS idx_image_capture_camera_id ON image_capture(camera_id);
CREATE INDEX IF NOT EXISTS idx_image_capture_captured_at ON image_capture(captured_at);
CREATE INDEX IF NOT EXISTS idx_vision_inference_capture_id ON vision_inference(capture_id);
CREATE INDEX IF NOT EXISTS idx_vision_inference_inferred_at ON vision_inference(inferred_at);
