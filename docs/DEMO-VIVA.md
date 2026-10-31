# Demo / Viva Quick Notes

- **Swing:** Desktop application entry point is `Main`. The dashboard includes room management, desktop summaries and a receipt-history view. The Swing data store is separate from the web database.
- **Servlet:** `SystemStatusServlet` extends `HttpServlet`, handles GET at `/legacy-status`, and is registered by the Spring Boot application.
- **JSP/JSTL:** Views under `src/main/webapp/WEB-INF/jsp` render dashboard, customer, room, reservation and operations pages on the server.
- **Spring:** Spring Boot starts the application. Spring MVC controllers call services; services use JdbcTemplate and H2. Transactional services coordinate reservation, order and invoice writes.
- **Room availability:** Reservations overlap if an active stay has `check_in < requested_check_out` and `check_out > requested_check_in`. Cancelled and checked-out records do not block availability.
- **Billing:** Room, served-food, delivered-laundry and completed-room-service charges are persisted into an invoice. GST defaults to configurable 18%. Payment rows only record demo payments; no gateway is integrated.
- **File Manager:** Swing `FileManager` continues appending the existing timestamped receipt log. File Management displays that same log.
- **H2:** The web app starts without a separately installed DB. Its default in-memory database resets on restart.
- **Demo credentials:** Web `admin` / `admin123`; Swing `admin` / `1234`. These are unsuitable for production.
- **Legacy compatibility:** Existing bookings remain in the original `bookings` table and are shown separately from date-based `reservations`; legacy bookings have no dates to migrate.
