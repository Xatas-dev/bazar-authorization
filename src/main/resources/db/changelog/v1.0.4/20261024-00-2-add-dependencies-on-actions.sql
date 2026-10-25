--liquibase formatted sql
--changeset AsterYng:2
--description add basic dependencies on actions
-- chat_messages: WRITE → READ
update action set depends_on = 3 where id = 4;

-- chat_messages: DELETE → READ
update action set depends_on = 3 where id = 5;

-- roles: ASSIGN → READ
update action set depends_on = 8 where id = 9;

-- roles: MANAGE → READ
update action set depends_on = 8 where id = 16;

-- storage_node: UPLOAD → READ
update action set depends_on = 12 where id = 13;

-- storage_node: DOWNLOAD → READ
update action set depends_on = 12 where id = 14;

-- storage_node: DELETE → READ
update action set depends_on = 12 where id = 15;
