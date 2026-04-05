--liquibase formatted sql
--changeset AsterYng:1
--description insert all mvp global roles

insert into role (id, name, space_id, scope, created_at, updated_at)
values (1, 'Создатель', null, 'GLOBAL', now(), now()),
       (2, 'Лил непищик', null, 'GLOBAL', now(), now());

--Creator
insert into roles_actions (role_id, action_id, assigned_attribute, created_at, updated_at)
values (1, 1, null, now(), now()),
       (1, 2, null, now(), now()),
       (1, 3, null, now(), now()),
       (1, 4, null, now(), now()),
       (1, 5, null, now(), now()),
       (1, 6, null, now(), now()),
       (1, 7, null, now(), now()),
       (1, 8, null, now(), now());

-- Lil
insert into roles_actions (role_id, action_id, assigned_attribute, created_at, updated_at)
values (2, 3, null, now(), now()),
       (2, 4, null, now(), now()),
       (2, 5, null, now(), now());