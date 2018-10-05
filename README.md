# sat4envi

## s4e-backend

### Prerequisites

The application uses postgres database at `localhost:5433`.
The database can be run in a docker container by issuing `docker-compose up --rm` in root directory.

In case of changes in `docker-compose.yml` to remove the database volume it is necessary to issue `docker volume rm <volume name>`.
`docker volume ls` lists volumes.

### Building and running

To build run `mvn package` in directory `s4e-backend`. This will produce a jar in `s4e-backend/target`, which can be
started by running `java -cp <path to jar> pl.cyfronet.s4e.Application`.

Alternatively, the app can be built and run by issuing `mvn spring-boot:run` in directory `s4e-backend`. 

Both methods will expose the server under `http://localhost:4201`.


## s4e-web

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 6.0.8.

The snippets presented here should be run in directory `s4e-web`.

### Development server

Run `npm start` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

The server will start with `/api` route proxied to `http://localhost:4201`, where backend should be present.

### Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

### Build

Run `ng build` to build the project. The build artifacts will be stored in the `target/` directory. Use the `--prod` flag for a production build.

Running `ng build --prod` may fail due to a bug in either angular-cli or openlayers: https://github.com/openlayers/openlayers/issues/8357.
Makeshift fix is to remove `sideEffects` property from `node_modules/ol/package.json` as described in the github issue. 

### Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

### Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).

### Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).
