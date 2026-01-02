FROM eclipse-temurin:17-jdk

ENV ANDROID_SDK_ROOT=/sdk
ENV PATH="$PATH:/sdk/cmdline-tools/latest/bin:/sdk/platform-tools:/sdk/build-tools/34.0.0"

RUN apt-get update && apt-get install -y wget unzip && rm -rf /var/lib/apt/lists/*

# Android commandline-tools
ARG CMDLINE_TOOLS_URL=https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
RUN mkdir -p /sdk/cmdline-tools && \
    wget -q ${CMDLINE_TOOLS_URL} -O /tmp/tools.zip && \
    unzip -q /tmp/tools.zip -d /sdk/cmdline-tools && \
    mv /sdk/cmdline-tools/cmdline-tools /sdk/cmdline-tools/latest && \
    rm /tmp/tools.zip

# Lisanslar ve paketler
RUN yes | sdkmanager --licenses
RUN sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

WORKDIR /app
COPY . /app

ENV GRADLE_USER_HOME=/app/.gradle
RUN chmod +x ./gradlew || true

CMD ["./gradlew", "assembleDebug"]
