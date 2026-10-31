# Hotel Management System — AJT CSE Project

**AJT technologies demonstrated:** Java Swing, Jakarta Servlet, JSP/JSTL, and Spring MVC / Spring Boot.

This is an academic demonstration project built by extending the supplied Swing Hotel Management System. The original desktop source files are retained under `src/main/java` in the default package. A Spring-powered web module is added alongside it.

## Technology mapping

| Technology | Where demonstrated | Purpose |
|---|---|---|
| Swing | `src/main/java/*.java` | Desktop login, dashboard, room management, legacy customer/food/laundry/billing screens, reports and receipt history |
| Servlet | `com/ahotel/servlet/SystemStatusServlet.java` | Explicit Jakarta HttpServlet endpoint at `/legacy-status` |
| JSP | `src/main/webapp/WEB-INF/jsp/dashboard.jsp` | Server-rendered dashboard and room booking form using JSTL |
| Spring | `com/ahotel/controller`, `service`, `config` | Spring MVC controllers, dependency injection, booking business rules and JDBC persistence |

## Requirements

- JDK 21
- Apache Maven 3.9+
- Browser for the web module

## Academic demo credentials

- Web application: `admin` / `admin123`
- Swing application: `admin` / `1234`

These are hard-coded demonstration credentials, not suitable for production.

## Run the web application

From the project root:

```bash
mvn clean package
mvn spring-boot:run
```

Open `http://localhost:8080/` or `http://localhost:8080/dashboard`.

- Servlet check: `http://localhost:8080/legacy-status`
- JSON API: `http://localhost:8080/api/bookings`
- Customer management: `http://localhost:8080/customers` (requires the demo login)
- Rooms: `http://localhost:8080/rooms`
- Date-ranged reservations: `http://localhost:8080/booking-management`
- Food menu and orders: `http://localhost:8080/food`
- Laundry: `http://localhost:8080/laundry`
- Room service: `http://localhost:8080/room-service`
- Employees: `http://localhost:8080/employees`
- Billing and invoices: `http://localhost:8080/billing`
- Reports: `http://localhost:8080/reports`
- H2 database console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:hoteldb`, user `sa`, blank password)

The web demo uses an in-memory H2 database by default. Data resets when the application stops. An optional MySQL starter schema is in `database/mysql-schema.sql`; configure the datasource in `src/main/resources/application.properties` before using MySQL.

## Customer management

Sign in with the web demo credentials, open **Customer Management** from the dashboard, and use the customer list to add, search, filter, view, edit, or delete/deactivate records. Mobile number and email must be unique. Existing bookings store customer name and phone rather than a customer foreign key, so booking history is matched by mobile number; a customer with matching booking records is deactivated rather than deleted.

The web modules persist through JdbcTemplate. Reservations reference customers and rooms, block overlapping active stays, and support cancel/check-in/check-out. Food orders store line-item price snapshots; laundry and room-service charges are included only after delivery/completion. Invoices are generated once per checked-out reservation, use configurable GST (`hotel.billing.gst-rate`, default 18%), and accept recorded cash/UPI/card payments without a payment gateway.

The customer and module pages are tested through an embedded HTTP server. Service tests cover room CRUD, reservation overlap/lifecycle, order totals, service charges, invoice/GST arithmetic, payment status, employee validation and reports.

## Run the original Swing desktop application

Compile and run the desktop client from the project root:

```bash
javac -d out src/main/java/*.java
java -cp out Main
```

The Swing client remains a separate desktop demonstration using its original in-memory `DataStore` and `FileManager` receipt log; it does not share the web H2 database. The added Swing room manager and reports operate on that desktop store. Use `File Management` to view receipt history.

## Current implementation boundaries

The legacy `bookings` table and `/bookings` form/API remain for compatibility; those records have no dates or status. New date-based reservations use the separate `reservations` table. Old bookings remain visible in the dashboard and are retained in customer history by phone; they are not automatically migrated because their dates are unknown.

The web modules are session-protected academic demo flows. The Swing application is still only partially aligned with web features and uses in-memory data. The default H2 database is in-memory and resets when the web application stops. The demo credentials are not production authentication. Payments are recorded locally; there is no payment gateway.

## API example

```http
GET http://localhost:8080/api/bookings
Content-Type: application/json
```

```http
POST http://localhost:8080/api/bookings
Content-Type: application/json

{"customerName":"Aman Kumar","phone":"9876543210","roomNumber":"201","nights":2}
```

## Academic note

This project demonstrates the four technologies in one submission. It is a prototype, not a production hotel SaaS: it does not include production authentication, payment gateway integration, deployment hardening, or a shared database connection between the legacy Swing client and the web module.
