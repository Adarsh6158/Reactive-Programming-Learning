# Spring Boot WebFlux Reactive Application

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        HTTP Request/Response                    │
├─────────────────────────────────────────────────────────────────┤
│  Controller Layer (ProductController, OperatorsController)      │
│  - Handles HTTP requests                                        │
│  - Returns appropriate HTTP responses                           │
├─────────────────────────────────────────────────────────────────┤
│  Handler Layer (ProductHandler)                                 │
│  - Functional routing alternative to controllers                │
│  - Serverside utilities for request/response processing         │
├─────────────────────────────────────────────────────────────────┤
│  Service Layer (ProductService)                                 │
│  - Business logic                                               │
│  - Validation                                                   │
│  - Error handling                                               │
├─────────────────────────────────────────────────────────────────┤
│  Repository Layer (ProductRepository)                           │
│  - Data access operations                                       │
│  - ReactiveMongoRepository interface                            │
├─────────────────────────────────────────────────────────────────┤
│            MongoDB (Non-blocking Driver)                        │
└─────────────────────────────────────────────────────────────────┘
```

### Reactive Request Processing Flow

```
HTTP Request
    ↓
@RestController / Handler
    ↓
Controller Method (returns Mono/Flux)
    ↓
Service Layer (business logic)
    ↓
Repository (database query)
    ↓
MongoDB (non-blocking driver)
    ↓
Result emitted through reactive stream
    ↓
Spring WebFlux converts to HTTP Response
    ↓
HTTP Response sent to client
```

## 🛠️ Technologies

- **Java 17** - Latest LTS version with modern features
- **Spring Boot 3.5.13** - Latest Spring Boot with WebFlux
- **Spring WebFlux** - Reactive web framework
- **Spring Data MongoDB Reactive** - Non-blocking database access
- **Project Reactor** - Reactive library (Mono, Flux)
- **Lombok** - Boilerplate reduction
- **Maven** - Build tool

## 📁 Project Structure

```
src/main/java/com/reactive/programming/practice/reactiveProgramming/
├── model/
│   └── Product.java                    # Domain model entity
├── repository/
│   └── ProductRepository.java          # ReactiveMongoRepository
├── service/
│   └── ProductService.java             # Business logic layer
├── controller/
│   ├── ProductController.java          # Annotation-based REST controller
│   └── OperatorsController.java        # Reactor operators examples
├── handler/
│   └── ProductHandler.java             # Functional routing handlers
├── exception/
│   ├── GlobalExceptionHandler.java     # Global exception handling
│   └── ProductNotFoundException.java    # Custom exception
├── config/
│   ├── WebFluxConfig.java              # Functional routing configuration
│   └── WebClientConfig.java            # WebClient configuration
├── client/
│   └── ExternalApiClient.java          # WebClient examples
├── util/
│   └── OperatorsDemonstration.java     # Operators demonstration
└── ReactiveProgrammingApplication.java # Main application class

src/main/resources/
└── application.properties               # Application configuration
```

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- MongoDB 4.0+

### Running the Application

1. **Start MongoDB**
   ```bash
   # macOS (with brew)
   brew services start mongodb-community
   
   # Or Docker
   docker run -d -p 27017:27017 --name mongodb mongo:latest
   ```

2. **Build the project**
   ```bash
   ./mvnw clean build
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Access the API**
   - Swagger UI: http://localhost:8081/swagger-ui.html
   - API Docs: http://localhost:8081/v3/api-docs

## 📚 API Documentation

### Endpoints Overview

**Product Management (9 endpoints)**
- `GET /api/v1/products` - Get all products
- `POST /api/v1/products` - Create new product
- `GET /api/v1/products/{id}` - Get product by ID
- `PUT /api/v1/products/{id}` - Update product
- `DELETE /api/v1/products/{id}` - Delete product
- `GET /api/v1/products/category/{category}` - Filter by category
- `GET /api/v1/products/status/{status}` - Filter by status
- `GET /api/v1/products/count/category/{category}` - Count products
- `GET /api/v1/products/search?name=X` - Search products

