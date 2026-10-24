--liquibase formatted sql
--changeset AsterYng:2
--description create manage action
insert into action(id, code, name, resource, resource_name, created_at, updated_at)
values (16, 'MANAGE', 'Управлять ролями', 'roles', 'Роли', now(), now());
