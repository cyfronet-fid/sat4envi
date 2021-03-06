// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

import sharedEnvironment from './base';
import commonEnvironmentVariables from './environment.common';

export const environment = {
  ...commonEnvironmentVariables,
  version: 'undefined-dev-version',
  ...sharedEnvironment,
  production: false,
  hmr: true,
  hmrPersist: true,
  inviteOnly: true,
  uiDevelopment: true
};

export default environment;
