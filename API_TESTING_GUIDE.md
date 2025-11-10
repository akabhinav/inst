# API Testing Guide - Instagram Clone

Complete guide to test all API endpoints. All requests go through API Gateway at `http://localhost:8080`

## Prerequisites

1. Start infrastructure: `docker-compose up -d`
2. Start all services (see README.md)
3. Wait for all services to be ready (~2 minutes)

## Test Flow

### 1. User Registration & Authentication

**Register User 1:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "email": "alice@example.com",
    "password": "password123",
    "fullName": "Alice Johnson"
  }'
```

**Save the JWT token from response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": { "id": 1, "username": "alice", ... }
  }
}
```

**Register User 2:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "bob",
    "email": "bob@example.com",
    "password": "password123",
    "fullName": "Bob Smith"
  }'
```

**Login (if needed):**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "alice",
    "password": "password123"
  }'
```

---

### 2. User Profile Management

**Get User Profile:**
```bash
# Replace {TOKEN} with your JWT token
curl -X GET http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer {TOKEN}"
```

**Update Profile:**
```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer {TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Alice Johnson Updated",
    "bio": "Software Engineer | Coffee Lover ☕",
    "isPrivate": false
  }'
```

**Search Users:**
```bash
curl -X GET "http://localhost:8080/api/users/search?q=alice&page=0&size=10" \
  -H "Authorization: Bearer {TOKEN}"
```

---

### 3. Follow/Unfollow

**Alice follows Bob (User 1 follows User 2):**
```bash
curl -X POST http://localhost:8080/api/users/2/follow \
  -H "Authorization: Bearer {ALICE_TOKEN}"
```

**Get Alice's Following List:**
```bash
curl -X GET "http://localhost:8080/api/users/1/following?page=0&size=20" \
  -H "Authorization: Bearer {ALICE_TOKEN}"
```

**Get Bob's Followers:**
```bash
curl -X GET "http://localhost:8080/api/users/2/followers?page=0&size=20" \
  -H "Authorization: Bearer {BOB_TOKEN}"
```

**Unfollow:**
```bash
curl -X DELETE http://localhost:8080/api/users/2/unfollow \
  -H "Authorization: Bearer {ALICE_TOKEN}"
```

---

### 4. Posts Management

**Create a Post:**
```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Authorization: Bearer {TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "caption": "Beautiful sunset at the beach! 🌅 #sunset #beach #nature",
    "mediaUrls": ["https://example.com/images/sunset1.jpg"],
    "mediaType": "IMAGE",
    "location": "Santa Monica Beach, CA"
  }'
```

**Get Post by ID:**
```bash
curl -X GET http://localhost:8080/api/posts/1 \
  -H "Authorization: Bearer {TOKEN}"
```

**Get User's Posts:**
```bash
curl -X GET "http://localhost:8080/api/posts/user/1?page=0&size=20" \
  -H "Authorization: Bearer {TOKEN}"
```

**Get Explore Feed:**
```bash
curl -X GET "http://localhost:8080/api/posts/explore?page=0&size=20" \
  -H "Authorization: Bearer {TOKEN}"
```

**Delete Post:**
```bash
curl -X DELETE http://localhost:8080/api/posts/1 \
  -H "Authorization: Bearer {TOKEN}"
```

---

### 5. Likes

**Like a Post:**
```bash
curl -X POST http://localhost:8080/api/posts/1/like \
  -H "Authorization: Bearer {TOKEN}"
```

**Unlike a Post:**
```bash
curl -X DELETE http://localhost:8080/api/posts/1/unlike \
  -H "Authorization: Bearer {TOKEN}"
```

---

### 6. Comments

**Add Comment to Post:**
```bash
curl -X POST http://localhost:8080/api/posts/1/comments \
  -H "Authorization: Bearer {TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Amazing photo! Love the colors 😍"
  }'
```

**Reply to Comment:**
```bash
curl -X POST http://localhost:8080/api/posts/1/comments \
  -H "Authorization: Bearer {TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Thank you so much!",
    "parentCommentId": 1
  }'
```

**Get Post Comments:**
```bash
curl -X GET "http://localhost:8080/api/posts/1/comments?page=0&size=20" \
  -H "Authorization: Bearer {TOKEN}"
```

**Get Comment Replies:**
```bash
curl -X GET "http://localhost:8080/api/posts/comments/1/replies?page=0&size=20" \
  -H "Authorization: Bearer {TOKEN}"
```

**Delete Comment:**
```bash
curl -X DELETE http://localhost:8080/api/posts/comments/1 \
  -H "Authorization: Bearer {TOKEN}"
```

---

### 7. Media Upload

**Upload Image:**
```bash
curl -X POST http://localhost:8080/api/media/upload/image \
  -H "Authorization: Bearer {TOKEN}" \
  -F "file=@/path/to/your/image.jpg" \
  -F "userId=1"
```

Response will include:
```json
{
  "success": true,
  "data": {
    "fileName": "users/1/uuid.jpg",
    "fileUrl": "http://localhost:9000/instagram-media/users/1/uuid.jpg",
    "thumbnailUrl": "http://localhost:9000/instagram-media/users/1/uuid_thumb.jpg",
    "fileType": "image/jpeg",
    "fileSize": 1234567
  }
}
```

