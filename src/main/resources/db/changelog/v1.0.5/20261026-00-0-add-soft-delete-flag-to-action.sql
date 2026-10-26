--liquibase formatted sql
--changeset AsterYng:7
--description add soft-delete flag to action table and temporarily hide roles: ASSIGN and roles: MANAGE

alter table action
add column deleted boolean not null default false;

comment on column action.deleted is 'Soft-delete flag. True = action is hidden from UI and cannot be granted/used by non-creators';

-- Temporarily block roles: ASSIGN and roles: MANAGE for everyone except the space owner (creator)
update action set deleted = true where resource = 'roles' and code in ('ASSIGN', 'MANAGE');