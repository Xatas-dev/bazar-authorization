--liquibase formatted sql
--changeset AsterYng:1
--description create unique index on space_id + user_id

create unique index uq_space_id_user_id_idx on space_user (space_id, user_id);