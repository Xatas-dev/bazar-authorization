--liquibase formatted sql
--changeset AsterYng:1
--description insert all mvp global roles

insert into role (id, name, space_id, scope, created_at, updated_at)
values (1, 'Создатель', null, 'GLOBAL', now(), now()),
       (2, 'Лил непищик', null, 'GLOBAL', now(), now());

--Creator
insert into roles_actions (role_id, action_id, assigned_attribute)
values (1, 1, null),
       (1, 2, null),
       (1, 3, null),
       (1, 4, null),
       (1, 5, null),
       (1, 6, null),
       (1, 7, null),
       (1, 8, null);

-- Lil
insert into roles_actions (role_id, action_id, assigned_attribute)
values (2, 3, null),
       (2, 4, null),
       (2, 5, null);