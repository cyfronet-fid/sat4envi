export interface LoginFormState {
  login: string;
  password: string;
  rememberMe: boolean;
}

export interface Role {
  role: 'INST_ADMIN'|'INST_MANAGER'|'GROUP_MANAGER'|'GROUP_MEMBER',
  institutionSlug: string|null,
  groupSlug: string|null,
}

export interface Session {
  email: string;
  name: string;
  surname: string;
  roles: Role[];
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
    name: '',
    surname: '',
    memberZK: false,
    admin: false
  };
}
