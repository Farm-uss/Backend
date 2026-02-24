CREATE TABLE IF NOT EXISTS growth_metrics (
    id BIGSERIAL PRIMARY KEY,
    crops_id BIGINT NOT NULL REFERENCES crops(crops_id),
    metric VARCHAR(30) NOT NULL,
    measured_date DATE NOT NULL,
    value NUMERIC(10,2) NOT NULL,
    source VARCHAR(10) NOT NULL,
    image_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_growth_metrics_crop_metric_date
    ON growth_metrics (crops_id, metric, measured_date);
