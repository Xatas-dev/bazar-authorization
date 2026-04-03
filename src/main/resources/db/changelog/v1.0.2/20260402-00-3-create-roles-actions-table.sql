--liquibase formatted sql
--changeset AsterYng:3
--description Create table roles_actions
create table roles_actions
(
    role_id            bigint not null references "role" (id),
    action_id          bigint not null references action (id),
    assigned_attribute jsonb,
    constraint pk_roles_actions primary key (role_id, action_id),
    constraint fk_roles_actions_role
        foreign key (role_id) references "role" (id),
    constraint fk_roles_actions_action
        foreign key (action_id) references "action" (id)
);

comment on table roles_actions is 'Role to action mapping';
comment on column roles_actions.role_id is 'Role ID';
comment on column roles_actions.action_id is 'Action ID';
comment on column roles_actions.assigned_attribute is 'Assigned action attributes with values';