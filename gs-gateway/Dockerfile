FROM openjdk:17.0.2-jdk
VOLUME /tmp
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/BOOT-INF/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/org /app/org
COPY ${DEPENDENCY}/BOOT-INF/classes /app/BOOT-INF/classes
ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -cp /app org.springframework.boot.loader.JarLauncher ${0} ${@}"]
