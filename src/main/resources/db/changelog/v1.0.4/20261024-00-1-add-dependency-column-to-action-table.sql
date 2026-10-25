--liquibase formatted sql
--changeset AsterYng:2
--description add dependency column to action table
alter table action
add column depends_on int
references action(id);

comment on column action.depends_on is 'Action ID that is required for this action to be in the same role';