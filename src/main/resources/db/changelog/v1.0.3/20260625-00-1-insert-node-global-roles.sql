--liquibase formatted sql
--changeset PavelKulebyakin:1
--description add some node actions to default role
insert into roles_actions (role_id, action_id, assigned_attribute, created_at, updated_at)
values (1, 12, null, now(), now()),
       (1, 14, null, now(), now())
