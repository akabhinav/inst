#!/bin/bash

# Instagram Clone - Service Startup Script
# This script helps you start all microservices easily

echo "========================================="
echo "Instagram Clone - Starting Services"
echo "========================================="

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check if a port is in use
check_port() {
    if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null ; then
        echo -e "${YELLOW}Warning: Port $1 is already in use${NC}"
        return 1
    else
        return 0
    fi
}

# Check Java version
echo "Checking Java version..."
java -version 2>&1 | head -1
if [ $? -ne 0 ]; then
    echo "Error: Java is not installed or not in PATH"
    exit 1
fi

# Check if Docker is running
echo "Checking Docker..."
if ! docker info > /dev/null 2>&1; then
    echo "Error: Docker is not running. Please start Docker first."
    exit 1
fi

echo ""
echo "Step 1: Starting Infrastructure Services..."
echo "-----------------------------------------"
docker-compose up -d

echo "Waiting for infrastructure to be ready (30 seconds)..."
sleep 30

echo ""
echo "Step 2: Building Services..."
echo "-----------------------------------------"
mvn clean install -DskipTests

echo ""
echo "Step 3: Starting Microservices..."
echo "-----------------------------------------"
echo "Services will be started in background."
echo "Logs will be saved to respective .log files"

# Create logs directory
mkdir -p logs

# Start services in background
echo "Starting User Service (Port 8081)..."
cd user-service && mvn spring-boot:run > ../logs/user-service.log 2>&1 &
USER_PID=$!
cd ..

sleep 10

echo "Starting Post Service (Port 8082)..."
cd post-service && mvn spring-boot:run > ../logs/post-service.log 2>&1 &
POST_PID=$!
cd ..

sleep 10

echo "Starting Media Service (Port 8083)..."
cd media-service && mvn spring-boot:run > ../logs/media-service.log 2>&1 &
MEDIA_PID=$!
cd ..

sleep 10

echo "Starting Feed Service (Port 8084)..."
cd feed-service && mvn spring-boot:run > ../logs/feed-service.log 2>&1 &
FEED_PID=$!
cd ..

sleep 10

echo "Starting API Gateway (Port 8080)..."
cd api-gateway && mvn spring-boot:run > ../logs/api-gateway.log 2>&1 &
GATEWAY_PID=$!
cd ..

echo ""
echo "========================================="
echo -e "${GREEN}All services are starting!${NC}"
echo "========================================="
echo ""
echo "Service Status:"
echo "  User Service:    http://localhost:8081/actuator/health"
echo "  Post Service:    http://localhost:8082/actuator/health"
echo "  Media Service:   http://localhost:8083"
echo "  Feed Service:    http://localhost:8084"
echo "  API Gateway:     http://localhost:8080"
echo ""
echo "Infrastructure:"
echo "  MinIO Console:   http://localhost:9001 (minioadmin/minioadmin)"
echo "  Elasticsearch:   http://localhost:9200"
echo ""
echo "Logs are saved in: ./logs/"
echo ""
echo "To view logs in real-time:"
echo "  tail -f logs/user-service.log"
echo "  tail -f logs/api-gateway.log"
echo ""
echo "To stop all services, run: ./stop-services.sh"
echo ""
