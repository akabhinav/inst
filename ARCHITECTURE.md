# Instagram Clone - System Architecture

## Overview
A scalable Instagram clone designed to support 50 million daily active users, built with Java 21 and microservices architecture.

## Technology Stack

### Backend
- **Java 21** (Latest LTS with Virtual Threads)
- **Spring Boot 3.2.x** (Framework)
- **Spring Cloud Gateway** (API Gateway)
- **Spring Security** (Authentication/Authorization)
- **Spring Data JPA** (PostgreSQL)
- **Spring Data Redis** (Caching)
- **Spring Kafka** (Event Streaming)
- **Spring WebSocket** (Real-time features)

### Databases & Storage
- **PostgreSQL** - User data, relationships, post metadata
- **Redis** - Caching, sessions, real-time data
- **Apache Cassandra** - Feed/timeline data (high write throughput)
- **MinIO** - Object storage for images/videos (S3-compatible)
- **Elasticsearch** - Search (users, hashtags, posts)

### Message Queue
- **Apache Kafka** - Event streaming, async processing

### Infrastructure (Local Development)
- **Docker & Docker Compose** - Containerization
- **Nginx** - Reverse proxy/load balancer

## Microservices Architecture

### 1. API Gateway Service (Port 8080)
- Route requests to appropriate services
- Authentication/Authorization
- Rate limiting
- Request/response transformation

### 2. User Service (Port 8081)
- User registration/login
- Profile management
- Follow/unfollow
- User search
- JWT token generation

### 3. Post Service (Port 8082)
- Create/delete posts
- Like/unlike posts
- Comments on posts
- Post retrieval
- Hashtag processing

### 4. Media Service (Port 8083)
- Image/video upload
- Image processing (resize, compress)
- Storage management (MinIO/S3)
- CDN integration ready

### 5. Feed Service (Port 8084)
- Home feed generation
- User timeline
- Feed ranking algorithm
- Pull/Push hybrid model

### 6. Notification Service (Port 8085)
- Real-time notifications
- WebSocket connections
- Push notifications ready
- Email notifications

### 7. Search Service (Port 8086)
- User search
- Hashtag search
- Post search
- Elasticsearch integration

## Database Schema Design

### PostgreSQL - Core Data

```sql
-- Users Table
users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(50) UNIQUE,
  email VARCHAR(255) UNIQUE,
  password_hash VARCHAR(255),
  full_name VARCHAR(255),
  bio TEXT,
  profile_image_url VARCHAR(500),
  followers_count INT DEFAULT 0,
  following_count INT DEFAULT 0,
  posts_count INT DEFAULT 0,
  is_verified BOOLEAN DEFAULT FALSE,
  is_private BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
)

-- Follows Table
follows (
  id BIGSERIAL PRIMARY KEY,
  follower_id BIGINT REFERENCES users(id),
  following_id BIGINT REFERENCES users(id),
  created_at TIMESTAMP,
  UNIQUE(follower_id, following_id)
)

-- Posts Table
posts (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT REFERENCES users(id),
  caption TEXT,
  media_urls TEXT[], -- Array of image/video URLs
  media_type VARCHAR(20), -- IMAGE, VIDEO, CAROUSEL
  likes_count INT DEFAULT 0,
  comments_count INT DEFAULT 0,
  location VARCHAR(255),
  is_archived BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
)

-- Likes Table
likes (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT REFERENCES users(id),
  post_id BIGINT REFERENCES posts(id),
  created_at TIMESTAMP,
  UNIQUE(user_id, post_id)
)

-- Comments Table
comments (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT REFERENCES users(id),
  post_id BIGINT REFERENCES posts(id),
  parent_comment_id BIGINT REFERENCES comments(id),
  content TEXT,
  likes_count INT DEFAULT 0,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
)

-- Hashtags Table
hashtags (
  id BIGSERIAL PRIMARY KEY,
  tag VARCHAR(255) UNIQUE,
  posts_count INT DEFAULT 0,
  created_at TIMESTAMP
)

-- Post_Hashtags Table (Many-to-Many)
post_hashtags (
  post_id BIGINT REFERENCES posts(id),
  hashtag_id BIGINT REFERENCES hashtags(id),
  PRIMARY KEY(post_id, hashtag_id)
)
```

### Cassandra - Feed Data (Time-series, High Write)

```cql
-- User Feed (Home Timeline)
CREATE TABLE user_feed (
  user_id BIGINT,
  post_id BIGINT,
  author_id BIGINT,
  created_at TIMESTAMP,
  PRIMARY KEY (user_id, created_at, post_id)
) WITH CLUSTERING ORDER BY (created_at DESC);

-- User Posts Timeline
CREATE TABLE user_timeline (
  user_id BIGINT,
  post_id BIGINT,
  created_at TIMESTAMP,
  PRIMARY KEY (user_id, created_at, post_id)
) WITH CLUSTERING ORDER BY (created_at DESC);
```

