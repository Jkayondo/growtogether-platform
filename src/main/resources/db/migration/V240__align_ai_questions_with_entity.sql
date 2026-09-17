-- GT AI Question Entity Alignment
-- Adds missing columns required by AIQuestion entity

ALTER TABLE ai_questions
    ADD COLUMN IF NOT EXISTS concept VARCHAR(250);

