# Swagger/OpenAPI Documentation Guide

## Overview

This Spring Boot application now includes **Swagger UI** (powered by SpringDoc OpenAPI) for interactive API documentation and testing.

## 🔗 Access URLs

Once the application is running, you can access:

### Swagger UI (Interactive Documentation)
```
http://localhost:8080/swagger-ui.html
```
or
```
http://localhost:8080/swagger-ui/index.html
```

### OpenAPI JSON Schema
```
http://localhost:8080/v3/api-docs
```

### OpenAPI YAML Schema
```
http://localhost:8080/v3/api-docs.yaml
```

---

## 🚀 Quick Start

### 1. Start the Application

```bash
mvn clean install
mvn spring-boot:run
```

### 2. Open Swagger UI

Navigate to: **http://localhost:8080/swagger-ui.html**

---

## 📚 API Documentation Structure

### Tags/Controllers

1. **Authentication** - User registration and login endpoints
   - `POST /api/auth/register` - Register a new user
   - `POST /api/auth/login` - Login and get JWT token

2. **User** - User profile management (requires authentication)
   - `GET /api/users/me` - Get current user profile

3. **Admin** - Administrative endpoints (requires ROLE_ADMIN)
   - `GET /api/admin/users` - Get all users

---

## 🔐 Testing Authenticated Endpoints

### Step 1: Register a User

1. Expand the **Authentication** section
2. Click on `POST /api/auth/register`
3. Click **Try it out**
4. Enter the request body:
   ```json
   {
     "username": "testuser",
     "password": "password123",
     "role": "ROLE_USER"
   }
   ```
5. Click **Execute**

### Step 2: Login

1. Click on `POST /api/auth/login`
2. Click **Try it out**
3. Enter the credentials:
   ```json
   {
     "username": "testuser",
     "password": "password123"
   }
   ```
4. Click **Execute**
5. **Copy the JWT token** from the response

### Step 3: Authorize with JWT Token

1. Click the **🔓 Authorize** button at the top right of Swagger UI
2. Paste your JWT token (without "Bearer " prefix)
3. Click **Authorize**
4. Click **Close**

### Step 4: Test Protected Endpoints

Now you can test authenticated endpoints:

1. Expand **User** section
2. Click `GET /api/users/me`
3. Click **Try it out**
4. Click **Execute**

You should see your user information returned!

---

## 🎯 Testing Scenarios

### Scenario 1: Regular User Access

```json
// 1. Register as regular user
POST /api/auth/register
{
  "username": "john_doe",
  "password": "password123",
  "role": "ROLE_USER"
}

// 2. Login
POST /api/auth/login
{
  "username": "john_doe",
  "password": "password123"
}

// 3. Copy token and authorize

// 4. Test user endpoint (✅ Should work)
GET /api/users/me

// 5. Try admin endpoint (❌ Should fail with 403 Forbidden)
GET /api/admin/users
```

### Scenario 2: Admin User Access

```json
// 1. Register as admin
POST /api/auth/register
{
  "username": "admin",
  "password": "admin123",
  "role": "ROLE_ADMIN"
}

// 2. Login
POST /api/auth/login
{
  "username": "admin",
  "password": "admin123"
}

// 3. Copy token and authorize

// 4. Test user endpoint (✅ Should work)
GET /api/users/me

// 5. Test admin endpoint (✅ Should work)
GET /api/admin/users
```

---

## 🛠️ Swagger Features

### Available Features

- **Try it out** - Execute real API calls
- **Request Body Schema** - View expected JSON structure
- **Response Examples** - See sample responses
- **Schema Models** - Browse DTOs and entity structures
- **Authorization** - JWT Bearer token authentication
- **Filter** - Search endpoints
- **Response Status Codes** - See all possible responses

### Swagger Annotations Used

#### Controller Level
```java
@Tag(name = "Authentication", description = "User authentication endpoints")
```

#### Method Level
```java
@Operation(summary = "Login user", description = "Authenticate and return JWT token")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Login successful"),
    @ApiResponse(responseCode = "400", description = "Invalid credentials")
})
```

#### Security
```java
@SecurityRequirement(name = "bearerAuth")
```

---

## 📝 Configuration Details

### pom.xml Dependency

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
    <version>1.8.0</version>
</dependency>
```

### application.properties

```properties
# SpringDoc OpenAPI Configuration
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.swagger-ui.tryItOutEnabled=true
springdoc.swagger-ui.filter=true
```

### Security Configuration

Swagger endpoints are accessible without authentication:

```java
.antMatchers("/swagger-ui/**", "/swagger-ui.html", 
             "/v3/api-docs/**", "/swagger-resources/**", 
             "/webjars/**").permitAll()
