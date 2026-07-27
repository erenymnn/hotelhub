# 🏨 HotelHub - Advanced Hotel Booking & Management API

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen.svg)
![Redis](https://img.shields.io/badge/Cache-Redis-red.svg)
![RabbitMQ](https://img.shields.io/badge/Message%20Broker-RabbitMQ-orange.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)
![Docker](https://img.shields.io/badge/Docker-Containerized-blueviolet.svg)
![Security](https://img.shields.io/badge/Security-JWT-red.svg)
![Search](https://img.shields.io/badge/Search-Elasticsearch-yellow.svg)
![Mapping](https://img.shields.io/badge/Mapping-MapStruct-lightgrey.svg)

## 📖 About the Project
HotelHub is a robust, production-ready backend API designed to power modern hotel reservation and management systems. Built on a clean, scalable architecture, it moves beyond simple CRUD operations to handle complex business scenarios. From advanced search algorithms to robust security mechanisms and automated object mapping, HotelHub is engineered to ensure data integrity, prevent common concurrency issues (such as double-booking), and provide a seamless experience for both developers and end-users.

## ✨ Key Architectural Features

* **Advanced Dynamic Search:** Implemented **Criteria API** to handle complex, multi-parameter, and dynamic hotel search queries without bloating the repository layer.
* **High-Performance Caching & Memory Management:** Integrated **Redis** to cache frequently accessed data (e.g., hotel details, search results), significantly reducing database load. Configured the **`allkeys-lru` eviction policy** for proactive capacity management, ensuring optimal performance by automatically clearing the least recently used data when memory limits are reached.
* **API Rate Limiting & Throttling:** Implemented distributed rate limiting via **Redis** to prevent API abuse, mitigate brute-force attacks, and ensure fair usage of system resources across all clients.
* **Stateless Security (JWT):** Secured endpoints using JSON Web Tokens. Authentication errors at the filter level (e.g., Expired or Malformed JWT) are gracefully caught via `HandlerExceptionResolver` and passed to a centralized handler.
* **Global Exception Handling:** Replaced default server errors (500) with standard, frontend-friendly JSON responses (401, 404, 400) using `@RestControllerAdvice`.
* **API Documentation:** Integrated Swagger/OpenAPI for automated, interactive documentation, facilitating seamless testing and frontend integration.
* **Data Transfer Objects (DTO) Isolation:** Entity models are strictly isolated from the presentation layer. **MapStruct** is utilized for automated, highly optimized object mapping.
* **Database-Level Data Integrity:** Handled concurrency and "Double Booking" scenarios using optimized JPQL/SQL queries instead of memory-heavy Java loops.
* **Layered Architecture: Followed a clean, modular architecture (Controller-Service-Repository) for high maintainability and code testability.**
* **Fully Containerized Environment:** The application and its dependencies (PostgreSQL, Redis, RabbitMQ, Elasticsearch) are fully orchestrated using **Docker** and **Docker Compose**, ensuring a 100% portable "works on my machine" guarantee.

## 🛠️ Tech Stack

* **Core:** Java 21, Spring Boot 4.0.5
* **Data Access:** Spring Data JPA, Hibernate, PostgreSQL, Elasticsearch
* **Caching:** Redis
* **Messaging:** RabbitMQ
* **Security:** Spring Security, JWT (JSON Web Tokens)
* **Tooling:** MapStruct, Lombok, Gradle
* **DevOps:** Docker, Docker Compose

## 🚀 Getting Started (Run with Docker)

You don't need to install Java, PostgreSQL, or Gradle on your local machine to run this project. The entire infrastructure is containerized.

### Prerequisites
* [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running.

### Installation & Execution

1. Clone the repository and start the application:
   ```bash
   git clone https://github.com/erenymnn/hotelhub.git
   cd hotelhub
   docker compose up -d

🚀 Accessing API Documentation

   Once the application is up and running, you can explore and test all available endpoints through the Swagger UI interface: http://localhost:8080/swagger-ui/index.html
