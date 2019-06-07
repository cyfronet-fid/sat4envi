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

Alternatively, the app can be built and run by issuing `./mvnw spring-boot:run -Dspring.profiles.active=development` in directory `s4e-backend`. 

Both methods will expose the server under `http://localhost:4201`.

In the `development` mode the DB and GeoServer are seeded by `SeedDevelopment`.
It is executed on every startup.


#### Places CSV generation

In case it is required to regenerate `places.csv` use `PlacesXlsxToCsv` tool.

First, obtain the current `miejscowosci.xlsx` file from [PRNG](http://www.gugik.gov.pl/pzgik/dane-bez-oplat/dane-z-panstwowego-rejestru-nazw-geograficznych-prng).
I assume you have built the jar file, so, being in the root directory, run
`java -cp s4e-backend/target/<jar> pl.cyfronet.s4e.tool.PlacesXlsxToCsv <path to miejscowosci.xlsx> s4e-backend/src/main/resources/db/places.csv`.
The generated file should overwrite the pre-existing one, and you can commit the resulting csv into the repo.


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

Set its policy to download: `mc policy download local-docker/static`.


#### Miscellaneous

To check for dependency updates run `../mvnw versions:display-dependency-updates` in directory `s4e-backend`.


### s4e-web

Detailed frontend description can be found [here](./s4e-web/README.md)


## Development & Docker

In the root project directory `docker-compose.yml` is provide for developer's ease of use. 

**NOTE**: As for now application components run via docker-compose have fixed ports, so make sure that there is no application (especially **postgres**) conflicting with them.

This docker-compose recipe can be used to either run whole application for demo purposes, or to run specific parts of the application which can be usefull if for example we would like to develop only frontend part of the application.

In order do run docker-compose following steps must be done (**unless stated otherwise working directory should be project root**):

1. Run `./mvnw package -DskipTests` in the root of the project, this will build artifacts for `s4e-backend` and `s4e-web`. 
   **NOTICE**: In some cases packaging may fail due to inability to compile some binary dependencies for frontend packages. In that case the easiest solution is to delete `s4e-web/node_moules` directory (`rm -rf s4e-web/node_modules`)

2. Run `docker-compose up`

3. After all services are up in the browser navigate to http://localhost:4200 - web interface should be there

4. No it's time to get S3 data to the application.
   First make sure that you have [minio client](https://github.com/minio/mc) (`mc`) installed.
   
5. Configure `mc`.

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

6. Download s3 data for minio:

   ```bash
   mc cp cyfronet-ceph/data-packs/minio-data-v1.tar.xz .
   tar -xJf minio-data-v1.tar.xz -C ./tmp
   ```
   
7. Create a bucket with static application content as well as shown [here](#backend-static).

8. You're done - application should be available on http://localhost:4200 and have 3 available products

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




