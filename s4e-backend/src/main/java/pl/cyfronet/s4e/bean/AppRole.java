package pl.cyfronet.s4e.bean;

public enum AppRole {
    /**
     * Institution admin, can:
     * - edit institution,
     * - add/edit user role,
     * - add child institution,
     * - add institution admin for child institution
     */
    INST_ADMIN,

    /**
     * Institution member, can read institution
     */
    INST_MEMBER
}
