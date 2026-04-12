TRUNCATE TABLE
    user_space_role,
    space_user
    CASCADE;

delete from roles_actions as ra
where ra.role_id in (select id from role where scope != 'GLOBAL');

delete from role
where scope != 'GLOBAL';

