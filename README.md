# sat4envi

## Requirements

- java `^11`
- docker `^18.09.3`
- docker-compose `^1.18.0`
- node `^8`,  npm `^5`


## System Components

System consists of two applications:

* frontend (directory `s4e-web` - angular application)
* backend (directory `s4e-backend` - JAVA Spring Boot application)

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

You will need the test db up and running: `docker-compose up db-test`.
It will be exposed at `localhost:5434`.

Then, run `./mvnw test`.


#### Running integration tests

You will need test db, minio and geoserver for the tests to pass.
Run them by `docker-compose up db-test geoserver minio`.

Minio must be provisioned with `data-packs/minio-data-v1.tar.xz`.

Then, execute `./mvnw verify`.


#### Building and running

To build run `./mvnw package` in directory `s4e-backend`. This will produce a jar in `s4e-backend/target`, which can be
started by running `java -Dspring.profiles.active=development -jar <path to jar>`.

Alternatively, the app can be built and run by issuing `../mvnw spring-boot:run -Dspring-boot.run.profiles=development` in directory `s4e-backend`. 

Both methods will expose the server under `http://localhost:4201`.

During bootstrap Flyway will migrate the schema based on migrations placed in `src/main/resources/db/migration`.

In the `development` mode the DB and GeoServer are seeded by components from `pl.cyfronet.s4e.db.seed`.
They are executed on every startup, but they can also be run by setting appropriate profiles.


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

`run-seed-products`: Runs `SeedProducts`.
See the section Seeding for more info.

`run-seed-places`: Runs `SeedPlaces`.


#### Seeding

The operation of seeding products seeds the DB and synchronizes GeoServer to it.
To disable either phase set `seed.products.seed-db=false` or `seed.products.sync-geoserver=false`, respectively.

If GeoServer is not seeded then the `Product::created` flag is set to true without extra checks.

You can also disable GeoServer workspace reset by setting `seed.products.sync-geoserver.reset-workspace=true`.
This may come handy if the GeoServer workspace has a lot of layers and operation of deleting it would trigger
the `RestTemplate`'s timeout.

There are currently two data sets which the backend is capable of seeding: `minio-data-v1` and `s4e-demo`.
The default is `minio-data-v1`, but another one can be set with the property `seed.products.data-set`.

`minio-data-v1` contains a single day of data for three products.
It uses local minio for S3 storage.

`s4e-demo` is a data set which consists of a month of data of five products.
There is no archive with this data set, but it is available in the CEPH's bucket `s4e-demo`.
After setting the property `seed.products.data-set`, you must modify the GeoServer docker container volume to point to
the `s3.properties` file with cyfronet's CEPH instance url and credentials.
Moreover, you must set a proper bucket by setting `s3.bucket=s4e-demo` so that layers provisioned to the
GeoServer have correct bucket set.
(The provisioning will take a while, so it makes sense to only do it once and in the subsequent runs disable
the products seeding by setting the profile `skip-seed-products`.)

#### Seeding with s4e-demo data

The first thing is to obtain production CEPH credentials, they are for example [here](https://docs.cyfronet.pl/display/FID/Projekty).
Once you have them, create a copy of the file `resources/geoserver/s3.properties` called `s3.properties.prod` in the
same dir and set the `endpoint` (`url`), `user` (`access_key`) and `password` (`secret_key`) accordingly.

Then, update the `docker-compose.yml`.
The service `geoserver` should be wired up to the newly created `s3.properties.prod` file.
```yaml
  geoserver:
    [...]
    volumes:
      - ./resources/geoserver/s3.properties.prod:/opt/geoserver/s3.properties
    [...]
```

If you run with docker, package the application with `./mvnw package -DskipTests` first.

Run required docker-compose services with `docker-compose up -d db minio geoserver`.

Then, run the backend with properties:
```
spring.profiles.active=development
seed.products.data-set=s4e-demo
s3.bucket=s4e-demo
```
If you run with docker, update the backend-development.env (or another env file you have wired up to `s4e-backend`
service in `docker-compose.yml`) by appending:
```
SEED_PRODUCTS_DATASET=s4e-demo
S3_BUCKET=s4e-demo
```

The backend will seed db and GeoServer, which will take around an hour.
It is normal that there are some errors in the backend's logs as there are gaps in the available data.
However, the beginning of the data is quite clean, so you should see progress info when the seeder starts.

If you want to skip seeding db and GeoServer with products on subsequent runs add profile `skip-seed-products`,
either by adding property:
```
spring.profiles.active=development,skip-seed-products
```
or by setting envvar:
```
SPRING_PROFILES_ACTIVE=development,skip-seed-products
```


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
SEED_PRODUCTS_DATASET=s4e-demo
S3_BUCKET=s4e-demo
SEED_PRODUCTS_SYNCGEOSERVER=false
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


#### API docs

We use Swagger to create API documentation.
When you run the backend Swagger-UI is exposed under `http://localhost:4201/swagger-ui.html`.


#### <a id="backend-static"></a> Static images S3 storage

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


## Development & Docker

In the root project directory `docker-compose.yml` is provide for developer's ease of use. 

**NOTE**: As for now application components run via docker-compose have fixed ports, so make sure that there is no application (especially **postgres**) conflicting with them.

This docker-compose recipe can be used to either run whole application for demo purposes, or to run specific parts of the application which can be usefull if for example we would like to develop only frontend part of the application.

In order do run docker-compose following steps must be done (**unless stated otherwise working directory should be project root**):

1. No it's time to get S3 data to the application.
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

7. You're done - application should be available on http://localhost:4200 and have 3 available products.

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
