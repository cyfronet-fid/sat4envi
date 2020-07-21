import sharedEnvironment from './base';
import commonEnvironmentVariables from './environment.common';

export const environment = {
  ...commonEnvironmentVariables,
  version: 'undefined-prod-version',
  ...sharedEnvironment,
  production: true,
  hmr: false,
  hmrPersist: false,
  inviteOnly: true,
  uiDevelopment: false
};

export default environment;
