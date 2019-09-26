import {AuthConfig} from 'angular-oauth2-oidc';

export const authConfig: AuthConfig = {
  clientId: 's4e',
  dummyClientSecret: 'secret',
  tokenEndpoint: 'http://localhost:4200/oauth/token',
  useHttpBasicAuth: true,
  requireHttps: false,
  scope: '',
  timeoutFactor: 0.03
};
