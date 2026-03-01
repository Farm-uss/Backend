-- Crop standard manual ops (without admin API)
-- 운영자가 서버 접속 후 직접 실행하는 변경/검증/롤백 예시

-- 0) 사전 검증
SELECT crop_code, crop_name, base_temp, target_gdd
FROM crop_growth_standard
ORDER BY crop_code;

-- 1) 단건 변경 + crops 동기화 + 이력 저장 (트랜잭션)
-- 예시: 토마토(02) 기준값 변경
BEGIN;

WITH before_row AS (
    SELECT crop_code, crop_name, base_temp, target_gdd
    FROM crop_growth_standard
    WHERE crop_code = '02'
),
update_std AS (
    UPDATE crop_growth_standard
    SET base_temp = 9.5,
        target_gdd = 1850.00
    WHERE crop_code = '02'
    RETURNING crop_code, crop_name, base_temp, target_gdd
)
INSERT INTO crop_growth_standard_history
(
    crop_code, crop_name,
    before_base_temp, before_target_gdd,
    after_base_temp, after_target_gdd,
    changed_by, reason
)
SELECT
    b.crop_code, b.crop_name,
    b.base_temp, b.target_gdd,
    u.base_temp, u.target_gdd,
    'ops@farm-us', 'manual update'
FROM before_row b
JOIN update_std u ON u.crop_code = b.crop_code;

UPDATE crops c
SET base_temp = s.base_temp,
    target_gdd = s.target_gdd
FROM crop_growth_standard s
WHERE c.crop_code = s.crop_code
  AND c.crop_code = '02';

COMMIT;

-- 2) 변경 검증
SELECT crop_code, crop_name, base_temp, target_gdd
FROM crop_growth_standard
WHERE crop_code = '02';

SELECT crops_id, farm_id, name, crop_code, base_temp, target_gdd
FROM crops
WHERE crop_code = '02'
ORDER BY crops_id DESC
LIMIT 20;

SELECT id, crop_code, before_base_temp, after_base_temp, before_target_gdd, after_target_gdd, changed_by, reason, changed_at
FROM crop_growth_standard_history
WHERE crop_code = '02'
ORDER BY id DESC
LIMIT 20;

-- 3) 롤백 예시 (직전 이력 기준으로 원복)
BEGIN;

WITH latest AS (
    SELECT *
    FROM crop_growth_standard_history
    WHERE crop_code = '02'
    ORDER BY id DESC
    LIMIT 1
)
UPDATE crop_growth_standard s
SET base_temp = l.before_base_temp,
    target_gdd = l.before_target_gdd
FROM latest l
WHERE s.crop_code = l.crop_code;

UPDATE crops c
SET base_temp = s.base_temp,
    target_gdd = s.target_gdd
FROM crop_growth_standard s
WHERE c.crop_code = s.crop_code
  AND c.crop_code = '02';

COMMIT;

