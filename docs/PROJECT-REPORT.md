# HOTEL MANAGEMENT SYSTEM
## AJT CSE Project Report
### Servlet, JSP, Spring Boot and Swing

**Submitted by:** Aayush Kumar Mishra  
**Course:** Advanced Java Technology (AJT)  
**Project type:** Java desktop and web application prototype

## 1. Abstract

This project demonstrates Java desktop and web technologies used in hotel operations. The web application uses Spring MVC, JSP/JSTL and JdbcTemplate with H2 persistence. The legacy Swing client remains available as a separate desktop demo and retains its receipt logging.

## 2. Technology stack

Java 21, Spring Boot 4, Spring MVC, Spring JDBC, Jakarta Servlet, JSP/JSTL, H2, Maven, embedded Tomcat and Swing.

## 3. Modules

**Web:** academic demo login/logout, dashboard, customer CRUD and booking history, rooms, date-ranged reservations, food menu and multi-item orders, laundry requests, room service, employee records, invoices/payments and reports. The Servlet endpoint `/legacy-status` reports that the servlet container is serving requests.

**Swing:** login, dashboard, legacy room/customer/food/laundry/billing flows, room administration, desktop summary report and receipt history via File Management.

## 4. Architecture

Web requests follow Spring MVC Controller → Service → JdbcTemplate/Repository → H2. Services own validation, transaction boundaries, reservation-overlap checks, service charge calculations and invoice arithmetic. JSP/JSTL renders server-side pages. The explicit `SystemStatusServlet` is registered by Spring Boot.

The original booking table is retained for compatibility. New reservations are stored separately with room/customer references, check-in/out dates and lifecycle status; existing legacy records do not have dates and are not migrated.

## 5. Database

`src/main/resources/schema.sql` initializes additive tables for customers, rooms, reservations, food items/orders/lines, employees, laundry, room service, invoices and payments. The schema uses create-if-not-exists statements and does not drop existing records. H2 is in-memory by default, so data resets when the process stops. `database/mysql-schema.sql` provides an optional MySQL 8 schema; it has not been validated against a running MySQL server.

GST defaults to 18%, configurable with `hotel.billing.gst-rate`. Invoice generation is limited to checked-out reservations, with one invoice per reservation. Payments are recorded in the database; no payment gateway is connected.

## 6. Run and test

Requirements: JDK 21, Maven 3.9+.

```shell
mvn clean test
mvn clean package
mvn spring-boot:run
```

Web URL: `http://localhost:8080/`  
Demo credentials: `admin` / `admin123`  
Servlet: `http://localhost:8080/legacy-status`

Swing:

```shell
javac -d out src/main/java/*.java
java -cp out Main
```

Swing credentials: `admin` / `1234`.

Automated tests cover customer flows, protected HTTP/JSP pages, room CRUD, reservation overlaps and transitions, service totals, employee validation, GST, invoices, payments and reports. The Swing process is launch-tested; UI interactions are not automated.

## 7. Limitations

This is an academic prototype, not a production hotel platform. Demo authentication is hard-coded. H2 is in-memory. Swing and web use separate stores; the Swing UI does not share the web H2 database. The legacy bookings lack dates and lifecycle statuses. Payment recording is not payment processing. The optional MySQL schema is not integration-tested.

## 8. Conclusion

The project has runnable examples of Swing, Servlet, JSP and Spring Boot, plus database-backed web workflows for hotel room and guest operations. Tests and documentation distinguish these implemented flows from the retained legacy desktop demo.
