-- Les rôles et permissions font partie du modèle applicatif. Même s'ils sont
-- référencés par leur code dans les API, leurs clés internes suivent également
-- la convention UUID.

ALTER TABLE role_permissions DROP CONSTRAINT FK_role_permissions_role;
ALTER TABLE role_permissions DROP CONSTRAINT FK_role_permissions_permission;
ALTER TABLE account_roles DROP CONSTRAINT FK_account_roles_role;

ALTER TABLE roles ADD uuid_id UNIQUEIDENTIFIER NULL;
GO
UPDATE roles SET uuid_id = NEWID();
ALTER TABLE roles ALTER COLUMN uuid_id UNIQUEIDENTIFIER NOT NULL;

ALTER TABLE permissions ADD uuid_id UNIQUEIDENTIFIER NULL;
GO
UPDATE permissions SET uuid_id = NEWID();
ALTER TABLE permissions ALTER COLUMN uuid_id UNIQUEIDENTIFIER NOT NULL;

ALTER TABLE role_permissions ADD role_uuid_id UNIQUEIDENTIFIER NULL;
ALTER TABLE role_permissions ADD permission_uuid_id UNIQUEIDENTIFIER NULL;
GO
UPDATE role_permission
SET role_uuid_id = role.uuid_id,
    permission_uuid_id = permission.uuid_id
FROM role_permissions role_permission
INNER JOIN roles role ON role.id = role_permission.role_id
INNER JOIN permissions permission ON permission.id = role_permission.permission_id;
ALTER TABLE role_permissions ALTER COLUMN role_uuid_id UNIQUEIDENTIFIER NOT NULL;
ALTER TABLE role_permissions ALTER COLUMN permission_uuid_id UNIQUEIDENTIFIER NOT NULL;

ALTER TABLE account_roles ADD role_uuid_id UNIQUEIDENTIFIER NULL;
GO
UPDATE account_role
SET role_uuid_id = role.uuid_id
FROM account_roles account_role
INNER JOIN roles role ON role.id = account_role.role_id;
ALTER TABLE account_roles ALTER COLUMN role_uuid_id UNIQUEIDENTIFIER NOT NULL;

ALTER TABLE role_permissions DROP CONSTRAINT PK_role_permissions;
ALTER TABLE account_roles DROP CONSTRAINT PK_account_roles;
ALTER TABLE roles DROP CONSTRAINT PK_roles;
ALTER TABLE permissions DROP CONSTRAINT PK_permissions;

ALTER TABLE role_permissions DROP COLUMN role_id;
ALTER TABLE role_permissions DROP COLUMN permission_id;
ALTER TABLE account_roles DROP COLUMN role_id;
ALTER TABLE roles DROP COLUMN id;
ALTER TABLE permissions DROP COLUMN id;

EXEC sp_rename N'dbo.roles.uuid_id', N'id', N'COLUMN';
EXEC sp_rename N'dbo.permissions.uuid_id', N'id', N'COLUMN';
EXEC sp_rename N'dbo.role_permissions.role_uuid_id', N'role_id', N'COLUMN';
EXEC sp_rename N'dbo.role_permissions.permission_uuid_id', N'permission_id', N'COLUMN';
EXEC sp_rename N'dbo.account_roles.role_uuid_id', N'role_id', N'COLUMN';
GO

ALTER TABLE roles ADD CONSTRAINT PK_roles PRIMARY KEY (id);
ALTER TABLE permissions ADD CONSTRAINT PK_permissions PRIMARY KEY (id);
ALTER TABLE role_permissions ADD CONSTRAINT PK_role_permissions PRIMARY KEY (role_id, permission_id);
ALTER TABLE account_roles ADD CONSTRAINT PK_account_roles PRIMARY KEY (account_id, role_id);
ALTER TABLE role_permissions ADD CONSTRAINT FK_role_permissions_role
    FOREIGN KEY (role_id) REFERENCES roles (id);
ALTER TABLE role_permissions ADD CONSTRAINT FK_role_permissions_permission
    FOREIGN KEY (permission_id) REFERENCES permissions (id);
ALTER TABLE account_roles ADD CONSTRAINT FK_account_roles_role
    FOREIGN KEY (role_id) REFERENCES roles (id);
