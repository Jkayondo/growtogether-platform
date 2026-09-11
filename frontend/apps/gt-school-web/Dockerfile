FROM node:22-alpine AS build

WORKDIR /app/frontend

COPY frontend/ ./

RUN npm ci

RUN npm --workspace apps/gt-school-web run build

FROM nginx:1.27-alpine

COPY frontend/apps/gt-school-web/nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=build \
  /app/frontend/apps/gt-school-web/dist \
  /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
