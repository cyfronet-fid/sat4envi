UPDATE user_role SET role = 'INST_MEMBER' WHERE role = 'GROUP_MEMBER';

DELETE FROM user_role WHERE role = 'INST_MANAGER' OR role = 'GROUP_MANAGER';
