import sharedEnvironment from './base';

export const environment = {
  ...sharedEnvironment,
  production: true,
  hmr: false,
  inviteOnly: true,
  uiDevelopment: false
};
