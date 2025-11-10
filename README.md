# Instagram Clone - Java 21 Microservices

A scalable Instagram clone built with Java 21 and Spring Boot, designed to support 50 million daily active users.

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed system design and architecture documentation.

## Technology Stack

- **Backend**: Java 21, Spring Boot 3.2.5
- **Databases**: PostgreSQL, Redis, Cassandra (optional)
- **Message Queue**: Apache Kafka
- **Object Storage**: MinIO (S3-compatible)
- **Search**: Elasticsearch
- **API Gateway**: Spring Cloud Gateway
- **Build Tool**: Maven 3.9+
- **Containerization**: Docker & Docker Compose

## Microservices

1. **API Gateway** (Port 8080) - Entry point for all requests
2. **User Service** (Port 8081) - User management, authentication, follow/unfollow
3. **Post Service** (Port 8082) - Posts, likes, comments
4. **Media Service** (Port 8083) - Image/video upload and storage
5. **Feed Service** (Port 8084) - Timeline generation
6. **Notification Service** (Port 8085) - Real-time notifications

## Prerequisites

- **Java 21** - [Download](https://adoptium.net/)
- **Maven 3.9+** - [Download](https://maven.apache.org/download.cgi)
- **Docker & Docker Compose** - [Download](https://www.docker.com/products/docker-desktop/)
- At least **8GB RAM** and **20GB disk space**

## Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd inst
```

### 2. Start Infrastructure Services

Start all required infrastructure (PostgreSQL, Redis, Kafka, MinIO, Elasticsearch) using Docker Compose:

```bash
docker-compose up -d
```

Wait for all services to be healthy (about 30-60 seconds):

```bash
docker-compose ps
```

You should see all services in "running" state.

### 3. Create MinIO Bucket

Access MinIO Console at http://localhost:9001
- Username: `minioadmin`
- Password: `minioadmin`

Create a bucket named: `instagram-media`

### 4. Build All Services

Build all microservices:

```bash
mvn clean install -DskipTests
```

### 5. Start Microservices

Open separate terminal windows for each service:

**Terminal 1 - User Service:**
```bash
cd user-service
mvn spring-boot:run
```

**Terminal 2 - Post Service:**
```bash
cd post-service
mvn spring-boot:run
```

**Terminal 3 - Media Service:**
```bash
cd media-service
mvn spring-boot:run
```

**Terminal 4 - Feed Service:**
```bash
cd feed-service
mvn spring-boot:run
```

**Terminal 5 - Notification Service:**
```bash
cd notification-service
mvn spring-boot:run
```

**Terminal 6 - API Gateway:**
```bash
cd api-gateway
mvn spring-boot:run
```

Wait for all services to start (look for "Started [ServiceName]Application" in logs).

## API Testing

All requests go through API Gateway at `http://localhost:8080`

### 1. Register a User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123",
    "fullName": "John Doe"
  }'
```

Response:
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "username": "john_doe",
      "email": "john@example.com",
      "fullName": "John Doe"
    }
  }
}
```

Save the JWT token for subsequent requests!

### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "john_doe",
    "password": "password123"
  }'
```

### 3. Get User Profile

```bash
curl -X GET http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Update Profile

```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Doe Updated",
    "bio": "Software Engineer | Coffee Lover",
    "isPrivate": false
  }'
```

### 5. Follow a User

```bash
curl -X POST http://localhost:8080/api/users/2/follow \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 6. Unfollow a User

```bash
curl -X DELETE http://localhost:8080/api/users/2/unfollow \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 7. Get Followers

```bash
curl -X GET "http://localhost:8080/api/users/1/followers?page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 8. Get Following

```bash
curl -X GET "http://localhost:8080/api/users/1/following?page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 9. Search Users

```bash
curl -X GET "http://localhost:8080/api/users/search?q=john&page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 10. Create a Post

```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "caption": "Beautiful sunset! 🌅",
    "mediaUrls": ["https://example.com/image.jpg"],
    "mediaType": "IMAGE",
    "location": "San Francisco, CA"
  }'
