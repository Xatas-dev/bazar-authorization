--liquibase formatted sql
--changeset AsterYng:2
--description Create table action
create table action
(
    id         int primary key,
    code       varchar(64)  not null,
    name       varchar(128) not null,
    resource   varchar(64)  not null,
    created_at timestamptz  not null,
    updated_at timestamptz  not null
);

comment on column action.id is 'Action ID';
comment on column action.code is 'Action code (READ, WRITE, etc.)';
comment on column action.name is 'Human readable action name';
comment on column action.resource is 'Resource name (space, chat, chat_message, etc.)';
comment on column action.created_at is 'Insert timestamp';
comment on column action.updated_at is 'Update timestamp';