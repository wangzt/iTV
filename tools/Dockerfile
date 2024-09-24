FROM --platform=$TARGETPLATFORM alpine:3.14

RUN apk add --no-cache tzdata ca-certificates nginx && rm -rf /var/cache/apk/*

COPY public /var/lib/nginx/html
COPY etc/nginx/default.conf /etc/nginx/http.d/default.conf

EXPOSE 80

ENV TZ Asia/Shanghai

CMD ["sh", "-c", "nginx -g 'daemon off;'"]
