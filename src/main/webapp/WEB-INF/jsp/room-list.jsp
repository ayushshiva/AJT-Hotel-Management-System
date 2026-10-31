<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Rooms · AJT Hotel</title><style>
*{box-sizing:border-box}body{margin:0;background:#f3f6fb;color:#172033;font:15px Arial,sans-serif}.top{background:#142d4e;color:white;padding:22px max(5%,calc((100% - 1120px)/2))}.top a{color:white}
main{max-width:1120px;margin:28px auto;padding:0 18px}.card{background:white;border-radius:12px;padding:20px;margin:16px 0;box-shadow:0 3px 14px #152b4512}
.bar{display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap}.button{display:inline-block;background:#1765c1;color:white;border:0;border-radius:7px;padding:10px 14px;text-decoration:none;font-weight:bold;cursor:pointer}
input,select{padding:10px;border:1px solid #ccd5e0;border-radius:7px;font:inherit}table{width:100%;border-collapse:collapse}th,td{text-align:left;padding:11px;border-bottom:1px solid #e7ebf0}.actions{white-space:nowrap}.actions a{margin-right:8px;color:#1765c1}
.notice{padding:12px;border-radius:8px;margin:12px 0}.error{background:#ffeded;color:#991b1b}.success{background:#e4f7e9;color:#17613b}.badge{background:#e8eef6;padding:4px 8px;border-radius:12px}
@media(max-width:700px){.table-wrap{overflow:auto}table{min-width:800px}.top{padding:20px}}
</style></head><body>
<header class="top"><h1>Room Management</h1><a href="${pageContext.request.contextPath}/dashboard">Dashboard</a> · <a href="${pageContext.request.contextPath}/booking-management">Bookings</a> · Welcome, <c:out value="${loggedInUser}"/> · <a href="${pageContext.request.contextPath}/logout">Logout</a></header>
<main><c:if test="${not empty success}"><div class="notice success"><c:out value="${success}"/></div></c:if><c:if test="${not empty error}"><div class="notice error"><c:out value="${error}"/></div></c:if>
<section class="card bar"><div><h2>Rooms</h2><p>${totalRooms} total · ${availableRooms} available</p></div><a class="button" href="${pageContext.request.contextPath}/rooms/new">Add Room</a></section>
<section class="card"><form method="get" action="${pageContext.request.contextPath}/rooms">
<input name="q" value="<c:out value='${search}'/>" placeholder="Search number, description, amenities">
<select name="type"><option value="">All types</option><c:forEach items="${['Single','Double','Deluxe','Suite']}" var="item"><option value="${item}" <c:if test="${typeFilter eq item}">selected</c:if>><c:out value="${item}"/></option></c:forEach></select>
<select name="status"><option value="">All statuses</option><c:forEach items="${['AVAILABLE','RESERVED','OCCUPIED','MAINTENANCE']}" var="item"><option value="${item}" <c:if test="${statusFilter eq item}">selected</c:if>><c:out value="${item}"/></option></c:forEach></select>
<button class="button">Filter</button></form></section>
<section class="card table-wrap"><table><thead><tr><th>Room</th><th>Type</th><th>Floor</th><th>Capacity</th><th>Rate/night</th><th>Status</th><th>Actions</th></tr></thead><tbody>
<c:choose><c:when test="${empty rooms}"><tr><td colspan="7">No rooms match the selected filters.</td></tr></c:when><c:otherwise><c:forEach items="${rooms}" var="room"><tr>
<td><c:out value="${room.roomNumber}"/></td><td><c:out value="${room.roomType}"/></td><td>${room.floorNumber}</td><td>${room.capacity}</td><td>${room.pricePerNight}</td><td><span class="badge"><c:out value="${room.status}"/></span></td>
<td class="actions"><a href="${pageContext.request.contextPath}/rooms/${room.id}">View</a><a href="${pageContext.request.contextPath}/rooms/${room.id}/edit">Edit</a>
<form style="display:inline" method="post" action="${pageContext.request.contextPath}/rooms/${room.id}/delete" onsubmit="return confirm('Delete this room?');"><button class="button" type="submit">Delete</button></form></td></tr></c:forEach></c:otherwise></c:choose>
</tbody></table></section></main></body></html>
