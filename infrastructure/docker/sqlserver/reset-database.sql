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
