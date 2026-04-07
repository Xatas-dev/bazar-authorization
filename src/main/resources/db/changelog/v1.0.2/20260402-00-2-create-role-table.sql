--liquibase formatted sql
--changeset AsterYng:3
--description Create table role
create table "role"
(
    id         bigserial primary key,
    name       varchar(255),
    space_id   bigint,
    scope      varchar(50)        not null,
    created_at timestamptz        not null,
    updated_at timestamptz        not null
);

comment on table "role" is 'Role dictionary';
comment on column "role".id is 'Role ID';
comment on column "role".name is 'Role name';
comment on column "role".space_id is 'Space ID';
comment on column "role".scope is 'Role scope: GLOBAL, SPACE, USER';
comment on column "role".created_at is 'Insert timestamp';
comment on column "role".updated_at is 'Update timestamp';