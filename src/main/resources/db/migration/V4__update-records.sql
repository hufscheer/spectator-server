ALTER TABLE records DROP COLUMN scored_quarter;
ALTER TABLE records ADD COLUMN scored_quarter_id BIGINT NOT NULL;

ALTER TABLE records MODIFY scored_at INT;

ALTER TABLE records
    ADD CONSTRAINT FK_RECORDS_ON_SCORED_QUARTER FOREIGN KEY (scored_quarter_id) REFERENCES quarters (id);