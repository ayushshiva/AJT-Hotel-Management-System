package com.ahotel.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerControllerIntegrationTest {
    @LocalServerPort
    private int port;

    @Test
    void customerPagesRequireLoginAndRenderAfterAuthentication() throws Exception {
        CookieManager cookies = new CookieManager();
        cookies.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        HttpClient client = HttpClient.newBuilder()
                .cookieHandler(cookies)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        URI customersUri = URI.create("http://localhost:" + port + "/customers");

        HttpResponse<String> unauthorized = client.send(
                HttpRequest.newBuilder(customersUri).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(302, unauthorized.statusCode());
        String loginLocation = unauthorized.headers().firstValue("Location").orElse("");
        assertTrue(URI.create(loginLocation).getPath().startsWith("/login"));

        String loginForm = "username=" + encode("admin") + "&password=" + encode("admin123");
        HttpResponse<String> login = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/login"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(loginForm))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(302, login.statusCode());

        HttpResponse<String> customerList = client.send(
                HttpRequest.newBuilder(customersUri).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, customerList.statusCode(), customerList.body());
        assertTrue(customerList.body().contains("Customer Management"));
        assertTrue(customerList.body().contains("Customer records"));

        HttpResponse<String> dashboard = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/dashboard")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, dashboard.statusCode(), dashboard.body());
        for (String path : new String[]{"/rooms", "/booking-management", "/food", "/laundry",
                "/room-service", "/employees", "/billing", "/reports"}) {
            HttpResponse<String> page = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, page.statusCode(), path + ": " + page.body());
        }

        String customerName = "Integration Guest " + System.nanoTime();
        String email = "integration." + System.nanoTime() + "@example.com";
        String mobileNumber = "9" + String.format("%09d", Math.floorMod(System.nanoTime(), 1000000000L));
        String customerForm = "fullName=" + encode(customerName)
                + "&mobileNumber=" + mobileNumber + "&emailAddress=" + encode(email);
        HttpResponse<String> createCustomer = client.send(
                HttpRequest.newBuilder(customersUri)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(customerForm))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(302, createCustomer.statusCode(), createCustomer.body());

        HttpResponse<String> customerSearch = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port
                        + "/customers?q=" + encode(customerName))).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, customerSearch.statusCode(), customerSearch.body());
        assertTrue(customerSearch.body().contains(customerName));

        long customerId = lastPathId(createCustomer.headers().firstValue("Location").orElseThrow());
        String roomNumber = "HT" + Long.toString(System.nanoTime()).substring(0, 8);
        HttpResponse<String> createRoom = post(client, "/rooms", Map.of(
                "roomNumber", roomNumber, "roomType", "Double", "floorNumber", "2",
                "capacity", "2", "pricePerNight", "100.00", "status", "AVAILABLE"));
        assertEquals(302, createRoom.statusCode());
        long roomId = lastPathId(createRoom.headers().firstValue("Location").orElseThrow());

        LocalDate checkIn = LocalDate.now().plusDays(1);
        HttpResponse<String> createReservation = post(client, "/booking-management", Map.of(
                "customerId", Long.toString(customerId), "roomId", Long.toString(roomId),
                "checkIn", checkIn.toString(), "checkOut", checkIn.plusDays(2).toString()));
        assertEquals(302, createReservation.statusCode());
        long reservationId = lastPathId(createReservation.headers().firstValue("Location").orElseThrow());
        assertEquals(200, client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port
                + "/booking-management/" + reservationId)).GET().build(), HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(302, post(client, "/booking-management/" + reservationId + "/check-in", Map.of()).statusCode());

        String foodName = "HTTP meal " + System.nanoTime();
        assertEquals(302, post(client, "/food/items", Map.of("name", foodName, "category", "Test",
                "price", "5.00", "available", "true")).statusCode());
        HttpResponse<String> foodPage = client.send(HttpRequest.newBuilder(
                URI.create("http://localhost:" + port + "/food")).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertTrue(foodPage.body().contains(foodName));

        assertEquals(302, post(client, "/laundry", Map.of("reservationId", Long.toString(reservationId),
                "itemName", "Towel", "quantity", "2", "ratePerItem", "3.00")).statusCode());
        assertEquals(302, post(client, "/room-service", Map.of("reservationId", Long.toString(reservationId),
                "serviceType", "Water", "serviceCharge", "8.00")).statusCode());

        HttpResponse<String> servlet = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/legacy-status")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, servlet.statusCode());
        assertTrue(servlet.body().contains("Servlet Status"));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private HttpResponse<String> post(HttpClient client, String path, Map<String, String> values) throws Exception {
        String body = values.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(java.util.stream.Collectors.joining("&"));
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private long lastPathId(String location) {
        String path = URI.create(location).getPath();
        String segment = path.substring(path.lastIndexOf('/') + 1).replaceFirst(";.*$", "");
        return Long.parseLong(segment);
    }
}
