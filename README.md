# 🏨 AJT Hotel Management System

A web-based **Hotel Management System** designed to simplify and manage day-to-day hotel operations through a centralized dashboard.

The system provides essential hotel management features, including customer management, room management, reservations, food orders, laundry services, room service, employee management, billing, and reports.

🔗 **Live Demo:** https://ajt-hotel-management-system.onrender.com/

📂 **GitHub Repository:** https://github.com/ayushshiva/AJT-Hotel-Management-System

---

## ✨ Features

### 📊 Dashboard
- Centralized hotel management dashboard.
- Quick access to major hotel operations.
- Overview of hotel activities.

### 👥 Customer Management
- Register new customers.
- Manage customer information.
- Search and view customer records.

### 🛏️ Room Management
- Manage hotel rooms.
- Track room availability and details.
- Organize room-related operations.

### 📅 Booking & Reservations
- Manage customer reservations.
- Handle booking-related operations.
- Support the hotel reservation workflow.

### 🍽️ Food Management
- Create food orders.
- Manage food order status.
- Track the order workflow:
  `PENDING → PREPARING → READY → SERVED`

### 🧺 Laundry Management
- Manage laundry requests.
- Track laundry service status.

### 🛎️ Room Service
- Create room service requests.
- Manage service progress and completion.

### 👨‍💼 Employee Management
- Manage employee information.
- Maintain employee records.

### 💳 Billing & Payments
- Generate customer invoices.
- Calculate GST.
- Record payment details.
- Track outstanding amounts.

### 📈 Reports
- Access hotel management reports.
- Review operational information.

---

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Backend programming |
| Spring Boot 4.1.2 | Backend framework |
| Spring MVC | Web application architecture |
| JSP | Dynamic web pages |
| JDBC | Database connectivity |
| H2 Database | Database management |
| Maven | Build and dependency management |
| JUnit 5 | Unit and integration testing |
| Mockito | Testing support |
| Docker | Deployment environment |
| Render | Cloud hosting |

---

## 🏗️ Application Architecture

The application follows a layered architecture:

```text
AJT Hotel Management System
│
├── Presentation Layer
│   └── JSP Views
│
├── Controller Layer
│   └── Spring MVC Controllers
│
├── Service Layer
│   └── Business Logic
│
├── Repository / Data Access
│   └── JDBC
│
└── Database Layer
    └── H2 Database
```

---

## 🚀 Getting Started

### Prerequisites

Make sure you have installed:

- Java JDK 21 or later
- Apache Maven
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/ayushshiva/AJT-Hotel-Management-System.git
```

### 2. Navigate to the Project

```bash
cd AJT-Hotel-Management-System
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

### 4. Open in Browser

```text
http://localhost:8081/
```

---

## 🔐 Demo Login

The deployed demo has the following sample login credentials:

| Field | Value |
|---|---|
| Username | `admin` |
| Password | `admin123` |

**Note:** These are demonstration credentials. Do not use demo credentials for a production environment.

---

## 🧪 Testing

The project includes automated tests for customer management, hotel operations, room reservations, and customer controller integration.

Run all tests:

```bash
mvn test
```

Build the application:

```bash
mvn clean package
```

**Latest verified build results:**

- 8 Tests Executed
- 0 Failures
- 0 Errors
- WAR Packaging Successful

---

## ☁️ Deployment

The application is deployed using Render.

**Live Application:**  
https://ajt-hotel-management-system.onrender.com/

Note: The current demonstration deployment uses Render's Free instance. The instance may spin down after inactivity, and its local filesystem is not persistent. Database records may therefore not survive restarts or redeployments.

---

## 📸 Screenshots

Add application screenshots here to showcase:

- Dashboard
- Customer Management
- Room Management
- Booking Management
- Food Management
- Billing & Invoice
- Reports

Example:

```markdown
![Dashboard](screenshots/dashboard.png)
```

---

## 🎯 Project Objectives

- Simplify hotel management operations.
- Centralize customer and reservation information.
- Organize room and service management.
- Improve billing and payment workflows.
- Provide a user-friendly web interface.
- Demonstrate practical implementation of Java and Spring Boot technologies.

---

## 👨‍💻 Developer

**Ayush Kumar**

GitHub: [@ayushshiva](https://github.com/ayushshiva)

---

## 📄 License

This project was developed as an academic/college project.

---

⭐ If you find this project interesting, consider giving the repository a star!

**Built with Java, Spring Boot and dedication. ❤️**
