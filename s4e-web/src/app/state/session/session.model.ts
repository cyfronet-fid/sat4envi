export interface LoginFormState {
  email: string;
  password: string;
}

export interface Role {
  role: 'INST_ADMIN'|'INST_MANAGER'|'GROUP_MANAGER'|'INST_MEMBER',
  institutionSlug: string|null,
  groupSlug: string|null,
}

export interface Session {
  email: string;
  name: string;
  surname: string;
  roles: Role[];
  authorities: string[];
  memberZK: boolean;
  admin: boolean;
}

export interface PasswordChangeFormState {
  oldPassword: string;
  newPassword: string;
}

/**
 * A factory function that creates Session
 */
export function createSession(params: Partial<Session>): Session {
  return {
    email: params.email || null,
    roles: params.roles || [],
    authorities: params.authorities || [],
    name: '',
    surname: '',
    memberZK: false,
    admin: false
  };
}
