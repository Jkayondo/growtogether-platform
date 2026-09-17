-- GT AI Question Entity Alignment
-- Adds subject_name required by AIQuestion entity

ALTER TABLE ai_questions
    ADD COLUMN IF NOT EXISTS subject_name VARCHAR(250);

