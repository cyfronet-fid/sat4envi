export interface LoginFormState {
  login: string;
  password: string;
  rememberMe: boolean;
}

export interface Role {
  role: string,
  institution: number|null,
  group: number|null,
}

export interface Session {
  initialized: boolean;
  token: string|null;
  email: string|null;
  roles: Role[];
}

export interface LoginRequestResponse {
  email: string;
  token?: string;
}

/**
 * A factory function that creates Session
 */
export function createSession(params: Partial<Session>) {
  return {
    initialized: false,
    token: params.token || null,
    email: params.email || null,
    roles: params.roles || [],
  } as Session;
}
