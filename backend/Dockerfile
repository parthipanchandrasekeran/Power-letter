FROM node:20-slim

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install production dependencies only
RUN npm ci --only=production

# Copy source
COPY server.js ./

# Run as non-root user
USER node

# Expose port
EXPOSE 8080

# Start server
CMD ["node", "server.js"]