**Upload Video:**
```bash
curl -X POST http://localhost:8080/api/media/upload/video \
  -H "Authorization: Bearer {TOKEN}" \
  -F "file=@/path/to/your/video.mp4" \
  -F "userId=1"
```

**Delete Media:**
```bash
curl -X DELETE http://localhost:8080/api/media/{fileName} \
  -H "Authorization: Bearer {TOKEN}"
```

---

### 8. Feed

**Get Home Feed:**
```bash
curl -X GET "http://localhost:8080/api/feed/home?userId=1&page=0&size=20" \
  -H "Authorization: Bearer {TOKEN}"
```

**Get User Timeline:**
```bash
curl -X GET "http://localhost:8080/api/feed/user/1?page=0&size=20" \
  -H "Authorization: Bearer {TOKEN}"
```

---

## Complete Test Scenario

Here's a complete flow to test the entire system:

```bash
# 1. Register two users
USER1_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@test.com","password":"password123","fullName":"Alice"}')

USER2_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"bob","email":"bob@test.com","password":"password123","fullName":"Bob"}')

# Extract tokens (requires jq)
TOKEN1=$(echo $USER1_RESPONSE | jq -r '.data.token')
TOKEN2=$(echo $USER2_RESPONSE | jq -r '.data.token')

echo "Alice Token: $TOKEN1"
echo "Bob Token: $TOKEN2"

# 2. Alice creates a post
curl -X POST http://localhost:8080/api/posts \
  -H "Authorization: Bearer $TOKEN1" \
  -H "Content-Type: application/json" \
  -d '{"caption":"My first post!","mediaUrls":["https://example.com/photo.jpg"],"mediaType":"IMAGE"}'

# 3. Bob follows Alice
curl -X POST http://localhost:8080/api/users/1/follow \
  -H "Authorization: Bearer $TOKEN2"

# 4. Bob likes Alice's post
curl -X POST http://localhost:8080/api/posts/1/like \
  -H "Authorization: Bearer $TOKEN2"

# 5. Bob comments on Alice's post
curl -X POST http://localhost:8080/api/posts/1/comments \
  -H "Authorization: Bearer $TOKEN2" \
  -H "Content-Type: application/json" \
  -d '{"content":"Great post!"}'

# 6. Get Bob's home feed (should include Alice's post)
curl -X GET "http://localhost:8080/api/feed/home?userId=2&page=0&size=20" \
  -H "Authorization: Bearer $TOKEN2"

# 7. Get Alice's followers (should include Bob)
curl -X GET "http://localhost:8080/api/users/1/followers?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN1"
```

---

## Testing with Postman

1. Import the collection (if provided)
2. Create environment variables:
   - `BASE_URL`: http://localhost:8080
   - `TOKEN`: Your JWT token
   - `USER_ID`: Your user ID

3. Use `{{BASE_URL}}` and `{{TOKEN}}` in requests

---

## Expected Response Codes

- `200 OK` - Successful GET/PUT/DELETE
- `201 Created` - Successful POST (creation)
- `400 Bad Request` - Validation error or bad input
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - No permission for this action
- `404 Not Found` - Resource doesn't exist
- `500 Internal Server Error` - Server error

---

## Common Issues & Solutions

### "Unauthorized" Error
- Token expired (24 hours) - login again
- Wrong token - check you're using the correct user's token
- Token format - ensure "Bearer {token}" format

### "Post not found"
- Post may be deleted or archived
- Check post ID is correct
- Post may belong to different database (if you restarted services)

### Cannot upload media
- Check MinIO is running: `docker ps | grep minio`
- Verify bucket exists: http://localhost:9001
- Check file size limits (10MB images, 100MB videos)

### Feed is empty
- Make sure you're following users who have posts
- Posts were created BEFORE you followed (feed is generated on post creation)
- Try the explore endpoint: `/api/posts/explore`

---

## Monitoring

**Check Service Health:**
```bash
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Post Service
curl http://localhost:8083/actuator/health  # Media Service (no actuator)
curl http://localhost:8084/actuator/health  # Feed Service (no actuator)
```

**Check Logs:**
```bash
tail -f logs/user-service.log
tail -f logs/post-service.log
tail -f logs/media-service.log
tail -f logs/feed-service.log
```

**Check Kafka Topics:**
```bash
docker exec -it instagram-kafka kafka-topics --list --bootstrap-server localhost:9092
```

**Check Redis Keys:**
```bash
docker exec -it instagram-redis redis-cli
> KEYS *
> GET feed:1
```

---

## Performance Testing

**Load Test with Apache Bench:**
```bash
# Test user registration
ab -n 1000 -c 10 -p register.json -T application/json \
   http://localhost:8080/api/auth/register

# Test post retrieval (with auth header)
ab -n 1000 -c 50 -H "Authorization: Bearer {TOKEN}" \
   http://localhost:8080/api/posts/1
```

**Create register.json:**
```json
{"username":"testuser","email":"test@test.com","password":"password123","fullName":"Test User"}
```

---

Happy Testing! 🚀