```

### 11. Upload Media (Coming Soon)

```bash
curl -X POST http://localhost:8080/api/media/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/image.jpg"
```

### 12. Get Feed (Coming Soon)

```bash
curl -X GET "http://localhost:8080/api/feed/home?page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Testing with Postman

1. Import the collection: `postman_collection.json` (to be created)
2. Set environment variable `JWT_TOKEN` with your token
3. All requests will automatically use the token

## Service Monitoring

### Health Checks

- User Service: http://localhost:8081/actuator/health
- Post Service: http://localhost:8082/actuator/health
- API Gateway: http://localhost:8080/actuator/health

### Infrastructure UIs

- **MinIO Console**: http://localhost:9001 (minioadmin/minioadmin)
- **Elasticsearch**: http://localhost:9200
- **Kafka**: localhost:9092

## Stopping Services

### Stop Microservices
Press `Ctrl+C` in each terminal window

### Stop Infrastructure
```bash
docker-compose down
```

### Stop and Remove All Data
```bash
docker-compose down -v
```

## Project Structure

```
instagram-clone/
├── common/                    # Shared DTOs, exceptions, utilities
├── api-gateway/              # API Gateway service
├── user-service/             # User management service
├── post-service/             # Post management service
├── media-service/            # Media upload/storage service
├── feed-service/             # Feed generation service
├── notification-service/     # Notification service
├── docker-compose.yml        # Infrastructure setup
├── pom.xml                   # Parent Maven configuration
├── ARCHITECTURE.md           # Architecture documentation
└── README.md                 # This file
```

## Development

### Build Single Service

```bash
cd user-service
mvn clean install
```

### Run Tests

```bash
mvn test
```

### Format Code

```bash
mvn spring-javaformat:apply
```

## Scalability Features

✅ **Microservices Architecture** - Independent scaling
✅ **Database Sharding Ready** - Partition by user_id
✅ **Caching (L1 + L2)** - Caffeine + Redis
✅ **Async Processing** - Kafka event streaming
✅ **Connection Pooling** - HikariCP
✅ **Feed Generation** - Hybrid fan-out (write + read)
✅ **Rate Limiting** - Redis-based distributed
✅ **Load Balancing Ready** - Via API Gateway

## Performance Targets

- **API Response Time**: < 100ms (P95)
- **Feed Load**: < 200ms (P95)
- **Media Upload**: < 2s for 5MB
- **Throughput**: 100K requests/second
- **Availability**: 99.95%

## Security Features

- JWT authentication
- BCrypt password hashing
- SQL injection prevention
- XSS protection
- Rate limiting
- Input validation

## Next Steps

### Phase 1 (Current - MVP)
- [x] User registration/login
- [x] Follow/unfollow
- [x] Basic post structure
- [ ] Complete post CRUD
- [ ] Media upload
- [ ] Basic feed

### Phase 2 (Enhanced Features)
- [ ] Comments
- [ ] Real-time notifications
- [ ] User search with Elasticsearch
- [ ] Hashtags
- [ ] Media optimization

### Phase 3 (Scale)
- [ ] Cassandra for feed data
- [ ] Advanced caching strategies
- [ ] Feed ranking algorithm
- [ ] Load testing (JMeter/Gatling)

### Phase 4 (Advanced)
- [ ] Stories
- [ ] Direct messaging
- [ ] Video support
- [ ] Recommendation engine

## Troubleshooting

### Port Already in Use
```bash
# Find process using port 8081
lsof -i :8081
# Kill the process
kill -9 <PID>
```

### Docker Services Not Starting
```bash
# Check logs
docker-compose logs postgres-users
docker-compose logs kafka

# Restart services
docker-compose restart
```

### Database Connection Issues
- Ensure PostgreSQL is running: `docker ps | grep postgres`
- Check connection: `psql -h localhost -U postgres -d instagram_users`

### Out of Memory
- Increase Docker memory: Docker Desktop → Settings → Resources → Memory (8GB+)
- Reduce JVM heap: Add `-Xmx512m` to `MAVEN_OPTS`

## Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

## License

This project is for educational purposes.

## Support

For issues and questions:
- GitHub Issues: [Create Issue]
- Documentation: [ARCHITECTURE.md](ARCHITECTURE.md)

---

Built with ☕ and Java 21
