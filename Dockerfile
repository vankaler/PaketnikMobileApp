FROM bash as build
ADD /app/build/outputs/bundle/release /temp

FROM nginx:stable
COPY --from=build /temp /usr/share/nginx/html