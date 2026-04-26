FROM node:18-alpine AS builder
WORKDIR /app
COPY RoomAppoint.elementui/package*.json ./
RUN npm ci
COPY RoomAppoint.elementui/ ./
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY deploy/nginx/default.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
