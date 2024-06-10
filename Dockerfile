FROM nginx:stable
ADD /build/outputs/bundle/release /app
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app /usr/share/nginx/html