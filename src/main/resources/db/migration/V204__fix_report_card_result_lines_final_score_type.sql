ALTER TABLE report_card_result_lines
ALTER COLUMN final_score TYPE DOUBLE PRECISION
USING final_score::DOUBLE PRECISION;
