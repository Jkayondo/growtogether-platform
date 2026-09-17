-- GT AI Question Entity Alignment
-- Adds topic required by AIQuestion entity

ALTER TABLE ai_questions
    ADD COLUMN IF NOT EXISTS topic VARCHAR(250);

