// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

import sharedEnvironment from './base';

export const environment = {
  version: 'undefined-dev-version',
  ...sharedEnvironment,
  production: false,
  hmr: true,
  inviteOnly: false,
  uiDevelopment: true
};