```

### OpenAPI Configuration Class

Located at: `src/main/java/com/example/auth/config/OpenApiConfig.java`

- Defines API metadata (title, description, version)
- Configures JWT Bearer authentication scheme
- Sets up global security requirements

---

## 🎨 Customizing Swagger UI

### Change API Information

Edit `OpenApiConfig.java`:

```java
.info(new Info()
    .title("Your API Title")
    .description("Your API Description")
    .version("2.0.0")
    .contact(new Contact()
        .name("Your Name")
        .email("your.email@example.com")))
```

### Add More Security Schemes

```java
.addSecuritySchemes("apiKey", new SecurityScheme()
    .type(SecurityScheme.Type.APIKEY)
    .in(SecurityScheme.In.HEADER)
    .name("X-API-Key"))
```

### Change Swagger UI Path

In `application.properties`:

```properties
springdoc.swagger-ui.path=/api-docs
```

---

## 🔍 Exploring the API

### View All Endpoints

1. Open Swagger UI
2. All endpoints are grouped by tags
3. Expand any tag to see endpoints

### View Request Schema

1. Click on any endpoint
2. See **Request body** section
3. Click **Schema** to see structure
4. Click **Example Value** for sample JSON

### View Response Schema

1. Scroll to **Responses** section
2. See all possible status codes
3. Click on status code to see response structure

### Download OpenAPI Spec

1. Navigate to `http://localhost:8080/v3/api-docs`
2. Save the JSON file
3. Import into Postman or other tools

---

## 💡 Tips and Best Practices

### 1. Use Authorize Button

Instead of manually adding headers, use the **Authorize** button for all authenticated requests.

### 2. Check Response Codes

Always check the response status code to understand what happened.

### 3. JWT Token Expiration

JWT tokens expire after 1 hour (default). Re-login if you get 401 errors.

### 4. Test Without Frontend

Swagger UI is perfect for testing APIs without needing a frontend application.

### 5. Share Documentation

Share the Swagger UI URL with team members for easy API exploration.

---

## 🚨 Troubleshooting

### Swagger UI Not Loading

**Problem:** 404 when accessing `/swagger-ui.html`

**Solution:**
1. Ensure application is running
2. Check security configuration allows Swagger paths
3. Verify dependency is in `pom.xml`
4. Try `/swagger-ui/index.html` instead

### Authorization Not Working

**Problem:** Get 401 even after authorizing

**Solutions:**
1. Make sure you copied the token correctly (no extra spaces)
2. Don't include "Bearer " prefix (Swagger adds it automatically)
3. Check if token expired (default: 1 hour)
4. Re-login to get a fresh token

### Can't See Endpoints

**Problem:** Swagger UI shows no endpoints

**Solutions:**
1. Check if controllers have `@RestController` annotation
2. Verify `@RequestMapping` is present
3. Check if package is being scanned by Spring Boot
4. Clear browser cache and refresh

### JWT Token in Swagger

**Problem:** Don't know where to enter JWT token

**Solution:**
1. Look for **🔓 Authorize** button (top right)
2. Click it
3. Paste token in the input field
4. Click **Authorize** button in modal
5. Close the modal

---

## 📦 Export API Documentation

### Export as JSON

```bash
curl http://localhost:8080/v3/api-docs > api-docs.json
```

### Export as YAML

```bash
curl http://localhost:8080/v3/api-docs.yaml > api-docs.yaml
```

### Import to Postman

1. Open Postman
2. Click **Import**
3. Paste URL: `http://localhost:8080/v3/api-docs`
4. Click **Import**

---

## 🎓 Additional Resources

### SpringDoc OpenAPI Documentation
- Official: https://springdoc.org/
- GitHub: https://github.com/springdoc/springdoc-openapi

### OpenAPI Specification
- https://swagger.io/specification/

### Swagger UI
- https://swagger.io/tools/swagger-ui/

---

## ✅ Verification Checklist

- [ ] Application starts successfully
- [ ] Swagger UI loads at `/swagger-ui.html`
- [ ] All 3 controller tags are visible
- [ ] Can register a new user
- [ ] Can login and receive JWT token
- [ ] Can authorize with JWT token
- [ ] Can access authenticated endpoints
- [ ] Admin endpoints show 403 for regular users
- [ ] API docs available at `/v3/api-docs`

---

## 🎉 Summary

You now have a fully functional Swagger UI integration with:

✅ Interactive API documentation  
✅ JWT authentication support  
✅ Try it out functionality  
✅ Comprehensive API annotations  
✅ Request/Response schemas  
✅ Role-based access control testing  
✅ OpenAPI 3.0 specification  

**Happy API Testing! 🚀**
