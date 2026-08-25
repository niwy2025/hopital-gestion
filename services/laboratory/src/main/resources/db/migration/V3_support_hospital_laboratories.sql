ALTER TABLE analysis_requests
    ADD laboratory_type NVARCHAR(30) NOT NULL
        CONSTRAINT DF_analysis_requests_laboratory_type DEFAULT N'REFERENCE' WITH VALUES;

EXEC sp_rename 'dbo.analysis_requests.reference_laboratory_code', 'laboratory_code', 'COLUMN';
