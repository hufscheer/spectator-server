-- Team.organization_id FK 제거 (Organization → Unit → Team 단일 계층화)
-- 검증 (2026-05-25 prod):
--   teams.organization_id <> units.organization_id, NULL 모두 0건
--   teams.unit_id IS NULL 0건, units.organization_id IS NULL 0건
ALTER TABLE teams DROP FOREIGN KEY FK_TEAMS_ON_ORGANIZATION;
ALTER TABLE teams DROP COLUMN organization_id;
