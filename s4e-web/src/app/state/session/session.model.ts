export interface LoginFormState {
  login: string;
  password: string;
  rememberMe: boolean;
}

export interface Session {
  initialized: boolean;
  logged: boolean;
  email: string | null;
  roles: string[];
}

/**
 * A factory function that creates Session
 */
export function createSession(params: Partial<Session>) {
  return {
    initialized: false,
    logged: false,
    email: params.email || null,
    roles: params.roles || [],
  } as Session;
}
