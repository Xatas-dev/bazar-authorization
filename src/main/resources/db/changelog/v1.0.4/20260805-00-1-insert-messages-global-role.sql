--liquibase formatted sql
--changeset PavelKulebyakin:1
--description add message edit action to global
insert into roles_actions (role_id, action_id, assigned_attribute, created_at, updated_at)
values (1, 16, null, now(), now())
