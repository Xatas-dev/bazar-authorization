--liquibase formatted sql
--changeset AsterYng:4
--description Create table space_user
create table space_user
(
    id         bigserial primary key,
    space_id   bigint             not null,
    user_id    varchar(255)       not null,
    role_id    bigint             not null references "role" (id),
    creator    boolean            not null,
    created_at timestamptz        not null,
    updated_at timestamptz        not null
);

comment on table space_user is 'User membership in a space';
comment on column space_user.id is 'Space user row ID';
comment on column space_user.space_id is 'Space ID';
comment on column space_user.user_id is 'User identifier';
comment on column space_user.role_id is 'Role ID';
comment on column space_user.creator is 'Is this user the space creator';
comment on column space_user.created_at is 'Insert timestamp';
comment on column space_user.updated_at is 'Update timestamp';