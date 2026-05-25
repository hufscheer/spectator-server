-- Drop legacy global UNIQUE on student_number (cross-organization leak source).
ALTER TABLE players DROP INDEX uc_players_student_number;

-- Re-scope uniqueness to (organization_id, student_number).
-- Players whose organization_id is NULL (legacy rows) remain exempt because
-- composite UNIQUE indexes ignore rows containing NULL in any column.
ALTER TABLE players
    ADD CONSTRAINT uc_players_org_student_number UNIQUE (organization_id, student_number);
