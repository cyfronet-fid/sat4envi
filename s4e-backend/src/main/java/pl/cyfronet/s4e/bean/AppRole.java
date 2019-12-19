package pl.cyfronet.s4e.bean;

public enum AppRole {
    /**
     * Institution admin, can add child institution, can add institution admin for child institution
     */
    INST_ADMIN,
    /**
     * Institution manager, can add/edit group, add user, add user role, can edit institution
     */
    INST_MANAGER,
    /**
     * Group manager, can edit group, add user, add/edit user role
     */
    GROUP_MANAGER,
    /**
     * Group member, can read group items
     */
    GROUP_MEMBER
}
