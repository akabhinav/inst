#!/bin/bash

# Instagram Clone - Service Stop Script

echo "========================================="
echo "Instagram Clone - Stopping Services"
echo "========================================="

echo "Stopping Java services..."

# Kill all Maven spring-boot processes
pkill -f "spring-boot:run"

echo "Java services stopped."

echo ""
echo "Stopping Docker infrastructure..."
docker-compose down

echo ""
echo "========================================="
echo "All services stopped!"
echo "========================================="
echo ""
echo "To remove all data, run: docker-compose down -v"
echo ""
