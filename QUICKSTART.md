# Quick Start Guide

Get the Instagram Clone running on your local machine in 5 minutes!

## Prerequisites Checklist

- [ ] Java 21 installed (`java -version`)
- [ ] Maven 3.9+ installed (`mvn -version`)
- [ ] Docker Desktop running (`docker ps`)
- [ ] At least 8GB RAM available

## Step-by-Step Setup

### 1. Start Infrastructure (2 minutes)

```bash
docker-compose up -d
```

Wait 30 seconds for services to initialize.

### 2. Build Project (1 minute)

```bash
mvn clean install -DskipTests
```

### 3. Start Services (2 minutes)

**Option A: Use the startup script (Recommended)**
```bash
./start-services.sh
```

**Option B: Manual startup (6 separate terminals)**
```bash
# Terminal 1
cd user-service && mvn spring-boot:run

# Terminal 2
cd post-service && mvn spring-boot:run

# Terminal 3
cd media-service && mvn spring-boot:run

# Terminal 4
cd feed-service && mvn spring-boot:run

# Terminal 5
cd api-gateway && mvn spring-boot:run
```

### 4. Test It! (30 seconds)

**Register a user:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "fullName": "Test User"
  }'
```

**Copy the JWT token from the response!**

**Get your profile:**
```bash
curl http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

## Verify Everything Works

Check service health:
```bash
# API Gateway
curl http://localhost:8080/actuator/health

# User Service
curl http://localhost:8081/actuator/health

# Post Service
curl http://localhost:8082/actuator/health
```

All should return `{"status":"UP"}`

## Access Web UIs

- **MinIO**: http://localhost:9001 (minioadmin/minioadmin)
- **Elasticsearch**: http://localhost:9200

## Stop Everything

```bash
./stop-services.sh
```

## Common Issues

### Port Already in Use
```bash
# Kill process on port 8080
lsof -i :8080
kill -9 <PID>
```

### Docker Not Running
```bash
# Start Docker Desktop, then verify
docker ps
```

### Services Not Starting
```bash
# Check logs
tail -f logs/user-service.log
```

### Database Connection Failed
```bash
# Restart PostgreSQL
docker-compose restart postgres-users
```

## What's Next?

1. Read [README.md](README.md) for API documentation
2. Check [ARCHITECTURE.md](ARCHITECTURE.md) for system design
3. Try the API endpoints with Postman or curl
4. Explore the code structure

## Need Help?

- Check logs in `./logs/` directory
- Verify Docker containers: `docker-compose ps`
- Ensure ports 8080-8085 are available
- Check Java version: `java -version` (must be 21+)

---

🚀 Happy coding!
