ALTER TABLE quarters ADD COLUMN _order INT DEFAULT 0;

ALTER TABLE timelines ADD COLUMN previous_quarter_id BIGINT NULL;

ALTER TABLE timelines
    ADD CONSTRAINT FK_TIMELINES_ON_PREV_QUARTER FOREIGN KEY (previous_quarter_id) REFERENCES quarters (id);

ALTER TABLE timelines ADD COLUMN previous_quarter_changed_at DATETIME NULL;
