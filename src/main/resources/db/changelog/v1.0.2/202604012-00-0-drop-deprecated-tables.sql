--liquibase formatted sql
--changeset AsterYng:1
--description drop old tables
drop table if exists user_space_role;