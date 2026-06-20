FROM node:20-alpine AS builder

WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --omit=dev
COPY . .
RUN npm run build

FROM node:20-alpine AS runner
RUN apk add --no-cache curl tzdata dumb-init
WORKDIR /app
COPY --from=builder /app/.output ./

ENV NODE_ENV=production
ENV HOST=0.0.0.0
ENV PORT=3000

# Health check
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=10 \
  CMD curl -f http://localhost:3000/ || exit 1

EXPOSE 3000
CMD ["dumb-init", "node", "server/index.mjs"]