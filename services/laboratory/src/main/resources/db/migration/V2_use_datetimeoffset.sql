ALTER TABLE analysis_requests
    ALTER COLUMN created_at TYPE TIMESTAMP(6) WITH TIME ZONE
    USING created_at AT TIME ZONE 'UTC';

ALTER TABLE specimens
    ALTER COLUMN collected_at TYPE TIMESTAMP(6) WITH TIME ZONE
    USING collected_at AT TIME ZONE 'UTC';

ALTER TABLE specimens
    ALTER COLUMN received_at TYPE TIMESTAMP(6) WITH TIME ZONE
    USING received_at AT TIME ZONE 'UTC';

ALTER TABLE analysis_results
    ALTER COLUMN entered_at TYPE TIMESTAMP(6) WITH TIME ZONE
    USING entered_at AT TIME ZONE 'UTC';

ALTER TABLE analysis_results
    ALTER COLUMN validated_at TYPE TIMESTAMP(6) WITH TIME ZONE
    USING validated_at AT TIME ZONE 'UTC';
