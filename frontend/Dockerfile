# ==============================================================================
# MULTI-STAGE DOCKERFILE CHO REACT + VITE FRONTEND
# Stage 1: Build static assets bằng Node.js
# Stage 2: Serve bằng Nginx Alpine nhẹ và bảo mật
# ==============================================================================

# Stage 1: Build
FROM node:20-alpine AS builder

WORKDIR /app

# Tận dụng Docker layer caching cho dependencies
COPY package*.json ./
RUN npm ci

COPY . .

# Build assets sản phẩm
ARG VITE_API_BASE_URL=http://localhost:8080/api
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

RUN npm run build

# Stage 2: Runtime với Nginx
FROM nginx:alpine

# Copy cấu hình SPA Routing & Proxy
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy static assets sau khi build
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=15s --timeout=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://127.0.0.1:80/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
