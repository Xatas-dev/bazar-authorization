--liquibase formatted sql
--changeset AsterYng:3
--description Create table role
create table "role"
(
    id         bigserial primary key,
    name       varchar(255)       not null,
    space_id   bigint,
    scope      varchar(50)        not null,
    is_visible boolean            not null,
    created_by varchar(48),
    created_at timestamptz        not null,
    updated_at timestamptz        not null
);

comment on table "role" is 'Role dictionary';
comment on column "role".id is 'Role ID';
comment on column "role".name is 'Role name';
comment on column "role".space_id is 'Space ID';
comment on column "role".scope is 'Role scope: GLOBAL, SPACE';
comment on column "role".is_visible is 'Is role name visible on UI';
comment on column "role".created_by is 'Role creator ID';
comment on column "role".created_at is 'Insert timestamp';
comment on column "role".updated_at is 'Update timestamp';