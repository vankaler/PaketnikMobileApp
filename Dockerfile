FROM nginx:stable
ADD /app/build/outputs/bundle/release /temp
COPY /temp /usr/share/nginx/html