**Reactive Operators Demonstrations (15 endpoints)**
- `GET /api/v1/demo/map` - Transform values
- `GET /api/v1/demo/filter` - Filter elements
- `GET /api/v1/demo/flatmap` - Flatten nested streams
- `GET /api/v1/demo/take` - Take N items
- `GET /api/v1/demo/skip` - Skip N items
- `GET /api/v1/demo/zip` - Combine streams
- `GET /api/v1/demo/merge` - Merge streams
- `GET /api/v1/demo/concat` - Concatenate streams
- `GET /api/v1/demo/distinct` - Filter duplicates
- `GET /api/v1/demo/switchmap` - Switch streams
- `GET /api/v1/demo/do-on-operators` - Side effects
- `GET /api/v1/demo/error-handling/on-error-return` - Error fallback
- `GET /api/v1/demo/error-handling/on-error-resume` - Error recovery
- `GET /api/v1/demo/error-handling/retry` - Retry logic
- `GET /api/v1/demo/error-handling/timeout` - Timeout handling

### Testing Endpoints

```bash
# Get all products
curl http://localhost:8081/api/v1/products

# Create new product
curl -X POST http://localhost:8081/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{"name":"iPhone 15","price":999.99,"quantity":5,"category":"Electronics","status":"ACTIVE"}'

# Test Map operator
curl http://localhost:8081/api/v1/demo/map

# Test Filter operator
curl http://localhost:8081/api/v1/demo/filter
```

## ✅ Features

- ✓ **Fully Reactive** - Non-blocking operations with Project Reactor
- ✓ **Production-Ready** - Global exception handling, proper HTTP status codes
- ✓ **Comprehensive Testing** - 65+ unit and integration tests
- ✓ **Full API Documentation** - OpenAPI 3.0 with Swagger UI
- ✓ **Clean Architecture** - Separation of concerns with layered design
- ✓ **Error Handling** - Advanced reactive error handling patterns
- ✓ **MongoDB Integration** - Reactive database driver
- ✓ **CORS Enabled** - Cross-origin requests supported

## 🧪 Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=ProductServiceTests

# Generate test coverage
./mvnw test jacoco:report
```

All tests pass: **65/65 ✅**

## 📖 Learn More

- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/reference/web-reactive.html)
- [Project Reactor Guide](https://projectreactor.io/docs/core/release/reference/)
- [Spring Data MongoDB Reactive](https://docs.spring.io/spring-data/mongodb/reference/mongodb/reactive-repositories.html)
- [OpenAPI 3.0 Specification](https://spec.openapis.org/oas/v3.0.0)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd reactiveProgramming
   ```

2. **Start MongoDB** (using Docker recommended)
   ```bash
   docker run -d -p 27017:27017 --name mongodb mongo:latest
   ```

   Or using Homebrew (macOS):
   ```bash
   brew install mongodb-community
   brew services start mongodb-community
   ```

3. **Build the project**
   ```bash
   ./mvnw clean package
   ```

4. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

5. **Access the application**
   - Application running on: `http://localhost:8080`
   - Health check: `http://localhost:8080/actuator/health`

## 🔗 API Endpoints

### Annotation-Based Controller (Traditional REST)

Base URL: `/api/v1/products`

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/` | Get all products |
| GET | `/{id}` | Get product by ID |
| POST | `/` | Create new product |
| PUT | `/{id}` | Update product |
| PATCH | `/{id}` | Partially update product |
| DELETE | `/{id}` | Delete product |
| GET | `/category/{category}` | Get products by category |
| GET | `/status/{status}` | Get products by status |
| GET | `/search?query=text` | Search products by name |
| GET | `/count/category/{category}` | Count products in category |

### Functional Routing (Alternative Approach)

Base URL: `/api/v1/products-functional`

Same endpoints as above but implemented using functional routing approach.

### Operators Demonstration

Base URL: `/api/v1/demo`

| Endpoint | Operator Demonstrated |
|----------|----------------------|
| `/map` | MAP - element transformation |
| `/flatmap` | FLATMAP - async transformation |
| `/switchmap` | SWITCHMAP - cancel previous on new |
| `/filter` | FILTER - conditional filtering |
| `/take` | TAKE - emit first N elements |
| `/skip` | SKIP - skip first N elements |
| `/distinct` | DISTINCT - remove duplicates |
| `/zip` | ZIP - combine multiple streams |
| `/merge` | MERGE - interleave streams |
| `/concat` | CONCAT - sequential concatenation |
| `/error-handling/on-error-return` | ONERRORRETURN - fallback value |
| `/error-handling/on-error-resume` | ONERRORRESUME - recovery operation |
| `/error-handling/retry` | RETRY - automatic retry |
| `/error-handling/timeout` | TIMEOUT - fail if takes too long |
| `/do-on-operators` | DO-ON - side effects & lifecycle |

### Example Requests

**Get all products:**
```bash
curl http://localhost:8080/api/v1/products
```

**Create product:**
```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 999.99,
    "quantity": 10,
    "category": "Electronics",
    "status": "ACTIVE"
  }'
