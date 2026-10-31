<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Customer Details · AJT Hotel</title>
    <style>
        *{box-sizing:border-box}body{margin:0;background:#f3f6fb;color:#172033;font:15px Arial,sans-serif}
        .top{background:#142d4e;color:#fff;padding:22px max(5%,calc((100% - 1000px)/2))}.top h1{margin:0 0 8px;font-size:25px}.top a{color:#fff}
        main{max-width:1000px;margin:28px auto;padding:0 18px}.card{background:#fff;border-radius:12px;padding:22px;margin-bottom:18px;box-shadow:0 3px 14px #152b4512}
        .actions{display:flex;gap:10px;flex-wrap:wrap}.button{display:inline-block;border:0;border-radius:7px;padding:10px 15px;background:#1765c1;color:#fff;text-decoration:none;font-weight:bold;cursor:pointer}
        .button.secondary{background:#e8eef6;color:#173b64}.button.danger{background:#b42318}.grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:18px}
        dt{font-size:12px;color:#64748b;margin-bottom:4px}dd{margin:0;overflow-wrap:anywhere}.item{padding-bottom:10px}
        table{width:100%;border-collapse:collapse}td,th{text-align:left;padding:11px;border-bottom:1px solid #e7ebf0}.notice{padding:12px;border-radius:8px;margin-bottom:16px}
        .success{background:#e4f7e9;color:#17613b}.empty{padding:20px;text-align:center;color:#64748b}
        @media(max-width:620px){.grid{grid-template-columns:1fr}.top{padding:20px}.table-wrap{overflow-x:auto}}
    </style>
</head>
<body>
<header class="top"><h1>Customer Details</h1><a href="${pageContext.request.contextPath}/customers">Customer Management</a> · <a href="${pageContext.request.contextPath}/dashboard">Dashboard</a></header>
<main>
    <c:if test="${not empty success}"><div class="notice success"><c:out value="${success}"/></div></c:if>
    <section class="card">
        <div class="actions" style="justify-content:space-between;margin-bottom:20px"><h2 style="margin:0"><c:out value="${customer.fullName}"/></h2>
            <div class="actions"><a class="button" href="${pageContext.request.contextPath}/customers/${customer.id}/edit">Edit Customer</a>
                <form method="post" action="${pageContext.request.contextPath}/customers/${customer.id}/delete" onsubmit="return confirm('Delete this customer? Customers with booking history will be deactivated instead.');">
                    <button class="button danger" type="submit">Delete / Deactivate</button>
                </form>
            </div>
        </div>
        <dl class="grid">
            <div class="item"><dt>Customer ID</dt><dd>${customer.id}</dd></div>
            <div class="item"><dt>Status</dt><dd><c:out value="${customer.status}"/></dd></div>
            <div class="item"><dt>Mobile number</dt><dd><c:out value="${customer.mobileNumber}"/></dd></div>
            <div class="item"><dt>Email address</dt><dd><c:out value="${customer.emailAddress}"/></dd></div>
            <div class="item"><dt>Gender</dt><dd><c:out value="${empty customer.gender ? 'Not specified' : customer.gender}"/></dd></div>
            <div class="item"><dt>Registration date</dt><dd>${customer.registrationDate}</dd></div>
            <div class="item"><dt>Address</dt><dd><c:out value="${empty customer.address ? 'Not provided' : customer.address}"/></dd></div>
            <div class="item"><dt>City</dt><dd><c:out value="${empty customer.city ? 'Not provided' : customer.city}"/></dd></div>
            <div class="item"><dt>State / province</dt><dd><c:out value="${empty customer.state ? 'Not provided' : customer.state}"/></dd></div>
            <div class="item"><dt>Country</dt><dd><c:out value="${empty customer.country ? 'Not provided' : customer.country}"/></dd></div>
            <div class="item"><dt>ID proof</dt><dd><c:choose><c:when test="${not empty customer.idProofType}"><c:out value="${customer.idProofType}"/> · ending <c:out value="${maskedProofSuffix}"/></c:when><c:otherwise>Not provided</c:otherwise></c:choose></dd></div>
        </dl>
    </section>
    <section class="card">
        <h2>Legacy booking history</h2>
        <c:choose>
            <c:when test="${empty bookings}"><p class="empty">No bookings are associated with this customer's mobile number.</p></c:when>
            <c:otherwise><div class="table-wrap"><table><thead><tr><th>Booking ID</th><th>Customer on booking</th><th>Phone</th><th>Room</th><th>Nights</th></tr></thead><tbody>
                <c:forEach items="${bookings}" var="booking"><tr><td>${booking.id}</td><td><c:out value="${booking.customerName}"/></td><td><c:out value="${booking.phone}"/></td><td><c:out value="${booking.roomNumber}"/></td><td>${booking.nights}</td></tr></c:forEach>
            </tbody></table></div></c:otherwise>
        </c:choose>
    </section>
    <section class="card">
        <h2>Date-based reservation history</h2>
        <c:choose>
            <c:when test="${empty reservations}"><p class="empty">No date-based reservations are linked to this customer.</p></c:when>
            <c:otherwise><div class="table-wrap"><table><thead><tr><th>Reservation ID</th><th>Room</th><th>Check-in</th><th>Check-out</th><th>Nights</th><th>Charges</th><th>Status</th></tr></thead><tbody>
                <c:forEach items="${reservations}" var="reservation"><tr><td><a href="${pageContext.request.contextPath}/booking-management/${reservation.id}">${reservation.id}</a></td><td><c:out value="${reservation.roomNumber}"/></td><td>${reservation.checkIn}</td><td>${reservation.checkOut}</td><td>${reservation.nights}</td><td>${reservation.totalRoomCharges}</td><td><c:out value="${reservation.status}"/></td></tr></c:forEach>
            </tbody></table></div></c:otherwise>
        </c:choose>
    </section>
</main>
</body>
</html>
