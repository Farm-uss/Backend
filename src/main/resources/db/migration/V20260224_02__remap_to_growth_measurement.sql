DROP TABLE IF EXISTS growth_metrics;

CREATE TABLE IF NOT EXISTS growth_measurement (
    growth_id BIGSERIAL PRIMARY KEY,
    crops_id BIGINT NOT NULL REFERENCES crops(crops_id),
    measured_at TIMESTAMPTZ NOT NULL,
    height NUMERIC(10,2),
    leaf_area NUMERIC(10,2),
    leaf_count INT,
    fruit_count INT,
    ai_raw_json TEXT,
    ai_summary TEXT,
    ai_confidence NUMERIC(5,4),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_growth_measurement_crop_measured_at
    ON growth_measurement (crops_id, measured_at);
