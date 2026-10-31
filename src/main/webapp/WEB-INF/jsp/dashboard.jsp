from pathlib import Path

content = r'''<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Hotel Management | Dashboard</title>
    <style>
        :root {
            --navy: #142b4a;
            --navy-2: #1d3b61;
            --blue: #3478f6;
            --blue-soft: #edf4ff;
            --ink: #17243a;
            --muted: #738198;
            --line: #e8edf4;
            --surface: #ffffff;
            --page: #f5f7fb;
            --green: #15966b;
            --green-soft: #e9f8f1;
            --shadow: 0 8px 28px rgba(25, 48, 79, .06);
        }
        * { box-sizing: border-box; }
        body {
            margin: 0;
            background: var(--page);
            color: var(--ink);
            font-family: Inter, "Segoe UI", Arial, sans-serif;
        }
        a { color: inherit; text-decoration: none; }
        .app { min-height: 100vh; display: flex; }
        .sidebar {
            width: 246px; flex: 0 0 246px; background: var(--navy);
            color: #dce7f6; padding: 25px 16px; display: flex; flex-direction: column;
        }
        .brand { display: flex; align-items: center; gap: 11px; padding: 0 10px 30px; }
        .brand-mark {
            width: 39px; height: 39px; border-radius: 12px; display: grid; place-items: center;
            background: #2f74e8; color: white; font-size: 21px;
        }
        .brand-name { color: white; font-size: 16px; font-weight: 750; letter-spacing: .2px; }
        .brand-sub { color: #9fb2cc; font-size: 10px; margin-top: 3px; letter-spacing: 1.1px; text-transform: uppercase; }
        .nav-label { padding: 0 12px; margin: 8px 0 10px; color: #8fa5c2; font-size: 10px; font-weight: 700; letter-spacing: 1.3px; text-transform: uppercase; }
        .nav { display: grid; gap: 5px; }
        .nav a {
            display: flex; align-items: center; gap: 12px; padding: 11px 12px;
            border-radius: 9px; color: #c8d6e9; font-size: 13px; font-weight: 550;
            transition: background .18s ease, color .18s ease;
        }
        .nav a:hover, .nav a.active { background: #24466f; color: #fff; }
        .nav-icon { width: 20px; text-align: center; font-size: 16px; }
        .sidebar-bottom { margin-top: auto; padding: 18px 10px 0; color: #91a8c5; font-size: 11px; line-height: 1.6; }
        .main { min-width: 0; flex: 1; }
        .topbar {
            min-height: 76px; padding: 15px 34px; background: #fff; border-bottom: 1px solid var(--line);
            display: flex; justify-content: space-between; align-items: center; gap: 18px;
        }
        .crumb { color: var(--muted); font-size: 12px; }
        .top-title { font-size: 17px; font-weight: 750; margin-top: 4px; }
        .user-area { display: flex; align-items: center; gap: 12px; }
        .avatar {
            width: 38px; height: 38px; border-radius: 50%; display: grid; place-items: center;
            background: var(--blue-soft); color: var(--blue); font-weight: 800; font-size: 13px;
        }
        .user-name { font-size: 12px; font-weight: 700; }
        .user-role { color: var(--muted); font-size: 10px; margin-top: 3px; }
        .logout { color: #61718a; font-size: 12px; font-weight: 650; padding: 9px 12px; border: 1px solid var(--line); border-radius: 8px; }
        .logout:hover { color: #d13e4c; border-color: #f0cbd0; background: #fff8f8; }
        .content { max-width: 1320px; margin: 0 auto; padding: 30px 34px 42px; }
        .welcome { display: flex; align-items: flex-end; justify-content: space-between; gap: 16px; margin-bottom: 23px; }
        h1 { margin: 0; font-size: 25px; letter-spacing: -.6px; }
        .welcome p { margin: 7px 0 0; color: var(--muted); font-size: 13px; }
        .date-chip { padding: 9px 13px; background: #fff; border: 1px solid var(--line); border-radius: 9px; color: #60718a; font-size: 11px; white-space: nowrap; }
        .alerts { margin-bottom: 16px; }
        .msg, .err { padding: 12px 15px; border-radius: 9px; font-size: 13px; }
        .msg { color: #126c4d; background: var(--green-soft); border: 1px solid #cceedd; }
        .err { color: #a52d3a; background: #fff0f1; border: 1px solid #f4d0d4; }
        .stats { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; margin-bottom: 22px; }
        .stat {
            background: var(--surface); border: 1px solid var(--line); border-radius: 13px;
            padding: 19px 20px; box-shadow: var(--shadow); min-height: 122px;
        }
        .stat-top { display: flex; justify-content: space-between; align-items: center; }
        .stat-label { color: var(--muted); font-size: 12px; font-weight: 650; }
        .stat-icon { width: 35px; height: 35px; border-radius: 10px; display: grid; place-items: center; font-size: 17px; }
        .icon-blue { color: #286de0; background: #edf4ff; }
        .icon-green { color: #13875f; background: #e9f8f1; }
        .icon-violet { color: #7554d6; background: #f1edff; }
        .stat-value { margin-top: 13px; font-size: 27px; font-weight: 800; letter-spacing: -.8px; }
        .stat-note { color: #94a0b1; font-size: 10px; margin-top: 3px; }
        .section-head { display: flex; justify-content: space-between; align-items: center; gap: 12px; margin: 26px 0 13px; }
        .section-head h2 { margin: 0; font-size: 15px; }
        .section-head span { color: var(--muted); font-size: 11px; }
        .panel { background: #fff; border: 1px solid var(--line); border-radius: 13px; padding: 20px; box-shadow: var(--shadow); }
        .module-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 13px; }
        .module {
            display: flex; align-items: center; gap: 12px; min-height: 74px; padding: 14px;
            border: 1px solid var(--line); border-radius: 11px; background: #fff;
            transition: transform .16s ease, border-color .16s ease, box-shadow .16s ease;
        }
        .module:hover { transform: translateY(-2px); border-color: #c9dcff; box-shadow: 0 8px 20px rgba(45, 105, 200, .09); }
        .module-icon { width: 39px; height: 39px; flex: 0 0 39px; display: grid; place-items: center; border-radius: 11px; background: var(--blue-soft); font-size: 18px; }
        .module-title { font-size: 12px; font-weight: 750; }
        .module-desc { color: var(--muted); font-size: 10px; margin-top: 4px; }
        .quick-note { display: flex; align-items: flex-start; gap: 12px; margin-top: 18px; padding: 15px 17px; border-radius: 11px; background: #f0f6ff; color: #355477; }
        .quick-note strong { display: block; font-size: 12px; margin-bottom: 4px; }
        .quick-note p { margin: 0; font-size: 11px; line-height: 1.55; color: #637b98; }
        @media (max-width: 1050px) {
            .module-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
        }
        @media (max-width: 800px) {
            .sidebar { width: 70px; flex-basis: 70px; padding: 18px 9px; }
            .brand { justify-content: center; padding: 0 0 25px; }
            .brand-copy, .nav-label, .nav-text, .sidebar-bottom { display: none; }
            .nav a { justify-content: center; padding: 12px 5px; }
            .nav-icon { font-size: 18px; }
            .topbar { padding: 13px 20px; }
            .content { padding: 24px 20px 34px; }
            .stats { grid-template-columns: repeat(2, minmax(0, 1fr)); }
            .module-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
        }
        @media (max-width: 520px) {
            .sidebar { width: 58px; flex-basis: 58px; }
            .topbar { padding: 12px 14px; }
            .user-details { display: none; }
            .content { padding: 20px 14px 28px; }
            .welcome { align-items: flex-start; flex-direction: column; }
            h1 { font-size: 22px; }
            .stats { grid-template-columns: 1fr; gap: 10px; }
            .stat { min-height: 105px; }
            .module-grid { grid-template-columns: 1fr 1fr; gap: 9px; }
            .module { padding: 11px 9px; gap: 8px; }
            .module-icon { width: 33px; height: 33px; flex-basis: 33px; font-size: 15px; }
            .module-title { font-size: 11px; }
            .module-desc { font-size: 9px; }
            .panel { padding: 13px; }
            .logout { padding: 8px; font-size: 11px; }
        }
    </style>
</head>
<body>
<div class="app">
    <aside class="sidebar">
        <div class="brand">
            <div class="brand-mark">⌂</div>
            <div class="brand-copy">
                <div class="brand-name">StaySphere</div>
                <div class="brand-sub">Hotel Management</div>
            </div>
        </div>

        <div class="nav-label">Main menu</div>
        <nav class="nav" aria-label="Main navigation">
            <a class="active" href="${pageContext.request.contextPath}/dashboard"><span class="nav-icon">▦</span><span class="nav-text">Dashboard</span></a>
            <a href="${pageContext.request.contextPath}/customers"><span class="nav-icon">♙</span><span class="nav-text">Customers</span></a>
            <a href="${pageContext.request.contextPath}/rooms"><span class="nav-icon">▣</span><span class="nav-text">Rooms</span></a>
            <a href="${pageContext.request.contextPath}/booking-management"><span class="nav-icon">▤</span><span class="nav-text">Date-based Bookings</span></a>
        </nav>

        <div class="nav-label" style="margin-top:25px">Hotel operations</div>
        <nav class="nav" aria-label="Hotel operations">
            <a href="${pageContext.request.contextPath}/food"><span class="nav-icon">♨</span><span class="nav-text">Food</span></a>
            <a href="${pageContext.request.contextPath}/laundry"><span class="nav-icon">♧</span><span class="nav-text">Laundry</span></a>
            <a href="${pageContext.request.contextPath}/room-service"><span class="nav-icon">☷</span><span class="nav-text">Room Service</span></a>
            <a href="${pageContext.request.contextPath}/employees"><span class="nav-icon">♟</span><span class="nav-text">Employees</span></a>
        </nav>

        <div class="nav-label" style="margin-top:25px">Management</div>
        <nav class="nav" aria-label="Management">
            <a href="${pageContext.request.contextPath}/billing"><span class="nav-icon">₹</span><span class="nav-text">Billing</span></a>
            <a href="${pageContext.request.contextPath}/reports"><span class="nav-icon">▥</span><span class="nav-text">Reports</span></a>
            <a href="${pageContext.request.contextPath}/legacy-status"><span class="nav-icon">⌘</span><span class="nav-text">Servlet Status</span></a>
        </nav>

        <div class="sidebar-bottom">AJT CSE Project<br>Hotel operations workspace</div>
    </aside>

    <main class="main">
        <header class="topbar">
            <div>
                <div class="crumb">Workspace / Overview</div>
                <div class="top-title">Dashboard</div>
            </div>
            <div class="user-area">
                <div class="avatar">HM</div>
                <div class="user-details">
                    <div class="user-name"><c:out value="${loggedInUser}"/></div>
                    <div class="user-role">Administrator</div>
                </div>
                <a class="logout" href="${pageContext.request.contextPath}/logout">Logout ↗</a>
            </div>
        </header>

        <div class="content">
            <div class="alerts">
                <c:if test="${not empty success}"><p class="msg"><c:out value="${success}"/></p></c:if>
                <c:if test="${not empty error}"><p class="err"><c:out value="${error}"/></p></c:if>
            </div>

            <section class="welcome">
                <div>
                    <h1>Good to see you 👋</h1>
                    <p>Here’s what’s happening at your hotel today.</p>
                </div>
                <div class="date-chip">✦ &nbsp; Hotel operations overview</div>
            </section>

            <section class="stats" aria-label="Hotel statistics">
                <article class="stat">
                    <div class="stat-top"><span class="stat-label">Active reservations</span><span class="stat-icon icon-blue">▤</span></div>
                    <div class="stat-value"><c:out value="${activeReservationCount}"/></div>
                    <div class="stat-note">Current date-based reservations</div>
                </article>
                <article class="stat">
                    <div class="stat-top"><span class="stat-label">Total rooms</span><span class="stat-icon icon-violet">▣</span></div>
                    <div class="stat-value"><c:out value="${roomCount}"/></div>
                    <div class="stat-note">Rooms registered in the system</div>
                </article>
                <article class="stat">
                    <div class="stat-top"><span class="stat-label">Available rooms</span><span class="stat-icon icon-green">✓</span></div>
                    <div class="stat-value"><c:out value="${availableRoomCount}"/></div>
                    <div class="stat-note">Ready for guest allocation</div>
                </article>
            </section>

            <div class="section-head">
                <h2>Hotel management</h2>
                <span>Choose a module to get started</span>
            </div>
            <section class="panel">
                <div class="module-grid">
                    <a class="module" href="${pageContext.request.contextPath}/customers"><span class="module-icon">♙</span><span><span class="module-title">Customers</span><span class="module-desc">Guest information</span></span></a>
                    <a class="module" href="${pageContext.request.contextPath}/rooms"><span class="module-icon">▣</span><span><span class="module-title">Rooms</span><span class="module-desc">Room inventory</span></span></a>
                    <a class="module" href="${pageContext.request.contextPath}/booking-management"><span class="module-icon">▤</span><span><span class="module-title">Bookings</span><span class="module-desc">Manage reservations</span></span></a>
                    <a class="module" href="${pageContext.request.contextPath}/food"><span class="module-icon">♨</span><span><span class="module-title">Food</span><span class="module-desc">Food orders</span></span></a>
                    <a class="module" href="${pageContext.request.contextPath}/laundry"><span class="module-icon">♧</span><span><span class="module-title">Laundry</span><span class="module-desc">Laundry requests</span></span></a>
                    <a class="module" href="${pageContext.request.contextPath}/room-service"><span class="module-icon">☷</span><span><span class="module-title">Room Service</span><span class="module-desc">Guest requests</span></span></a>
                    <a class="module" href="${pageContext.request.contextPath}/employees"><span class="module-icon">♟</span><span><span class="module-title">Employees</span><span class="module-desc">Staff management</span></span></a>
                    <a class="module" href="${pageContext.request.contextPath}/billing"><span class="module-icon">₹</span><span><span class="module-title">Billing</span><span class="module-desc">Invoices & payments</span></span></a>
                    <a class="module" href="${pageContext.request.contextPath}/reports"><span class="module-icon">▥</span><span><span class="module-title">Reports</span><span class="module-desc">Hotel summaries</span></span></a>
                    <a class="module" href="${pageContext.request.contextPath}/legacy-status"><span class="module-icon">⌘</span><span><span class="module-title">Servlet Status</span><span class="module-desc">System information</span></span></a>
                </div>
                <div class="quick-note">
                    <span>💡</span>
                    <div><strong>Quick tip</strong><p>Use Date-based Bookings to manage guest reservations and check room availability. Use Billing to review the billing module.</p></div>
                </div>
            </section>
        </div>
    </main>
</div>
</body>
</html>
'''

path = Path("/mnt/data/dashboard.jsp")
path.write_text(content, encoding="utf-8")
print(f"Created {path} ({path.stat().st_size:,} bytes)")
