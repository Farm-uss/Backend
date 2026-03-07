CREATE TABLE IF NOT EXISTS disease_info (
    disease_id VARCHAR(50) PRIMARY KEY,
    disease_name VARCHAR(120) NOT NULL,
    disease_description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS disease_guide (
    guide_id BIGSERIAL PRIMARY KEY,
    disease_id VARCHAR(50) NOT NULL REFERENCES disease_info(disease_id),
    guide_type VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    sort_order INT
);

CREATE INDEX IF NOT EXISTS idx_disease_guide_disease_id ON disease_guide(disease_id);
CREATE INDEX IF NOT EXISTS idx_disease_guide_type ON disease_guide(guide_type);

INSERT INTO disease_info (disease_id, disease_name, disease_description, active) VALUES
('a7', '잎마름병', '잎 가장자리부터 갈변하며 진행되면 잎 전체가 마를 수 있습니다.', TRUE),
('a3', '흰가루병', '잎 표면에 흰가루가 퍼지며 광합성을 저해하는 병입니다.', TRUE),
('a5', '녹병', '잎 뒷면에 녹슨 가루 형태의 포자가 생기는 곰팡이성 병해입니다.', TRUE)
ON CONFLICT (disease_id) DO UPDATE SET
    disease_name = EXCLUDED.disease_name,
    disease_description = EXCLUDED.disease_description,
    active = EXCLUDED.active;

DELETE FROM disease_guide WHERE disease_id IN ('a7', 'a3', 'a5');

INSERT INTO disease_guide (disease_id, guide_type, content, sort_order) VALUES
('a7', 'CAUSE', '고온다습한 환경이 오래 지속될 때 발생하기 쉽습니다.', 1),
('a7', 'CAUSE', '통풍이 부족하고 잎 표면이 젖은 상태가 길어질 때 확산됩니다.', 2),
('a7', 'CAUSE', '감염된 잔재물이 남아 있을 경우 재발 가능성이 높습니다.', 3),
('a7', 'SYMPTOM', '잎 가장자리에 황갈색 반점이 생기고 점차 넓어집니다.', 1),
('a7', 'SYMPTOM', '병반 주변이 노랗게 변색되고 마르는 증상이 나타납니다.', 2),
('a7', 'SYMPTOM', '심해지면 생육이 저하되고 수확량이 감소합니다.', 3),
('a7', 'SOLUTION', '감염된 잎은 즉시 제거하고 재배지 밖으로 폐기합니다.', 1),
('a7', 'SOLUTION', '재배 밀도를 낮추고 환기를 개선해 잎의 건조 시간을 확보합니다.', 2),
('a7', 'SOLUTION', '병해 등록 약제를 사용 지침에 따라 살포하고 주기적으로 모니터링합니다.', 3),

('a3', 'CAUSE', '일교차가 크고 습도가 높을 때 발병 위험이 증가합니다.', 1),
('a3', 'CAUSE', '밀식 재배로 통풍이 나쁘면 병원균이 빠르게 퍼질 수 있습니다.', 2),
('a3', 'CAUSE', '질소 과다 시비는 연약한 조직을 만들어 감염 위험을 높입니다.', 3),
('a3', 'SYMPTOM', '잎에 하얀 가루 형태의 병반이 나타납니다.', 1),
('a3', 'SYMPTOM', '진행되면 잎이 누렇게 변하고 말리며 낙엽이 늘어납니다.', 2),
('a3', 'SYMPTOM', '신초 생장이 둔화되고 전반적인 초세가 약해집니다.', 3),
('a3', 'SOLUTION', '감염 부위를 제거해 초기 전염원을 줄입니다.', 1),
('a3', 'SOLUTION', '환기와 일조를 확보하고 과습을 피합니다.', 2),
('a3', 'SOLUTION', '작물과 시기에 맞는 흰가루병 방제제를 교호 살포합니다.', 3),

('a5', 'CAUSE', '장기간의 높은 습도와 잎 젖음 시간이 주요 원인입니다.', 1),
('a5', 'CAUSE', '질소 과다와 밀식 재배는 발병을 촉진할 수 있습니다.', 2),
('a5', 'CAUSE', '병든 잔재물 관리가 미흡하면 다음 작기 전염원이 됩니다.', 3),
('a5', 'SYMPTOM', '잎 뒷면에 주황색 또는 갈색 포자 무리가 형성됩니다.', 1),
('a5', 'SYMPTOM', '병반이 확대되면서 잎의 황화와 조기 낙엽이 나타납니다.', 2),
('a5', 'SYMPTOM', '광합성 저하로 생육 정체 및 품질 저하가 발생합니다.', 3),
('a5', 'SOLUTION', '초기 감염 잎을 제거해 전파를 차단합니다.', 1),
('a5', 'SOLUTION', '관수 시간을 조절해 잎 젖음 시간을 줄이고 통풍을 개선합니다.', 2),
('a5', 'SOLUTION', '등록 약제를 예방 및 초기 단계에서 사용합니다.', 3);
