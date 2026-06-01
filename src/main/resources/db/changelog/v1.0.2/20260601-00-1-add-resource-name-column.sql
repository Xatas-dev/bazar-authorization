--liquibase formatted sql
--changeset AsterYng:1
--description Add resource_name column

alter table action
add column resource_name varchar(64);

update action
set resource_name = 'Чат'
where resource = 'chat_messages';

update action
set resource_name = 'Спейс'
where resource = 'space';

update action
set resource_name = 'Пользователи'
where resource = 'space_user';

update action
set resource_name = 'Роли'
where resource = 'roles';

comment on column action.resource_name is 'Resource name';

alter table action
alter column resource_name set not null;
