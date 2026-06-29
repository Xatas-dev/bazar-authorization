--liquibase formatted sql
--changeset PavelKulebyakin:1
--description add storage node actions
insert into action(id, code, name, resource, resource_name, created_at, updated_at)
values (12, 'READ', 'Смотреть файлы в хранилище', 'storage_node', 'Хранилище' ,now(), now()),
       (13, 'UPLOAD', 'Загружать файлы в хранилище', 'storage_node', 'Хранилище' ,now(), now()),
       (14, 'DOWNLOAD', 'Скачивать файлы из хранилища', 'storage_node', 'Хранилище' , now(), now()),
       (15, 'DELETE', 'Удалять любые файлы из хранилища', 'storage_node', 'Хранилище' , now(), now())
