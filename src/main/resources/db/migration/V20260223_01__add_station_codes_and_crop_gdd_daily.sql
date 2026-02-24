-- farm 컬럼
ALTER TABLE farm ADD COLUMN IF NOT EXISTS station_type VARCHAR(10) NOT NULL DEFAULT 'SPOT';
ALTER TABLE farm ADD COLUMN IF NOT EXISTS obsr_spot_code VARCHAR(20);
ALTER TABLE farm ADD COLUMN IF NOT EXISTS zone_code VARCHAR(20);
ALTER TABLE farm ADD COLUMN IF NOT EXISTS station_name VARCHAR(50);
ALTER TABLE farm ADD COLUMN IF NOT EXISTS station_updated_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_farm_obsr_spot_code ON farm (obsr_spot_code);
CREATE INDEX IF NOT EXISTS idx_farm_zone_code ON farm (zone_code);

-- crops 컬럼
ALTER TABLE crops ADD COLUMN IF NOT EXISTS growth_temp_crop_code VARCHAR(2);
ALTER TABLE crops ADD COLUMN IF NOT EXISTS base_temp NUMERIC(4,1);

-- crop_growth_standard 컬럼
ALTER TABLE crop_growth_standard ADD COLUMN IF NOT EXISTS crop_code VARCHAR(2);
CREATE INDEX IF NOT EXISTS idx_crop_growth_standard_crop_code ON crop_growth_standard (crop_code);

-- crop_gdd_daily
CREATE TABLE IF NOT EXISTS crop_gdd_daily (
                                              id BIGSERIAL PRIMARY KEY,
                                              crop_id BIGINT NOT NULL REFERENCES crops(crops_id),
    target_date DATE NOT NULL,
    gdd NUMERIC(8,2) NOT NULL,
    gdd_normal_5y NUMERIC(8,2),
    base_temp NUMERIC(4,1) NOT NULL,
    station_type VARCHAR(10) NOT NULL,
    station_code VARCHAR(20) NOT NULL,
    source VARCHAR(30) NOT NULL DEFAULT 'KMA_OPENAPI',
    fetched_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE UNIQUE INDEX IF NOT EXISTS uk_crop_gdd_daily_crop_date
    ON crop_gdd_daily (crop_id, target_date);

CREATE INDEX IF NOT EXISTS idx_crop_gdd_daily_crop_date
    ON crop_gdd_daily (crop_id, target_date);