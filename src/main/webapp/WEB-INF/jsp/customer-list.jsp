<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Customer Management · AJT Hotel</title>
    <style>
        *{box-sizing:border-box}body{margin:0;background:#f3f6fb;color:#172033;font:15px Arial,sans-serif}
        .top{background:#142d4e;color:#fff;padding:22px max(5%,calc((100% - 1120px)/2))}
        .top h1{margin:0 0 8px;font-size:25px}.top a{color:#fff}.wrap{max-width:1120px;margin:28px auto;padding:0 18px}
        .bar,.card{background:#fff;border-radius:12px;padding:20px;margin-bottom:18px;box-shadow:0 3px 14px #152b4512}
        .bar{display:flex;justify-content:space-between;align-items:center;gap:14px;flex-wrap:wrap}
        .bar h2{margin:0}.button{display:inline-block;background:#1765c1;color:#fff;border:0;border-radius:7px;padding:11px 15px;text-decoration:none;font-weight:bold}
        .button.secondary{background:#e8eef6;color:#173b64}.stats{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:15px}
        .stat{background:#fff;border-radius:12px;padding:18px;box-shadow:0 3px 14px #152b4512}.stat strong{display:block;color:#1765c1;font-size:27px;margin-top:8px}
        form.search{display:flex;gap:9px;flex-wrap:wrap}input,select{padding:10px;border:1px solid #ccd5e0;border-radius:7px;font:inherit}
        input[name=q]{flex:1;min-width:220px}table{width:100%;border-collapse:collapse}th,td{text-align:left;padding:12px;border-bottom:1px solid #e7ebf0}
        th{font-size:13px;color:#526174}td.actions{white-space:nowrap}td a{color:#1765c1;margin-right:8px}
        .badge{display:inline-block;border-radius:20px;padding:4px 9px;background:#e4f7e9;color:#17613b;font-size:12px}
        .badge.inactive{background:#eef1f5;color:#596579}.notice{padding:12px 14px;border-radius:8px;margin-bottom:16px}
        .success{background:#e4f7e9;color:#17613b}.error{background:#ffeded;color:#991b1b}.empty{padding:28px;text-align:center;color:#65748b}
        .recent{display:flex;gap:10px;flex-wrap:wrap}.recent a{color:#1765c1;text-decoration:none;background:#f3f6fb;padding:8px 11px;border-radius:20px}
        .pagination{display:flex;justify-content:center;gap:12px;align-items:center;padding-top:16px}.pagination a{color:#1765c1}
        @media(max-width:700px){.stats{grid-template-columns:1fr}.table-wrap{overflow-x:auto}table{min-width:680px}.top{padding:20px}}
    </style>
</head>
<body>
<header class="top">
    <h1>Customer Management</h1>
    AJT Hotel Management · <a href="${pageContext.request.contextPath}/dashboard">Dashboard</a>
    <span style="float:right">Welcome, <c:out value="${loggedInUser}"/> · <a href="${pageContext.request.contextPath}/logout">Logout</a></span>
</header>
<main class="wrap">
    <c:if test="${not empty success}"><div class="notice success"><c:out value="${success}"/></div></c:if>
    <c:if test="${not empty error}"><div class="notice error"><c:out value="${error}"/></div></c:if>
    <section class="bar"><h2>Customer records</h2><a class="button" href="${pageContext.request.contextPath}/customers/new">Add New Customer</a></section>
    <section class="stats">
        <div class="stat">Total customers<strong>${totalCustomers}</strong></div>
        <div class="stat">Active customers<strong>${activeCustomers}</strong></div>
        <div class="stat">Matching records<strong>${filteredCount}</strong></div>
    </section>
    <section class="card">
        <h2>Recently added customers</h2>
        <c:choose>
            <c:when test="${empty recentCustomers}"><p class="empty">No customer records yet.</p></c:when>
            <c:otherwise><div class="recent"><c:forEach items="${recentCustomers}" var="recent"><a href="${pageContext.request.contextPath}/customers/${recent.id}"><c:out value="${recent.fullName}"/> · ${recent.registrationDate}</a></c:forEach></div></c:otherwise>
        </c:choose>
    </section>
    <section class="card">
        <form class="search" method="get" action="${pageContext.request.contextPath}/customers">
            <input name="q" value="<c:out value='${search}'/>" placeholder="Search name, mobile number or email" aria-label="Search customers">
            <select name="status" aria-label="Filter by status">
                <option value="">All statuses</option>
                <option value="ACTIVE" <c:if test="${statusFilter eq 'ACTIVE'}">selected</c:if>>Active</option>
                <option value="INACTIVE" <c:if test="${statusFilter eq 'INACTIVE'}">selected</c:if>>Inactive</option>
            </select>
            <button class="button" type="submit">Search</button>
            <a class="button secondary" href="${pageContext.request.contextPath}/customers">Clear</a>
        </form>
    </section>
    <section class="card">
        <div class="table-wrap">
            <table>
                <thead><tr><th>Customer</th><th>Mobile</th><th>Email</th><th>Registered</th><th>Status</th><th>Actions</th></tr></thead>
                <tbody>
                <c:choose>
                    <c:when test="${empty customers}"><tr><td class="empty" colspan="6">No customers match your search.</td></tr></c:when>
                    <c:otherwise>
                        <c:forEach items="${customers}" var="customer">
                            <tr>
                                <td><c:out value="${customer.fullName}"/></td>
                                <td><c:out value="${customer.mobileNumber}"/></td>
                                <td><c:out value="${customer.emailAddress}"/></td>
                                <td>${customer.registrationDate}</td>
                                <td><span class="badge ${customer.status eq 'INACTIVE' ? 'inactive' : ''}"><c:out value="${customer.status}"/></span></td>
                                <td class="actions"><a href="${pageContext.request.contextPath}/customers/${customer.id}">View</a><a href="${pageContext.request.contextPath}/customers/${customer.id}/edit">Edit</a></td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>
        <c:if test="${pageCount gt 1}">
            <c:url var="previousPageUrl" value="/customers">
                <c:param name="q" value="${search}"/><c:param name="status" value="${statusFilter}"/><c:param name="page" value="${page - 1}"/>
            </c:url>
            <c:url var="nextPageUrl" value="/customers">
                <c:param name="q" value="${search}"/><c:param name="status" value="${statusFilter}"/><c:param name="page" value="${page + 1}"/>
            </c:url>
            <nav class="pagination" aria-label="Customer pages">
                <c:if test="${page gt 0}"><a href="${previousPageUrl}">Previous</a></c:if>
                <span>Page ${page + 1} of ${pageCount}</span>
                <c:if test="${page + 1 lt pageCount}"><a href="${nextPageUrl}">Next</a></c:if>
            </nav>
        </c:if>
    </section>
</main>
</body>
</html>
