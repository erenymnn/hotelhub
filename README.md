# 🏨 HotelHub - Advanced Hotel Booking & Management API

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-brightgreen.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)
![Docker](https://img.shields.io/badge/Docker-Containerized-blueviolet.svg)

## 📖 About the Project
HotelHub is a robust, production-ready backend API designed for hotel reservation and management systems. Built with modern backend architecture principles, it ensures secure, isolated, and scalable operations. The system avoids traditional "CRUD-only" approaches by implementing complex business logic directly at the database and filter levels, preventing real-world issues like double-booking.

## ✨ Key Architectural Features

* **Advanced Dynamic Search:** Implemented **Criteria API** to handle complex, multi-parameter, and dynamic hotel search queries without bloating the repository layer.
* **Stateless Security (JWT):** Secured endpoints using JSON Web Tokens. Authentication errors at the filter level (e.g., Expired or Malformed JWT) are gracefully caught via `HandlerExceptionResolver` and passed to a centralized handler.
* **Global Exception Handling:** Replaced default server errors (500) with standard, frontend-friendly JSON responses (401, 404, 400) using `@RestControllerAdvice`.
* **Data Transfer Objects (DTO) Isolation:** Entity models are strictly isolated from the presentation layer. **MapStruct** is utilized for automated, highly optimized object mapping.
* **Database-Level Data Integrity:** Handled concurrency and "Double Booking" scenarios using optimized JPQL/SQL queries instead of memory-heavy Java loops.
* **Fully Containerized Environment:** The application and its PostgreSQL database are fully orchestrated using **Docker** and **Docker Compose**, ensuring a 100% portable "works on my machine" guarantee.

## 🛠️ Tech Stack

* **Core:** Java 21, Spring Boot 4.0.5
* **Data Access:** Spring Data JPA, Hibernate, PostgreSQL
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