FROM cirrusci/android-sdk:29

LABEL maintainer="Nejc Petkoski"

WORKDIR /app

COPY . /app

RUN sdkmanager --install "cmdline-tools;latest" "platform-tools" "platforms;android-29" "build-tools;29.0.3"

RUN chmod +x gradlew

RUN ./gradlew build

EXPOSE 3000

CMD ["./gradlew", "assembleDebug"]