```

**Search products:**
```bash
curl "http://localhost:8080/api/v1/products/search?query=laptop"
```

**Get operator demo:**
```bash
curl http://localhost:8080/api/v1/demo/map
```

## 📖 Key Concepts

### Reactive Streams

**Mono** - Represents 0 or 1 element:
```java
Mono<Product> product = repository.findById("123");
// Emits one Product or empty
```

**Flux** - Represents 0 to N elements (stream):
```java
Flux<Product> products = repository.findAll();
// Emits multiple Products
```

### Non-Blocking Operators

**map** - One-to-one transformation (synchronous):
```java
Flux.range(1, 5)
    .map(n -> n * 2)  // Transforms 1 → 2, 2 → 4, etc.
```

**flatMap** - One-to-many, async transformation:
```java
Flux.range(1, 3)
    .flatMap(userId -> fetchUserFromDB(userId))  // Async operation per element
```

**filter** - Conditional filtering:
```java
Flux.range(1, 10)
    .filter(n -> n % 2 == 0)  // Keep only even numbers
```

**switchMap** - Cancel previous, keep only latest:
```java
searchInput
    .switchMap(query -> search(query))  // Cancel previous search
```

### Error Handling

**onErrorReturn** - Return default value:
```java
mono.onErrorReturn("Default Value")
```

**onErrorResume** - Call fallback operation:
```java
mono.onErrorResume(error -> 
    callFallbackAPI()
)
```

**retry** - Automatic retry:
```java
mono.retry(3)  // Retry 3 times on error
```

### WebClient Usage

Non-blocking HTTP client:
```java
webClient.get()
    .uri("/api/users/{id}", userId)
    .retrieve()
    .bodyToMono(User.class)
    .timeout(Duration.ofSeconds(10))
```

## 🎓 Q/A

### 1. **Why Reactive Programming?**
- Handle more concurrent connections with fewer threads
- Better resource utilization
- Improved scalability and responsiveness

### 2. **Mono vs Flux**
- **Mono**: Single value or empty (findById, create, delete)
- **Flux**: Multiple values (findAll, search)

### 3. **Operator Categories**
- **Transformation**: map, flatMap, switchMap
- **Filtering**: filter, take, skip, distinct
- **Combining**: zip, merge, concat
- **Error Handling**: onErrorReturn, onErrorResume, retry

### 4. **Blocking vs Non-Blocking**
- **Blocking**: Thread waits for I/O (RestTemplate)
- **Non-Blocking**: Thread continues, callback on completion (WebClient)

### 5. **REST Approaches**
- **Annotation-based**: @RestController, @GetMapping (simpler)
- **Functional routing**: Router + Handler (more flexible)

### 6. **Global Exception Handling**
- @RestControllerAdvice centralized error handling
- Consistent error response format
- Appropriate HTTP status codes

### 7. **WebClient Usage**
- Non-blocking HTTP client
- Supports Mono/Flux responses
- Built-in timeout, retry support
- Better than RestTemplate for reactive apps


### Configuration

Update `application.properties`:

```properties
# Production MongoDB (Atlas)
spring.data.mongodb.uri=mongodb+srv://user:password@cluster.mongodb.net/dbname

# Logging
logging.level.root=WARN
logging.level.com.reactive.programming=INFO

# Error handling
server.error.include-stacktrace=never
server.error.include-message=always
```

### Docker Deployment

Create `Dockerfile`:
```dockerfile
FROM openjdk:17-slim
COPY target/reactiveProgramming-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Build and run:
```bash
docker build -t reactive-app .
docker run -p 8080:8080 reactive-app
```

