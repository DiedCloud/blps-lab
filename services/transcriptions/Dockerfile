# syntax=docker/dockerfile:1.4

# Stage 1: build with Gradle
FROM gradle:8.13-jdk17 AS builder
WORKDIR /home/gradle/project

COPY --chown=gradle:gradle gradle ./gradle
COPY --chown=gradle:gradle gradlew ./
COPY --chown=gradle:gradle settings.gradle.kts ./
COPY --chown=gradle:gradle shared/build.gradle.kts shared/
COPY --chown=gradle:gradle services/transcriptions/build.gradle.kts services/api/

RUN --mount=type=cache,id=gradle-cache-blps-transcriptions,target=/home/gradle/.gradle \
    ./gradlew :shared:dependencies :transcriptions:dependencies --no-daemon

COPY --chown=gradle:gradle . .

RUN --mount=type=cache,id=gradle-cache-blps-transcriptions,target=/home/gradle/.gradle \
    ./gradlew :transcriptions:bootJar --no-daemon -x test


# Stage 2: layered extraction
FROM eclipse-temurin:17-jdk-jammy AS extractor
WORKDIR /extract
COPY --from=builder /home/gradle/project/services/transcriptions/build/libs/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# Stage 3: deps
FROM eclipse-temurin:17-jdk-jammy AS deps
RUN apt-get update \
 && apt-get install -y --no-install-recommends libgomp1 xz-utils

RUN wget https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-linux64-gpl.tar.xz \
    && tar -xf ffmpeg-master-latest-linux64-gpl.tar.xz \
    && mv ffmpeg-master-latest-linux64-gpl/bin/ffmpeg /usr/local/bin/

# Stage 4: runtime
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

COPY --from=deps /usr/local/bin/ffmpeg /usr/local/bin/
COPY --from=deps /usr/lib/x86_64-linux-gnu/libgomp* /usr/lib/x86_64-linux-gnu/

# non-root пользователь
RUN addgroup --system app && adduser --system --ingroup app app && chown -R app:app /app

ARG WHISPER_MODEL_TYPE

ENV WHISPER_MODEL_TYPE=${WHISPER_MODEL_TYPE}
ENV PATH=${PATH}:/usr/local/bin

# log каталог
RUN mkdir -p /var/atomikos/logs && chown -R app:app /var/atomikos

# Явно задаём Atomikos log dir и уникальное имя TM через переменные среды
ENV JAVA_TOOL_OPTIONS="-Dcom.atomikos.icatch.log_base_dir=/var/atomikos/logs \
 -Dcom.atomikos.icatch.log_base_name=transcriptions_tmlog \
 -Dcom.atomikos.icatch.tm_unique_name=transcriptions-tm"

USER app

# Копируем слои jar из stage 2
COPY --from=extractor /extract/dependencies/ ./
COPY --from=extractor /extract/snapshot-dependencies/ ./
COPY --from=extractor /extract/spring-boot-loader/ ./
COPY --from=extractor /extract/application/ ./

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:MinRAMPercentage=20.0", "-Djava.security.egd=file:/dev/./urandom", "org.springframework.boot.loader.launch.JarLauncher"]
