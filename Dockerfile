## Этап 1: Сборка fat JAR с использованием Gradle
#FROM gradle:8.7-alpine AS builder
#WORKDIR /app
#
## Копируем исходный код и Gradle файлы
#COPY . .
#
## Собираем fat JAR
#RUN gradle shadowJar --no-daemon -Dorg.gradle.jvmargs="-Xmx512m"
#
#
## Этап 2: Финальный образ с Java и готовым JAR
#FROM amazoncorretto:22
#EXPOSE 8080
#
#WORKDIR /app
#
## Копируем fat JAR из предыдущего этапа
#COPY --from=builder /app/build/libs/*.jar app.jar
#
## Запуск приложения
#ENTRYPOINT ["java", "-jar", "app.jar"]



## Stage 1: Cache Gradle dependencies
#FROM gradle:latest AS cache
#RUN mkdir -p /home/gradle/cache_home
#ENV GRADLE_USER_HOME /home/gradle/cache_home
#COPY build.gradle.* gradle.properties /home/gradle/app/
#WORKDIR /home/gradle/app
#RUN gradle clean build -i --stacktrace
#
## Stage 2: Build Application
#FROM gradle:latest AS build
#COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
#COPY . /usr/src/app/
#WORKDIR /usr/src/app
#COPY --chown=gradle:gradle . /home/gradle/src
#WORKDIR /home/gradle/src
## Build the fat JAR, Gradle also supports shadow
## and boot JAR by default.
#RUN gradle buildFatJar --no-daemon
#
## Stage 3: Create the Runtime Image
#FROM amazoncorretto:22 AS runtime
#EXPOSE 8080:8080
#RUN mkdir /app
#COPY --from=build /home/gradle/src/build/libs/*.jar /app/ktor-docker-sample.jar
#ENTRYPOINT ["java","-jar","/app/ktor-docker-sample.jar"]

FROM amazoncorretto:22
EXPOSE 8090:8090
WORKDIR /app
COPY build/libs/fat.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]