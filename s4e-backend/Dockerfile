FROM openjdk:8-jdk-alpine

RUN mkdir /app
WORKDIR /app

CMD java -Dspring.datasource.url="${SPRING_DATASOURCE_URL}" -jar app.jar
