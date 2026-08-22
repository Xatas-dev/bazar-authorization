--liquibase formatted sql
--changeset PavelKulebyakin:1
--description add message edit action
insert into action(id, code, name, resource, resource_name, created_at, updated_at)
values (16, 'UPDATE', 'Редактировать сообщения', 'chat_messages', 'Чат' ,now(), now())
