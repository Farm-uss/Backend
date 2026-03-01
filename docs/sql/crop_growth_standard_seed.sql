-- Crop growth standard seed (manual apply)
-- Purpose: crop_code / crop_name / base_temp / target_gdd 기준값 일괄 반영

BEGIN;

CREATE TABLE IF NOT EXISTS crop_growth_standard_history (
    id BIGSERIAL PRIMARY KEY,
    crop_code VARCHAR(2) NOT NULL,
    crop_name VARCHAR(100),
    before_base_temp NUMERIC(4,1),
    before_target_gdd NUMERIC(10,2),
    after_base_temp NUMERIC(4,1),
    after_target_gdd NUMERIC(10,2),
    changed_by VARCHAR(100) NOT NULL,
    reason TEXT,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

INSERT INTO crop_growth_standard
    (crop_code, crop_name, base_temp, target_gdd)
VALUES
    ('01', '딸기', 5.0, 1500.00),
    ('02', '토마토', 10.0, 1800.00),
    ('03', '파프리카', 10.0, 1900.00),
    ('04', '오이', 10.0, 1400.00),
    ('05', '고추', 10.0, 1700.00),
    ('06', '포도', 10.0, 1600.00),
    ('07', '상추', 4.0, 900.00),
    ('08', '배추', 5.0, 1200.00),
    ('09', '양배추', 5.0, 1300.00),
    ('10', '가지', 10.0, 1750.00),
    ('11', '호박', 10.0, 1500.00),
    ('12', '감자', 5.0, 1100.00),
    ('99', '기타', NULL, NULL)
ON CONFLICT (crop_code)
DO UPDATE SET
    crop_name = EXCLUDED.crop_name,
    base_temp = EXCLUDED.base_temp,
    target_gdd = EXCLUDED.target_gdd;

COMMIT;

-- Verify
SELECT crop_code, crop_name, base_temp, target_gdd
FROM crop_growth_standard
ORDER BY crop_code;

