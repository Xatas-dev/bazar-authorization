--liquibase formatted sql
--changeset AsterYng:1
--description delete create and edit actions
delete
from action_attribute
where action_id in (10, 11);

delete
from action
where id in (10, 11);


