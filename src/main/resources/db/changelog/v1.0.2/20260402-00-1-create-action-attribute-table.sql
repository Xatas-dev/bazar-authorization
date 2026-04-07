--liquibase formatted sql
--changeset AsterYng:2
--description Create table action attribute
create table action_attribute
(
    id           int primary key,
    action_id    bigint       not null references action (id),
    name         varchar(255) not null,
    display_name varchar(255) not null,
    value_type   varchar(255) not null,
    created_at   timestamptz  not null,
    updated_at   timestamptz  not null
);

comment on table action_attribute is 'Action attribute dictionary';
comment on column action_attribute.id is 'Action attribute ID';
comment on column action_attribute.action_id is 'Linked action ID';
comment on column action_attribute.name is 'Attribute key (for example grantable_permissions)';
comment on column action_attribute.display_name is 'Display name for frontend';
comment on column action_attribute.value_type is 'Value type (int, string, boolean, arrays, etc.)';
comment on column action_attribute.created_at is 'Insert timestamp';
comment on column action_attribute.updated_at is 'Update timestamp';