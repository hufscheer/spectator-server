-- 1. units 테이블 생성
CREATE TABLE units
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    name            VARCHAR(255)          NOT NULL,
    organization_id BIGINT                NOT NULL,

    CONSTRAINT pk_units PRIMARY KEY (id),
    CONSTRAINT fk_units_on_organization FOREIGN KEY (organization_id) REFERENCES organizations (id)
);

-- 2. 기존 enum name → 한글 이름 매핑하여 units 테이블에 INSERT
INSERT INTO units (name, organization_id)
SELECT DISTINCT
    CASE t.unit
        WHEN 'ENGLISH' THEN '영어대학'
        WHEN 'OCCIDENTAL_LANGUAGES' THEN '서양어대학'
        WHEN 'ASIAN_LANGUAGES_AND_CULTURE' THEN '아시아언어문화대학'
        WHEN 'CHINESE_STUDIES' THEN '중국학대학'
        WHEN 'JAPANESE_STUDIES' THEN '일본어대학'
        WHEN 'SOCIAL_SCIENCES' THEN '사회과학대학'
        WHEN 'BUSINESS_AND_ECONOMICS' THEN '상경대학'
        WHEN 'BUSINESS' THEN '경영대학'
        WHEN 'EDUCATION' THEN '사범대학'
        WHEN 'AI_CONVERGENCE' THEN 'AI융합대학'
        WHEN 'INTERNATIONAL_STUDIES' THEN '국제학부'
        WHEN 'LD_AND_LT' THEN 'LD/LT학부'
        WHEN 'KOREAN_AS_A_FOREIGN_LANGUAGE' THEN 'KFL학부'
        WHEN 'LIBERAL_ARTS' THEN '자유전공학부'
        WHEN 'ETC' THEN '기타'
        ELSE t.unit
    END,
    t.organization_id
FROM teams t
WHERE t.organization_id IS NOT NULL;

-- 3. teams 테이블에 unit_id 컬럼 추가
ALTER TABLE teams ADD COLUMN unit_id BIGINT;

-- 4. 기존 데이터 마이그레이션: teams.unit → units.id 매핑
UPDATE teams SET unit_id = (
    SELECT u.id FROM units u
    WHERE u.organization_id = teams.organization_id
      AND u.name = CASE teams.unit
          WHEN 'ENGLISH' THEN '영어대학'
          WHEN 'OCCIDENTAL_LANGUAGES' THEN '서양어대학'
          WHEN 'ASIAN_LANGUAGES_AND_CULTURE' THEN '아시아언어문화대학'
          WHEN 'CHINESE_STUDIES' THEN '중국학대학'
          WHEN 'JAPANESE_STUDIES' THEN '일본어대학'
          WHEN 'SOCIAL_SCIENCES' THEN '사회과학대학'
          WHEN 'BUSINESS_AND_ECONOMICS' THEN '상경대학'
          WHEN 'BUSINESS' THEN '경영대학'
          WHEN 'EDUCATION' THEN '사범대학'
          WHEN 'AI_CONVERGENCE' THEN 'AI융합대학'
          WHEN 'INTERNATIONAL_STUDIES' THEN '국제학부'
          WHEN 'LD_AND_LT' THEN 'LD/LT학부'
          WHEN 'KOREAN_AS_A_FOREIGN_LANGUAGE' THEN 'KFL학부'
          WHEN 'LIBERAL_ARTS' THEN '자유전공학부'
          WHEN 'ETC' THEN '기타'
          ELSE teams.unit
      END
) WHERE teams.organization_id IS NOT NULL;

-- 5. unit_id FK 설정 및 기존 unit 컬럼 DROP
ALTER TABLE teams ADD CONSTRAINT fk_teams_on_unit FOREIGN KEY (unit_id) REFERENCES units (id);
ALTER TABLE teams DROP COLUMN unit;