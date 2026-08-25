IF DB_ID('hospital_auth') IS NULL
BEGIN
    CREATE DATABASE hospital_auth;
END;
GO

IF DB_ID('hospital_core') IS NULL
BEGIN
    CREATE DATABASE hospital_core;
END;
GO

IF DB_ID('hospital_account') IS NULL
BEGIN
    CREATE DATABASE hospital_account;
END;
GO

IF DB_ID('hospital_organization') IS NULL
BEGIN
    CREATE DATABASE hospital_organization;
END;
GO

IF DB_ID('hospital_laboratory') IS NULL
BEGIN
    CREATE DATABASE hospital_laboratory;
END;
GO

IF DB_ID('hospital_patient') IS NULL
BEGIN
    CREATE DATABASE hospital_patient;
END;
GO

IF DB_ID('hospital_personnel') IS NULL
BEGIN
    CREATE DATABASE hospital_personnel;
END;
GO
