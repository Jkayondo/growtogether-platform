-- GT School Release 1
-- A11.1a — Explicit timetable lifecycle evidence types
--
-- IMPROVEMENT:
-- Consequential timetable lifecycle transitions must be represented
-- explicitly in the immutable change-history evidence rather than
-- being collapsed into OTHER.

ALTER TABLE gts_timetable_change_history
    DROP CONSTRAINT ck_gts_timetable_change_type;

ALTER TABLE gts_timetable_change_history
    ADD CONSTRAINT ck_gts_timetable_change_type
        CHECK (
            change_type IN (
                'TIMETABLE_CREATED',
                'TIMETABLE_GENERATED',
                'TIMETABLE_SUBMITTED_FOR_REVIEW',
                'TIMETABLE_APPROVED',
                'TIMETABLE_PUBLISHED',
                'TIMETABLE_ACTIVATED',
                'TIMETABLE_SUSPENDED',
                'ENTRY_ADDED',
                'ENTRY_UPDATED',
                'ENTRY_MOVED',
                'ENTRY_CANCELLED',
                'TEACHER_CHANGED',
                'ROOM_CHANGED',
                'PERIOD_CHANGED',
                'CONFLICT_DETECTED',
                'CONFLICT_RESOLVED',
                'VERSION_SUPERSEDED',
                'OTHER'
            )
        );
