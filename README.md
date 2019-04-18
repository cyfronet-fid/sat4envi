# sat4envi

## Requirements

- java `^11`
- mvn `^3.3.9`
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

Then, run `mvn test`.

#### Running integration tests

You will need test db, minio and geoserver for the tests to pass.
Run them by `docker-compose up db-test geoserver minio`.

Minio must be provisioned with `data-packs/minio-data-v1.tar.xz`.

Then, execute `mvn verify`.

#### Building and running

To build run `mvn package` in directory `s4e-backend`. This will produce a jar in `s4e-backend/target`, which can be
started by running `java -Dspring.profiles.active=development -jar <path to jar>`.

Alternatively, the app can be built and run by issuing `mvn spring-boot:run -Dspring.profiles.active=development` in directory `s4e-backend`. 

Both methods will expose the server under `http://localhost:4201`.

#### API docs

We use Swagger to create API documentation.
When you run the backend Swagger-UI is exposed under `http://localhost:4201/swagger-ui.html`.


#### Miscellaneous

To check for dependency updates run `mvn versions:display-dependency-updates` in directory `s4e-backend`.

### s4e-web

Detailed frontend description can be found [here](./s4e-web/README.md)

## Development & Docker

In the root project directory `docker-compose.yml` is provide for developer's ease of use. 

**NOTE**: As for now application components run via docker-compose have fixed ports, so make sure that there is no application (especially **postgres**) conflicting with them.

This docker-compose recipe can be used to either run whole application for demo purposes, or to run specific parts of the application which can be usefull if for example we would like to develop only frontend part of the application.

In order do run docker-compose following steps must be done (**unless stated otherwise working directory should be project root**):

1. Build docker image from https://github.com/cyfronet-fid/docker-geoserver, you can just run following script

   ```bash
   #!/bin/bash
   git clone https://github.com/cyfronet-fid/docker-geoserver
   cd docker-geoserver
   ./build.sh # will ask for sudo
   cd ..
   ```

   You can remove `docker-geoserver` directory afterwards

2. Run `mvn package -DskipTests` in the root of the project, this will build artifacts for `s4e-backend` and `s4e-web`. 
   **NOTICE**: In some cases packaging may fail due to inability to compile some binary dependencies for frontend packages. In that case the easiest solution is to delete `s4e-web/node_moules` directory (`rm -rf s4e-web/node_modules`)

3. Run `docker-compose up`

4. After all services are up in the browser navigate to http://localhost:4200 - web interface should be there

5. No it's time to get S3 data to the application. First make sure that you have `s3cmd` installed:
   **OSX**: `brew install s3cmd`
   **Debian**: `apt-get install s3cmd`

6. Configure `s3cmd` - you can run configuration wizard by running:

   ```bash
   s3cmd --configure
   ```

   You'll need access data which is available [here](https://docs.cyfronet.pl/display/FID/Projekty) (section **sat4envi/CEPH**)

   During configuration there are 4 important params which you must define (you can leave all others to their defaults):

   * **Access Key**
   * **Secret Key**
   * **S3 Endpoint**
   * **DNS-style bucket+hostname:port template** - you should set it to `%(bucket)s.<ceph_url>`

   After configuration you should see summary similar to the one below:

   ```bash
   New settings:
     Access Key: <access_key>
     Secret Key: <secret_key>
     Default Region: US
     S3 Endpoint: <url>
     DNS-style bucket+hostname:port template for accessing a bucket: %(bucket)s.<url>
     Encryption password:
     Path to GPG program: None
     Use HTTPS protocol: True
     HTTP Proxy server name:
     HTTP Proxy server port: 0
   ```

   If running `s3cmd ls` prints output similar to the one below it means it's all good to go

   ```bash
   $ s3cmd ls
   2019-03-08 17:02  s3://data-packs
   2018-06-24 13:44  s3://marta-test
   2018-08-01 11:53  s3://s4e-test-1
   2019-03-06 11:02  s3://s4e-test-2
   ```

7. Download s3 data for minio:

   ```bash
   s3cmd get s3://data-packs/minio-data-v1.tar.xz
   tar -xJf minio-data-v1.tar.xz -C ./tmp
   ```

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




