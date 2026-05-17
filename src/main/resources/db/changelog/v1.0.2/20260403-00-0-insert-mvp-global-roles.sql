--liquibase formatted sql
--changeset AsterYng:1
--description insert all mvp global roles

insert into role (id, name, space_id, scope, is_visible, created_at, updated_at)
values (1, 'Дефолтыч', null, 'GLOBAL', false, now(), now());

SELECT setval('role_id_seq', (SELECT MAX(id) FROM "role"));

--Creator
insert into roles_actions (role_id, action_id, assigned_attribute, created_at, updated_at)
values (1, 3, null, now(), now()),
       (1, 4, null, now(), now()),
       (1, 6, null, now(), now());