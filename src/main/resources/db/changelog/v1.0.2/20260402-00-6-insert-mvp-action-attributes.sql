--liquibase formatted sql
--changeset AsterYng:6
--description insert all mvp action attributes

insert into action_attribute(id, action_id, name, display_name, value_type, created_at, updated_at)
values
    (1, 10, 'grantable_actions', 'Доступные права', 'array', now(), now()),
    (2, 11, 'grantable_actions', 'Доступные права', 'array', now(), now()),
    (3, 10, 'manageable_roles', 'Доступные роли', 'array', now(), now());