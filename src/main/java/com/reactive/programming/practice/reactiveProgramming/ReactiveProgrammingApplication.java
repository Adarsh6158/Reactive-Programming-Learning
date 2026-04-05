package com.reactive.programming.practice.reactiveProgramming;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * ReactiveProgrammingApplication - Main Spring Boot Application.
 *
 * This is a complete, production-ready Spring Boot WebFlux application demonstrating:
 *
 * ============ PROJECT STRUCTURE ============
 * model/           - Domain objects (Product entity)
 * repository/      - Data access layer (ReactiveMongoRepository)
 * service/         - Business logic layer
 * controller/      - REST endpoints (annotation-based routing)
 * handler/         - Functional routing handlers
 * config/          - Application configuration
 * exception/       - Global exception handling
 * util/            - Utility classes (operators demonstration)
 * client/          - WebClient examples (external API calls)
 *
 * ============ KEY TECHNOLOGIES ============
 * - Spring Boot 3.5.13: Latest version with Java 17
 * - Spring WebFlux: Reactive web framework
 * - MongoDB Reactive: Non-blocking database driver
 * - Project Reactor: Underlying reactive library (Mono, Flux)
 * - Lombok: Reduces boilerplate code
 *
 * ============ CORE CONCEPTS ============
 *
 * 1. REACTIVE PROGRAMMING:
 *    - Non-blocking: Threads aren't blocked waiting for I/O
 *    - Asynchronous: Operations return immediately
 *    - Backpressure: Consumer controls production rate
 *
 * 2. MONO vs FLUX:
 *    - Mono<T>: Represents 0 or 1 element
 *    - Flux<T>: Represents 0 to N elements (stream)
 *
 * 3. ROUTING APPROACHES:
 *    - Annotation-based (@RestController): Simpler, more discoverable
 *    - Functional routing (Router + Handler): More flexible
 *
 * 4. OPERATORS:
 *    - Transformation: map, flatMap, switchMap
 *    - Filtering: filter, take, skip, distinct
 *    - Combining: zip, merge, concat
 *    - Error Handling: onErrorResume, retry, timeout
 *
 * ============ API ENDPOINTS ============
 *
 * ANNOTATION-BASED CONTROLLER (/api/v1/products):
 * - GET    /api/v1/products                    → Get all products
 * - GET    /api/v1/products/{id}               → Get product by ID
 * - POST   /api/v1/products                    → Create product
 * - PUT    /api/v1/products/{id}               → Update product
 * - DELETE /api/v1/products/{id}               → Delete product
 * - GET    /api/v1/products/category/{category} → Filter by category
 * - GET    /api/v1/products/search?query=text → Search by name
 *
 * FUNCTIONAL ROUTING (/api/v1/products-functional):
 * - Same endpoints as above but with different implementation approach
 * - Demonstrates how to do routing without @RestController
 *
 * OPERATORS DEMONSTRATION (/api/v1/demo/):
 * - GET    /api/v1/demo/map                   → MAP operator
 * - GET    /api/v1/demo/flatmap               → FLATMAP operator
 * - GET    /api/v1/demo/filter                → FILTER operator
 * - GET    /api/v1/demo/take                  → TAKE operator
 * - GET    /api/v1/demo/skip                  → SKIP operator
 * - GET    /api/v1/demo/distinct              → DISTINCT operator
 * - GET    /api/v1/demo/zip                   → ZIP combinator
 * - GET    /api/v1/demo/error-handling/*      → Error handling patterns
 *
 * ============ DATABASE CONFIGURATION ============
 *
 * MongoDB is configured via properties (application.properties):
 * - Start MongoDB locally or use Docker:
 *   docker run -d -p 27017:27017 --name mongodb mongo:latest
 * - Or use MongoDB Atlas cloud service (update connection string)
 *
 * ============ EXCEPTION HANDLING ============
 *
 * GlobalExceptionHandler handles:
 * - ProductNotFoundException (404)
 * - IllegalArgumentException (400)
 * - Generic Exception (500)
 *
 * Returns consistent JSON error responses with:
 * - message: Error description
 * - errorCode: Machine-readable error code
 * - status: HTTP status code
 * - timestamp: When error occurred
 * - path: Request path
 *
 * ============ INTERVIEW TALKING POINTS ============
 *
 * 1. Why Reactive?
 *    - Scalability: Handle more concurrent connections with fewer threads
 *    - Resource efficiency: Non-blocking I/O reduces resource consumption
 *    - Better user experience: Asynchronous operations feel faster
 *
 * 2. Mono vs Flux:
 *    - Use Mono: When expecting 0-1 result (findById, create, delete)
 *    - Use Flux: When expecting multiple results (findAll, search)
 *
 * 3. flatMap vs map:
 *    - map: T → R (direct transformation, synchronous)
 *    - flatMap: T → Mono<R>/Flux<R> (async transformation, chains)
 *
 * 4. Error Handling:
 *    - onErrorReturn: Fallback value
 *    - onErrorResume: Call alternative operation
 *    - retry: Automatic retry mechanism
 *
 * 5. Performance:
 *    - Non-blocking frees threads
 *    - Allows single server to handle thousands of concurrent requests
 *    - Better resource utilization than traditional blocking approach
 *
 * ============ TESTING ENDPOINTS ============
 *
 * Using cURL:
 * - Get all: curl http://localhost:8080/api/v1/products
 * - Get one: curl http://localhost:8080/api/v1/products/{id}
 * - Create: curl -X POST http://localhost:8080/api/v1/products \
 *           -H "Content-Type: application/json" \
 *           -d '{"name":"Product","price":100,"quantity":5}'
 *
 * Using Postman: Import collection from project docs
 * Using REST Client VSCode extension: Create .http files
 *
 * ============ PRODUCTION READY FEATURES ============
 *
 * ✓ Proper package structure
 * ✓ Separation of concerns (model, repository, service, controller)
 * ✓ Global exception handling
 * ✓ Logging (SLF4J with Logback)
 * ✓ Non-blocking data access
 * ✓ Reactive REST endpoints
 * ✓ Functional routing support
 * ✓ WebClient for external APIs
 * ✓ Custom exceptions
 * ✓ Request validation
 * ✓ Comprehensive documentation
 *
 * ============ HOW TO USE THIS PROJECT ============
 *
 * 1. Clone/Download the project
 * 2. Start MongoDB:
 *    docker run -d -p 27017:27017 --name mongodb mongo:latest
 * 3. Build the project:
 *    ./mvnw clean package
 * 4. Run the application:
 *    ./mvnw spring-boot:run
 * 5. Test the endpoints using cURL, Postman, or a REST client
 * 6. Study the code and understand each component
 * 7. Modify for your use case
 * 8. Deploy to production with appropriate configurations
 *
 * ============ LEARNING RESOURCES ============
 *
 * - Spring WebFlux Documentation: https://spring.io/projects/spring-webflux
 * - Project Reactor: https://projectreactor.io/
 * - Reactive Programming: https://reactivex.io/
 * - RxJava Operators: https://rxjava.io/
 *
 * @author Learning Project
 * @version 1.0
 * @since 2024
 */
