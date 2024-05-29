# Stage 1: Download the latest release jar from GitHub
FROM alpine:latest as downloader

WORKDIR /app

RUN apk add --no-cache curl jq

RUN curl -s https://api.github.com/repos/jotrorox/napi/releases/latest \
| jq -r ".assets[] | select(.name | test(\"jar$\")) | .browser_download_url" \
| xargs curl -L -o app.jar

# Stage 2: Run the jar file
FROM sapmachine:21.0.3-jre-ubuntu-focal

WORKDIR /app

COPY --from=downloader /app/app.jar /app/app.jar

CMD ["java", "-jar", "/app/app.jar"]