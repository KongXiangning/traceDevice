FROM 163583/maven:openjdk11-ali as builder

WORKDIR /usr/local/source/
COPY . /usr/local/source/
RUN mvn clean install -Dmaven.test.skip=true
WORKDIR /application
RUN cp /usr/local/source/target/gps.jar gps.jar
RUN java -Djarmode=layertools -jar gps.jar extract

FROM openjdk:11-jdk-slim as runtime
WORKDIR /usr/local/
RUN export TZ="Asia/Shanghai"
COPY docker-entrypoint.sh ./
RUN chmod +x docker-entrypoint.sh
COPY --from=builder /application/dependencies/ ./
COPY --from=builder /application/snapshot-dependencies/ ./
COPY --from=builder /application/spring-boot-loader/ ./
COPY --from=builder /application/application/ ./
CMD ["./docker-entrypoint.sh"]