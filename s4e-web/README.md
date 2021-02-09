## S4E Frontend

This readme contains information about setting up and developing
Sat4Env frontend.

#### Requirements

* nodejs `14.15.5`
* npm `^6`

#### Setting up development environment

To install all dependencies run:

```bash
npm install
```

#### Running application

This application requires backend to be working, refer 
to the general documentation about running it and all related services [here](../README.md)

Application is written in angular so all standard `ng` commands will work, but for convenience
npm scripts has been defined:

* `npm start` starts development server with proxy to backend and HMR enabled.
* `npm run extract-i18n` extracts translation string from the application

So to start application for development run:

```bash
npm start
```

**NOTICE**: Local web server will be available on https://localhost:4200/, and it will start with `/api` route proxied to `http://localhost:4201`, where backend should be present.

#### Other important information

Application is written in Angular 7+, with support for [ReduxDevTools](https://github.com/zalmoxisus/redux-devtools-extension) via [Akita Store](https://netbasal.gitbook.io/akita/)

#### Testing

Unit tests use [JEST framework](https://jestjs.io/) integrated with angular via [@angular-builders/jest](https://github.com/meltedspark/angular-builders/tree/master/packages/jest). 
For E2E [cypress.io](https://www.cypress.io/) is used.

#### Build 

Run `ng build` to build the project. The build artifacts will be stored in the `target/` directory. Use the `--prod` flag for a production build.

Running `ng build --prod` may fail due to a bug in either angular-cli or openlayers: https://github.com/openlayers/openlayers/issues/8357.
Makeshift fix is to remove `sideEffects` property from `node_modules/ol/package.json` as described in the github issue. 

When building with `mvn`, you can customize the `run-script` build execution's argument.
By default, it runs the script `build`, but you can override it with an environment variable `NPM_BUILD_RUN_SCRIPT_ARG`,
e.g., `NPM_BUILD_RUN_SCRIPT_ARG=build-private ../mvnw package`.
