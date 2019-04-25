import { ID } from '@datorama/akita';
import {JsonObject, JsonProperty} from 'json2typescript';

export interface LoginFormState {
  login: string;
  password: string;
  rememberMe: boolean;
}

export interface Session {
  initialized: boolean;
  token: string|null;
  email: string|null;
  roles: string[];
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
