# Build stage
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json ./
RUN npm install
COPY . .
RUN npm run build

# Production stage
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/templates/default.conf.template

# Cloud Run sets PORT env var; BACKEND_URL is set during deploy
ENV PORT=8080
ENV BACKEND_URL=http://localhost:8080

EXPOSE 8080

# envsubst replaces ${PORT} and ${BACKEND_URL} in the nginx template at startup
CMD ["sh", "-c", "envsubst '$${PORT} $${BACKEND_URL}' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf && exec nginx -g 'daemon off;'"]
