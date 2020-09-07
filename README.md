# sat4envi

## Requirements

- java `^11`
- docker `^18.09.3`
- docker-compose `^1.18.0`
- node `^11`,  npm `^6`


## System Components

System consists of three applications:

* frontend (directory `s4e-web` - angular application)
* backend (directory `s4e-backend` - Java Spring Boot application)
* GeoServer licensing gateway (directory `gs-gateway` - Java Spring Cloud Gateway Boot application)

### Sentry - monitoring

To use sentry for GeoServer Gateway or s4e-backend you need to set up dsn property in sentry.properties 
in GS-Gateway or s4e-backend:
```
dsn = <https://url with token to your sentry>
```

### s4e-backend

#### Prerequisites

The application uses postgres database at `localhost:5433`.
The database can be run in a docker container by issuing `docker-compose up db` in root directory.

In case of changes in `docker-compose.yml` to remove the database volume it is necessary to issue `docker volume rm <volume name>`.
`docker volume ls` lists volumes.

If a backend Dockerfile is updated, then you have to rebuild the image.
You can do it by running `docker-compose build`, it will try to rebuild the linked Dockerfiles.
The Dockerfile may be updated in cases such as updating java version.

#### <a id="backend-running-unit-tests"></a> Running unit tests

You will need the test db up and running: `docker-compose -f docker-compose-test.yml up db-test`.
It will be exposed at `localhost:5434`.

Then, run `./mvnw test`.


#### Running integration tests

You will need test db, minio, geoserver and broker for the tests to pass.

Run them with `docker-compose -f docker-compose-test.yml up db-test geoserver-test minio-test broker-test`.

Then, execute `./mvnw verify`.


#### Building and running

