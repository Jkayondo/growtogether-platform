ALTER TABLE report_card_result_lines
ALTER COLUMN grade_point TYPE DOUBLE PRECISION
USING grade_point::DOUBLE PRECISION;