@SpringBootApplication
@Slf4j
public class ReactiveProgrammingApplication {

	/**
	 * Application entry point.
	 * Initializes Spring Boot application and logs startup information.
	 *
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		// Run the Spring Boot application
		ConfigurableApplicationContext context = SpringApplication.run(ReactiveProgrammingApplication.class, args);

		// Log application startup information
		log.info("╔════════════════════════════════════════════════════════════╗");
		log.info("║     Spring Boot WebFlux Application Started               ║");
		log.info("║     ────────────────────────────────────────             ║");
		log.info("║ 📚 SWAGGER UI:                                             ║");
		log.info("║    http://localhost:8081/swagger-ui/index.html            ║");
		log.info("║    http://localhost:8081/v3/api-docs                      ║");
		log.info("║                                                            ║");
		log.info("║ 🔗 API ENDPOINTS:                                          ║");
		log.info("║ Annotation-based:  http://localhost:8081/api/v1/products  ║");
		log.info("║ Functional:        http://localhost:8081/api/v1/products-functional");
		log.info("║ Demo:              http://localhost:8081/api/v1/demo      ║");
		log.info("║ Health:            http://localhost:8081/actuator/health  ║");
		log.info("║                                                            ║");
		log.info("║ 🗄️  DATABASE: MongoDB (Reactive)                           ║");
		log.info("║    Ensure MongoDB is running on localhost:27017          ║");
		log.info("║                                                            ║");
		log.info("╚════════════════════════════════════════════════════════════╝");
	}

}
