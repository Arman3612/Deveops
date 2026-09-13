# ============================================================
# Frontend Dockerfile — Multi-stage Build
# Online Examination System (BUG)
# ============================================================

# ── Stage 1: Build ──────────────────────────────────────────
FROM node:18-alpine AS build
WORKDIR /app

# Install dependencies first (layer caching optimization)
COPY package.json package-lock.json ./
RUN npm ci

# Copy source and build production bundle
COPY . .
RUN npm run build

# ── Stage 2: Production (Nginx) ─────────────────────────────
FROM nginx:alpine

LABEL maintainer="BUG Online Exam System"
LABEL description="Frontend for Online Examination System"

# Copy built files to Nginx html directory
COPY --from=build /app/dist /usr/share/nginx/html

# Health check: verify Nginx is serving content
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:80/ || exit 1

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
