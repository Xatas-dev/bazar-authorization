--liquibase formatted sql
--changeset AsterYng:2
--description create manage action
insert into action(id, code, name, resource, resource_name, created_at, updated_at)
values (16, 'MANAGE', 'Управлять ролями', 'roles', 'Роли', now(), now());

insert
into action_attribute(id, action_id, name, display_name, value_type, created_at, updated_at)
values
    (5, 16, 'manageable_roles', 'Доступные роли', 'array', now(), now());

