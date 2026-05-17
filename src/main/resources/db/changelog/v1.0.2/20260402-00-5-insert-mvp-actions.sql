--liquibase formatted sql
--changeset AsterYng:5
--description insert all mvp actions for cerbos policies

insert into action(id, code, name, resource, created_at, updated_at)
values (1,'WRITE', 'Редактировать спейс', 'space', now(), now()),
       (2, 'DELETE', 'Удалять спейс', 'space', now(), now()),
       (3, 'READ', 'Читать сообщения в чате', 'chat_messages', now(), now()),
       (4, 'WRITE', 'Писать в чат', 'chat_messages', now(), now()),
       (5, 'DELETE', 'Удалять любые сообщения', 'chat_messages', now(), now()),
       (6, 'ADD', 'Добавлять новых пользователей в спейс', 'space_user', now(), now()),
       (7, 'DELETE', 'Кикать пользователей из спейса', 'space_user', now(), now()),
       (8, 'READ', 'Чекать роли', 'roles', now(), now()),
       (9, 'ASSIGN', 'Назначать роли', 'roles', now(), now()),
       (10, 'EDIT', 'Редактировать существующие роли', 'roles', now(), now()),
       (11, 'CREATE', 'Создавать роли', 'roles', now(), now());

