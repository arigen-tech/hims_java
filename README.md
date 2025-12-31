# 🏥 HIMS Java Application

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14-blue)
![FHIR](https://img.shields.io/badge/FHIR-R4-blue)
![Architecture](https://img.shields.io/badge/Architecture-Microservices-success)
![Status](https://img.shields.io/badge/Status-Active-success)

> **HIMS Java** is a backend **Hospital Information Management System** built using **Java & Spring Boot**, designed to handle healthcare workflows with **FHIR R4 compliance**, secure data processing, and a **scalable microservices architecture**.

---

## 📌 Key Features

- 🔐 Secure encryption & decryption of healthcare data  
- 📄 FHIR R4 JSON validation  
- 🏥 Coverage Eligibility processing  
- 🔁 Asynchronous background status updates  
- 🧩 Microservices-ready architecture  
- 🗄 PostgreSQL for reliable data storage  
- 📊 Robust logging & error handling  

---

## 🧩 System Architecture (Microservices)

![Microservices Architecture](https://miro.medium.com/v2/resize:fit:1400/1*YhW3d9DkzY7O5Kb23xjz0Q.png)

### Architecture Overview
- **API Gateway** – Request routing & security  
- **Auth Service** – Token validation & authorization  
- **HIMS Core Service** – Business logic  
- **Eligibility Service** – Coverage & benefits  
- **Notification Service** – Status updates  
- **External Systems** – FHIR / ABDM / NHCX  

---

## 🗄 Database – PostgreSQL

![PostgreSQL](https://www.postgresql.org/media/img/about/press/elephant.png)

PostgreSQL is used as the primary database for:

- Patient & provider records  
- Coverage eligibility requests  
- Transaction & audit logs  
- JSON / JSONB storage for FHIR payloads  

### Database Advantages
- ACID-compliant transactions  
- High performance indexing  
- Strong consistency & reliability  
- Secure role-based access  

---

## 🏗 Microservices Overview

| Service Name | Responsibility |
|-------------|----------------|
| API Gateway | Routing, throttling, security |
| Auth Service | Authentication & authorization |
| HIMS Core | Core healthcare workflows |
| Eligibility Service | Coverage & benefits |
| Notification Service | Async status updates |
| Audit Service | Logging & compliance |

---

## 🛠 Technology Stack

| Layer | Technology |
|-----|-----------|
| Language | Java 17 |
| Framework | Spring Boot |
| Architecture | Microservices |
| Database | PostgreSQL |
| API Style | REST |
| Standards | FHIR R4 |
| Build Tool | Maven / Gradle |
| Logging | SLF4J / Logback |

---

## 📂 Project Structure

```text
hims-java
│── src/main/java
│   ├── controller
│   ├── service
│   ├── model
│   ├── repository
│   ├── config
│   └── util
│
│── src/main/resources
│   ├── application.yml
│   ├── logback.xml
│   └── db/migration
│
│── docs/images
│   ├── microservices-architecture.png
│   └── postgres-db-design.png
│
└── README.md
