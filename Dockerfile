# build
FROM maven:3-eclipse-temurin-21 AS build
WORKDIR /build
COPY ../pom.xml ./
COPY ../src ./src
RUN --mount=type=cache,target=/root/.m2 mvn clean install -Dmaven.test.skip=true

#run
FROM eclipse-temurin:21-jre-jammy AS run
# Create group and user (don't fail if they already exist)
RUN groupadd -f appuser && if [ $(id -u appuser > /dev/null 2>&1;echo $?) -ne 0 ]; then adduser --system --group appuser; fi
USER appuser
WORKDIR /home/appuser
COPY --from=build --chown=appuser:appuser build/target/*.jar /home/appuser/app.jar
CMD java -agentlib:jdwp=transport=dt_socket,server=y,address=*:18070,suspend=n -jar /home/appuser/app.jar