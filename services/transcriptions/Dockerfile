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


# Stage 3: whisper build
FROM ubuntu:22.04 AS whisper-builder
WORKDIR /build

# Установим зависимости для сборки whisper.cpp (glibc, etc.)
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential cmake git pkg-config \
    libopenblas-dev libsndfile1-dev libfftw3-dev \
    && rm -rf /var/lib/apt/lists/*

COPY ./whisper.cpp /build/whisper.cpp

WORKDIR /build/whisper.cpp
RUN mkdir -p build && cmake -B build && cmake --build build -j --config Release


# Stage 4: runtime
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

# Установим ffmpeg из репозитория (делаем это как root)
RUN apt-get update \
 && apt-get install -y --no-install-recommends ffmpeg \
 && rm -rf /var/lib/apt/lists/*

# non-root пользователь
RUN addgroup --system app && adduser --system --ingroup app app && chown -R app:app /app

# Копируем собранный whisper-cli и модели из whisper-builder
COPY --from=whisper-builder /build/whisper.cpp/build/bin/whisper-cli /usr/local/bin/whisper-cli
COPY --from=whisper-builder /build/whisper.cpp/models /usr/local/whisper.cpp/models
# Копируем все скомпилированные .so из build
COPY --from=whisper-builder /build/whisper.cpp/build/ggml/src/*.so* /usr/local/lib/
COPY --from=whisper-builder /build/whisper.cpp/build/src/*.so* /usr/local/lib/

RUN chmod +x /usr/local/bin/whisper-cli && chmod -R a+rX /usr/local/whisper.cpp/models && ldconfig

ENV WHISPER_MODEL_PATH=/usr/local/whisper.cpp/models/ggml-base.bin
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
