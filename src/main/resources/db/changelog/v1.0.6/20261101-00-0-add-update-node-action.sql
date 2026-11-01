--liquibase formatted sql
--changeset seny1:1
--description add update node action

insert into action(id, code, name, resource, resource_name, created_at, updated_at)
values (17, 'UPDATE', 'Редактировать узлы', 'storage_node', 'Хранилище', now(), now());