### Redis - Caching & Real-time

```
-- Session Management
session:{user_id} -> JWT token, user data

-- Feed Cache
feed:{user_id} -> List of post IDs (sorted by time)

-- Post Cache
post:{post_id} -> Post JSON data

-- User Cache
user:{user_id} -> User JSON data

-- Online Users
online_users -> Set of user IDs

-- Notifications Queue
notifications:{user_id} -> List of notifications
```

## API Endpoints

### User Service
```
POST   /api/auth/register
POST   /api/auth/login
POST   /api/auth/logout
GET    /api/users/{id}
PUT    /api/users/{id}
GET    /api/users/{id}/followers
GET    /api/users/{id}/following
POST   /api/users/{id}/follow
DELETE /api/users/{id}/unfollow
GET    /api/users/search?q={query}
```

### Post Service
```
POST   /api/posts
GET    /api/posts/{id}
DELETE /api/posts/{id}
PUT    /api/posts/{id}
POST   /api/posts/{id}/like
DELETE /api/posts/{id}/unlike
GET    /api/posts/{id}/likes
POST   /api/posts/{id}/comments
GET    /api/posts/{id}/comments
GET    /api/users/{id}/posts
```

### Feed Service
```
GET    /api/feed/home?page={n}
GET    /api/feed/user/{userId}?page={n}
GET    /api/feed/explore?page={n}
```

### Media Service
```
POST   /api/media/upload
GET    /api/media/{id}
DELETE /api/media/{id}
```

### Notification Service
```
GET    /api/notifications
PUT    /api/notifications/{id}/read
WS     /ws/notifications (WebSocket)
```

## Scalability Features

### 1. Feed Generation Strategy (Fan-out on Write + Fan-out on Read Hybrid)
- **Fan-out on Write**: For users with < 1000 followers (pre-generate feeds)
- **Fan-out on Read**: For celebrity users (generate on demand)
- Feed caching with Redis (TTL: 5 minutes)
- Pagination with cursor-based approach

### 2. Caching Strategy
- **L1 Cache**: Application-level (Caffeine)
- **L2 Cache**: Redis (distributed)
- Cache-aside pattern
- TTL-based expiration

### 3. Database Optimization
- Read replicas for PostgreSQL
- Connection pooling (HikariCP)
- Database sharding ready (by user_id)
- Indexes on frequently queried columns

### 4. Async Processing with Kafka
- Post creation → Feed generation (async)
- Like/Comment → Notification (async)
- Media upload → Image processing (async)
- Analytics events

### 5. Rate Limiting
- Token bucket algorithm
- Redis-based distributed rate limiting
- Per user, per IP, per endpoint

### 6. Media Optimization
- Image compression (JPEG 80% quality)
- Multiple resolutions (thumbnail, medium, original)
- Lazy loading
- CDN integration ready

### 7. Search Optimization
- Elasticsearch for full-text search
- Autocomplete with n-grams
- Search result ranking

### 8. Real-time Features
- WebSocket for notifications
- Redis Pub/Sub for broadcasting
- Connection pooling

## Performance Targets

- **API Response Time**: < 100ms (P95)
- **Feed Load Time**: < 200ms (P95)
- **Media Upload**: < 2s for 5MB image
- **Search**: < 50ms (P95)
- **Throughput**: 100,000 requests/second
- **Availability**: 99.95% uptime

## Security

- JWT-based authentication
- BCrypt password hashing
- SQL injection prevention (Prepared statements)
- XSS protection
- CSRF protection
- Rate limiting
- Input validation
- HTTPS only (production)

## Monitoring & Observability

- **Metrics**: Micrometer + Prometheus
- **Logging**: Logback + ELK Stack ready
- **Tracing**: Spring Cloud Sleuth + Zipkin ready
- **Health Checks**: Spring Actuator

## Deployment Strategy

### Local Development (Docker Compose)
- All services on single machine
- Reduced resource allocation
- Development-friendly configuration

### Production (Kubernetes - Future)
- Multi-region deployment
- Auto-scaling (HPA)
- Service mesh (Istio)
- Blue-green deployment

## Development Phases

### Phase 1: Core Features (MVP)
- User registration/login
- Create/view posts
- Follow/unfollow
- Basic feed
- Like posts

### Phase 2: Enhanced Features
- Comments
- Notifications
- Search
- Media optimization
- Hashtags

### Phase 3: Scale & Performance
- Cassandra integration
- Advanced caching
- Feed optimization
- Load testing

### Phase 4: Advanced Features
- Stories
- Direct messaging
- Video support
- Recommendations
