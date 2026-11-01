--liquibase formatted sql
--changeset seny1:2
--description add update node action to global role

insert into roles_actions (role_id, action_id, assigned_attribute, created_at, updated_at)
values (1, 17, null, now(), now())