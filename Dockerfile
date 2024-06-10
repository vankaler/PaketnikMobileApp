FROM nginx:stable
ADD /build/outputs/bundle/release /app
COPY /app /usr/share/nginx/html