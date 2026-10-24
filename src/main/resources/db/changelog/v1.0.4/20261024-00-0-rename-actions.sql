--liquibase formatted sql
--changeset AsterYng:1
--description rename all actions

-- Чат
UPDATE action SET name = 'Читать сообщения'      WHERE id = 3;
UPDATE action SET name = 'Отправлять сообщения'   WHERE id = 4;
UPDATE action SET name = 'Удалять сообщения'      WHERE id = 5;

-- Спейс
UPDATE action SET name = 'Добавлять пользователей' WHERE id = 6;
UPDATE action SET name = 'Удалять пользователей'   WHERE id = 7;

-- Роли
UPDATE action SET name = 'Просматривать роли'      WHERE id = 8;

-- Хранилище
UPDATE action SET name = 'Просматривать файлы'     WHERE id = 12;
UPDATE action SET name = 'Загружать файлы'         WHERE id = 13;
UPDATE action SET name = 'Скачивать файлы'         WHERE id = 14;
UPDATE action SET name = 'Удалять файлы'           WHERE id = 15;