import sharedEnvironment from './base';

export const environment = {
  ...sharedEnvironment,
  production: true,
  hmr: false,
  inviteOnly: false,
  uiDevelopment: false
};
