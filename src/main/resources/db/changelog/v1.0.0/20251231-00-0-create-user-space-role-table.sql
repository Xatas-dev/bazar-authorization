--liquibase formatted sql
--changeset AsterYng:1
--description Create table user_space_role
create table user_space_role(
    space_id bigint not null,
    user_id uuid not null,
    role varchar(64) not null,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    primary key (space_id, user_id)
);


comment on column user_space_role.space_id is 'Space id foreign key';
comment on column user_space_role.user_id is 'User id';
comment on column user_space_role.created_at is 'Created at timestamp';
comment on column user_space_role.updated_at is 'Updated at timestamp';