To build run `./mvnw package` in directory `s4e-backend`. This will produce a jar in `s4e-backend/target`, which can be
started by running `java -Dspring.profiles.active=development -jar <path to jar>`.
(For the tests to pass follow the instructions in [section about running unit tests](#backend-running-unit-tests) or skip them with `-DskipTests`.)

To set the version of the built artifacts use the `revision`, `sha1` and `changelist` system variables, for example:
`./mvnw package -Drevision=4.0.1 -Dsha1=<hash> -Dchangelist=-RELEASE`.
The values will be concatenated `${revision}${sha1}${changelist}` forming the artifact version.
By default `revision=0.0.0`, `sha1=` (empty) and `changelist=-SNAPSHOT`.

Alternatively, the app can be built and run by issuing `../mvnw spring-boot:run -Dspring-boot.run.profiles=development` in directory `s4e-backend`. 

Both methods will expose the server under `http://localhost:4201`.

During bootstrap Flyway will migrate the schema based on migrations placed in `src/main/resources/db/migration`.

In the `development` mode the DB is seeded by components from `pl.cyfronet.s4e.db.seed`.
They are executed on every startup, but they can also be run by setting appropriate profiles.

If you want Products in your instance, see [the instructions in Seeding Products](#seeding-products).

To bake version into the application (to be seen by the testers, etc) package application while having
environmental variable `NG_VERSION` set to the specified string which will be shown in the application.
For example: `NG_VERSION='1.1.1' ./mvnw package`


#### Application profiles

Spring application profile can be set with the var `spring.profiles.active`.
Several profiles can be specified in a comma separated list, e.g. `production,run-seed-products`.

`development`: 
- The seeds are run on every startup.
- Exception handler includes information about stacktraces.
- Mails are output to the logs.

`production`:
- No seeds are run by default.
- Exception handler excludes information about stacktraces.
- Mailer properties must be configured.

`run-seed-users`: Runs `SeedUsers`.

`run-seed-places`: Runs `SeedPlaces`.


#### Seeding Products

(Versions up to v11.0.x used Product seeding during application startup, if you need info about that removed
mechanism please refer to the history of `README.md`.)

After the backend boots up on a clean DB it will have no Schemas, Products, nor Scenes registered.
To initialize them, use admin endpoints: first creating Schemas, then, Products which depend on them, and finally run
a SyncJob to read Scenes in from Ceph.

Examples of their usage are in `s4e-backend/src/scripts/seed.sh`, a script which can also be used to perform
initialization based on a directory with configuration.

For the script to succeed, make sure you have correct admin credentials (admin user must be present in DB), DB hasn't
been seeded with Products yet, backend and GeoServer are connected to correct S3 endpoint, and backend has a proper
scene bucket set.
In particular, running `docker-compose up` will set it up correctly (although with bogus actual s3 data, only
metadata and scene files are copied with content, so GeoServer won't produce sensible imagery).

When the backend is running, issue `s4e-backend/src/scripts/seed.sh http://localhost:4201 <seed-config-path>`.
Set `<seed-config-path>` to lead to a seed-config, for example: `s4e-backend/seed-configs/seed-1`.

Extra envvars, which can be set on script:
- `BUCKET`: customizes the bucket name used by DB to generate granule paths for GeoServer,
- `SKIP_SEED_GEOSERVER`: set to `true` not to seed GeoServer (useful for PR instances),
- `ADMIN_EMAIL`: by default equal to `admin@mail.pl`,
- `ADMIN_PASSWORD`: by default equal to `adminPass20`.

The script will:
- authenticate (waiting with some timeout),
- set necessary property,
- create schemas,
- create products,
- synchronize scene prefixes,
- reset GeoServer workspace
- recreate overlays and synchronize them with GeoServer,
- create layer (one per product) in GeoServer.

The seed-config consists of a list of Schemas, Products and prefixes to scan.
Schemas are placed in separate files in directory `schema/`, their type is determined by name.
Products are put in `products.json`, which is an array of Product creation requests.
The prefixes to be scanned are put line by line in `prefixes.list`.

The existing seed-config can be used as a base for creating custom ones, just make sure to update `gs-gateway`
configuration when modifying product (layer) list.
By default, docker-compose uses envvar `GS_GATEWAY_DATASET_ENV_PATH=gsg-seed-1.env`, but it can be customized.

`seed-1` is configured to work with a local bucket `local-dataset-1`, with bogus data, however, it is also more or less
synchronized with bucket `s4e-sync-1` (`Opad_H05`, `Opad_H03` and `NatCol` are faulty as of 2020-08-31).
You can set SOK property `s3.bucket` or envvar `S3_BUCKET` to set the desired bucket name.

To access `s4e-sync-1` you need to obtain production CEPH credentials, they are for example
[here](https://docs.cyfronet.pl/display/FID/Projekty), use `s3.cloud...` endpoint.
Once you have them, point your GeoServer instance to the Cyfronet CEPH by setting the following evvars in `.env`:
```
GEOSERVER_S3_ENDPOINT=<endpoint>
GEOSERVER_S3_ACCESS_KEY=<access key>
GEOSERVER_S3_SECRET_KEY=<secret key>
```

The `.env` file is ignored in `.gitignore`, **UNDER NO CIRCUMSTANCES DO NOT COMMIT, NOR PUSH THIS FILE TO THE REPO!!!**

If you run with docker, package the application with `./mvnw package -DskipTests` first.

If you had already created a GeoServer container you may need to recreate it: `docker-compose down`, to remove
the container.
This will also erase other containers from the project.
Then, run required docker-compose services with `docker-compose up -d db geoserver broker`.

Then, run the backend with properties:
```
spring.profiles.active=development
s3.bucket=<bucket>
s3.access-key=<access_key>
s3.secret-key=<secret_key>
s3.endpoint=<path to storage>
```
If you run with docker, update the `backend-development.env` (or another env file you have wired up to `s4e-backend`
service in `docker-compose.yml`) by appending:
```
S3_BUCKET=<bucket>
S3_ACCESSKEY=<access_key>
S3_SECRETKEY=<secret_key>
S3_ENDPOINT=<path to storage>
```

The backend will initialize, then run the seeds.
If you're seeding `s4e-sync-1` there can be some errors in the logs, however, most likely it is fine.
Nevertheless, if you see S3ClientExceptions, or too many keys not found errors, you may need to evaluate if you have
correct credentials, bucket set, or if the Ceph infrastructure isn't down.


#### Scene updates from AMQP queue

Application can be configured to listen to notifications from an AMQP queue with S3 object updates.

In profile `development` this feature is enabled, and in `production` it has to be enabled by setting
`amqp.enabled=true` or `AMQP_ENABLED=true`.

By default, the listener subscribes to queue `s4e.scenes.incoming`.
When an error occurs during scene processing, the message is rejected with option not to requeue it.
The queue should have a dead-letter exchange configured to keep such notifications.

Related properties list and their defaults:
- `amqp.enabled=false`,
- `amqp.create-queues=false`: if set to true then the incoming queue is created on application startup
  (default in profiles `development`),
- `amqp.queues.incoming=s4e.scenes.incoming`: the name of the queue the module registers to. It must either
  exist or be created by setting `amqp.create-queues=true`.


#### Actuator endpoints

There are several HTTP actuator endpoints exposed:
- /actuator/health
- /actuator/info
- /actuator/prometheus

See [Actuator API docs](https://docs.spring.io/spring-boot/docs/current/actuator-api/html/) for more information.


#### Custom docker-compose local configurations

To customize the docker-compose configuration locally use [multiple Compose files](https://docs.docker.com/compose/extends/).

An example customization would be adding a file `/docker-compose.override.yml` with contents:
```yaml
services:
  s4e-backend:
    env_file:
      - backend-development.env
      - backend-custom.env
```
and `/backend-custom.env` with contents:
```properties
SPRING_PROFILES_ACTIVE=development
S3_BUCKET=s4e-demo
```
Both files are ignored by `.gitignore`, so they won't be accidentally committed and pushed to the public repository.

In this way, plain `docker-compose up` will pick up the override file and result in a customized behavior.


#### Places CSV generation

In case it is required to regenerate `places.csv` use `PlacesXlsxToCsv` tool.

First, obtain the current `miejscowosci.xlsx` file from [PRNG](http://www.gugik.gov.pl/pzgik/dane-bez-oplat/dane-z-panstwowego-rejestru-nazw-geograficznych-prng).
I assume you have built the jar file, so, being in the root directory, run
`java -cp s4e-backend/target/<jar> pl.cyfronet.s4e.tool.PlacesXlsxToCsv <path to miejscowosci.xlsx> s4e-backend/src/main/resources/db/places.csv`.
The generated file should overwrite the pre-existing one, and you can commit the resulting csv into the repo.


#### reCAPTCHA

It is configured with two properties:
```
recaptcha.validation.secretKey=6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe
recaptcha.validation.siteKey=6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI
```
The values present in `application-development.properties` (and above) are taken from the reCAPTCHA FAQ and the service
will always say that the response is ok when using them.
To test the production experience generate the keys yourself under https://www.google.com/recaptcha/admin/create.
(You can use the domain `localhost` to test locally.)


#### JWT signing

The application has to be configured with a key-store.
By default, in the development and test, we use a key-store **committed to the repository**.

In deployment, the key-store should be set using properties:
```
jwt.key-store=
jwt.key-store-password=
jwt.key-alias=
jwt.key-password=
```
or envvars:
```
JWT_KEYSTORE=<path to key-store>
JWT_KEYSTOREPASSWORD=<key-store password>
JWT_KEYALIAS=<key alias>
JWT_KEYPASSWORD=<key password>
```
The application requires passwords on both key-store and key.

Example snippet to generate PKCS12 keystore `$NAME.p12` and extract public key `$NAME.pub`:
```shell script
openssl req -x509 -newkey rsa:2048 -keyout $NAME.pem -out $NAME.crt
openssl rsa -pubout -in $NAME.pem -out $NAME.pub
openssl pkcs12 -export -inkey $NAME.pem -in $NAME.crt -out $NAME.p12
```


#### API docs

We use Swagger to create API documentation.
When you run the backend Swagger-UI is exposed under `http://localhost:4201/swagger-ui.html`.


#### <a id="backend-static"></a> Static images S3 storage

**This is somehow outdated, but describes the process more or less. **

To easily set bucket policies, install [minio client](https://github.com/minio/mc).

On Linux it is enough to run:
```bash
mkdir -p ~/bin
cp ~/bin
wget https://dl.min.io/client/mc/release/linux-amd64/mc
chmod +x mc
```
As ~/bin should be included in the PATH variable you can now use the tool system-wide.

Configure access to your local docker minio instance: 
```bash
mc config host add local-docker http://localhost:9001 minio minio123
```

Copy the `s4e-backend/static` to `tmp/minio-data` creating bucket `static`.

Set its policy to download: `mc policy set download local-docker/static`.


#### Miscellaneous

To check for dependency updates run `../mvnw versions:display-dependency-updates` in directory `s4e-backend`.


### s4e-web

Detailed frontend description can be found [here](./s4e-web/README.md)


### GeoServer Gateway

This component must be placed in front of GeoServer to provide licensing.

It reads and verifies JWT token in `Authorization` header.
The token can contain a claim `layers`, which gives access to restricted layers and scenes.

The gateway routes WMS requests to GeoServer instance.
It assumes a safe set of allowed query params.

The gateway keeps a list of OPEN and EUMETSAT layers.
If a requested layer is OPEN, then no authorization takes place and the request is allowed.
If a requested layer is EUMETSAT, then no authorization takes place if the requested time is more than 3h in the past.
Otherwise, the accessed layer must be contained in the `layers` claim, otherwise the request is rejected.

#### Docker image

The module directory contains a Dockerfile, which can be built with `docker build .`.


#### Running

To correctly run, the module requires the following configuration:

Server port. Property: `server.port` or envvar `SERVER_PORT`.

JWT public key. Property: `jwt.public-key` or envvar `JWT_PUBLICKEY`.
It is used to verify incoming JWT tokens and it must match with the one used in `s4e-backend`.

GeoServer URI. Property: `gateway.geoserver-uri` or envvar `GATEWAY_GEOSERVERURI`.

OPEN layers. Property: `gateway.open-layers` or envvar `GATEWAY_OPENLAYERS`.
A comma delimited list of layers with access type OPEN.
(Note name collision with front-end library openlayers.)

EUMETSAT layers. Property: `gateway.eumetsat-layers` or envvar `GATEWAY_EUMETSATLAYERS`.
A comma delimited list of layers with access type EUMETSAT.

An example of configuration can be found in `docker-compose.yml`.


#### Actuator endpoints

There are several HTTP actuator endpoints exposed:
- /actuator/health
- /actuator/info
- /actuator/prometheus

See [Actuator API docs](https://docs.spring.io/spring-boot/docs/current/actuator-api/html/) for more information.


## Development & Docker

In the root project directory `docker-compose.yml` is provide for developer's ease of use. 

**NOTE**: As for now application components run via docker-compose have fixed ports, so make sure that there is no application (especially **postgres**) conflicting with them.

This docker-compose recipe can be used to either run whole application for demo purposes, or to run specific parts of the application which can be usefull if for example we would like to develop only frontend part of the application.

In order do run docker-compose following steps must be done (**unless stated otherwise working directory should be project root**):

**NOTE**: If you use the fiddev/minio-test image then you can skip points 1-4.

1. Now it's time to get S3 data to the application.
   First make sure that you have [minio client](https://github.com/minio/mc) (`mc`) installed.
   
2. Configure `mc`.

   Add configuration for local-docker and cyfronet-ceph endpoints:
   ```bash
   mc config host add local-docker http://localhost:9001 minio minio123
   mc config host add cyfronet-ceph <url> <access_key> <secret_key>
   ```
   You'll need access to data available [here](https://docs.cyfronet.pl/display/FID/Projekty) (section **sat4envi/CEPH**).

   If running `mc ls cyfronet-ceph` prints output similar to the one below it means it's all good to go:
   ```bash
   $ mc ls cyfronet-ceph
   [2019-03-08 18:02:34 CET]      0B data-packs/
   [2018-06-24 15:44:38 CEST]      0B marta-test/
   [2018-08-01 13:53:15 CEST]      0B s4e-test-1/
   [2019-03-06 12:02:50 CET]      0B s4e-test-2/
   ```

3. Download s3 data for minio:

   ```bash
   mc cp cyfronet-ceph/data-packs/minio-data-v1.tar.xz .
   tar -xJf minio-data-v1.tar.xz -C ./tmp
   ```
   
   This step may not be necessary, depending on the data set used.
   
4. Create a bucket with static application content as well as shown [here](#backend-static).

5. Run `./mvnw package -DskipTests` in the root of the project, this will build artifacts for `s4e-backend` and `s4e-web`. 
   **NOTICE**: In some cases packaging may fail due to inability to compile some binary dependencies for frontend packages. In that case the easiest solution is to delete `s4e-web/node_modules` directory (`rm -rf s4e-web/node_modules`)

6. Run `docker-compose up`.

7. You're done - application should be available on https://localhost:4200 and have 3 available products.

**IMPORTANT** If you run `docker-compose up` you'll get application all set up, but it does not support any kind of live reload, etc. If you plan on developing any part of the project (frontend or backend) you should run docker compose without module you would like to develop - for example:

**Frontend**

```bash
docker-compose up -d
docker-compose stop s4e-web
```

And develop normally front end, while hosting it via `npm start / ng serve`

**Backend**

```bash
docker-compose up -d
docker-compose stop s4e-web
docker-compose stop s4e-backend
```

You will need to locally run frontend via `npm start / ng serve` and develop backend normally.


## Production docker-compose

When you are running in production set appropriate environment variables.
Most can be found in `docker-compose.yml`, excluding the ones which backend is configured with.
The template for backend vars is placed in `backend-production.env.template`, but you can override other variables too.
Copy the file, modify the values as required and load it by setting `BACKEND_ENV_PATH=<path to file>` (e.g. in `.env` file).
(The rule for env var matching to spring properties is roughly this: `SPRING_BASEPATH` matches `spring.basePath`.)


## FAQ

__Q: I get some FlywayException when running backend__

An example exception when the backend with Flyway is run using DB with schema generated by Hibernate:
```
Error starting Tomcat context.
[...]
nested exception is org.flywaydb.core.api.FlywayException: Found non-empty schema(s) "public" without schema history table! Use baseline() or set baselineOnMigrate to true to initialize the schema history table.
```

To fix it first try to drop your current DB schema.
You can connect to the DB from IntelliJ Database panel and drop all the tables
(databases->sat4envi->schemas->public, select all and drop from the context menu).

Alternatively, remove and recreate DB container:
```bash
docker-compose stop db
docker-compose rm db
docker-compose up db
```

__Q: Creating a read-only PostgreSQL user__

```postgresql
REVOKE CREATE ON SCHEMA public FROM public;
CREATE USER eva WITH PASSWORD 'secret';
CREATE ROLE readaccess;
GRANT readaccess TO eva;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO readaccess;
```
