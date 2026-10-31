<%@ page contentType="text/html;charset=UTF-8" language="java" %><%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Booking · AJT Hotel</title><style>body{margin:0;background:#f3f6fb;color:#172033;font:15px Arial}.top{background:#142d4e;color:white;padding:22px 5%}.top a{color:white}main{max-width:850px;margin:28px auto;padding:0 18px}.card{background:white;border-radius:12px;padding:24px}.grid{display:grid;grid-template-columns:1fr 1fr;gap:15px}.field label{display:block;font-weight:bold;margin-bottom:6px}.field input,.field select{width:100%;padding:11px;border:1px solid #cbd5e1;border-radius:7px;font:inherit}.wide{grid-column:1/-1}.button{display:inline-block;background:#1765c1;color:white;border:0;border-radius:7px;padding:11px 15px;text-decoration:none;cursor:pointer}.error{background:#ffeded;color:#991b1b;padding:12px}@media(max-width:620px){.grid{grid-template-columns:1fr}.wide{grid-column:auto}}</style></head><body>
<header class="top"><h1><c:choose><c:when test="${editing}">Edit Booking</c:when><c:otherwise>Create Booking</c:otherwise></c:choose></h1><a href="${pageContext.request.contextPath}/booking-management">Booking Management</a></header><main><section class="card">
<c:if test="${not empty error}"><p class="error"><c:out value="${error}"/></p></c:if><form method="post" action="${pageContext.request.contextPath}/booking-management${editing ? '/' : ''}${editing ? reservation.id : ''}"><div class="grid">
<div class="field"><label for="customerId">Customer *</label><select id="customerId" name="customerId" required><option value="">Select customer</option><c:forEach items="${customers}" var="c"><option value="${c.id}" <c:if test="${reservation.customerId eq c.id}">selected</c:if>><c:out value="${c.fullName}"/> · <c:out value="${c.mobileNumber}"/></option></c:forEach></select></div>
<div class="field"><label for="roomId">Room *</label><select id="roomId" name="roomId" required><option value="">Select room</option><c:forEach items="${rooms}" var="r"><option value="${r.id}" <c:if test="${reservation.roomId eq r.id}">selected</c:if>><c:out value="${r.roomNumber}"/> · <c:out value="${r.roomType}"/> · ${r.pricePerNight} / night · <c:out value="${r.status}"/></option></c:forEach></select></div>
<div class="field"><label for="checkIn">Check-in *</label><input id="checkIn" name="checkIn" type="date" value="${reservation.checkIn}" required></div>
<div class="field"><label for="checkOut">Check-out *</label><input id="checkOut" name="checkOut" type="date" value="${reservation.checkOut}" required></div>
<div class="wide"><button class="button" type="button" id="checkAvailability">Check room availability</button> <span id="availabilityResult" aria-live="polite"></span><p>The room rate is copied from the room record when you save; overlapping RESERVED or CHECKED_IN stays are rejected.</p></div></div><p><button class="button">Save Booking</button> <a class="button" href="${pageContext.request.contextPath}/booking-management">Cancel</a></p></form></section><script>
document.getElementById('checkAvailability').addEventListener('click', async function () {
    const roomId = document.getElementById('roomId').value;
    const checkIn = document.getElementById('checkIn').value;
    const checkOut = document.getElementById('checkOut').value;
    const result = document.getElementById('availabilityResult');
    if (!roomId || !checkIn || !checkOut) { result.textContent = 'Select a room and both dates first.'; return; }
    const query = new URLSearchParams({roomId, checkIn, checkOut});
    const response = await fetch('${pageContext.request.contextPath}/booking-management/availability?' + query);
    const data = await response.json();
    result.textContent = data.message;
});
</script></main></body></html>
