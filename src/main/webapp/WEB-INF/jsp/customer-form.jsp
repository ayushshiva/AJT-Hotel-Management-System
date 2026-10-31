<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:choose><c:when test="${editing}">Edit Customer</c:when><c:otherwise>Add Customer</c:otherwise></c:choose> · AJT Hotel</title>
    <style>
        *{box-sizing:border-box}body{margin:0;background:#f3f6fb;color:#172033;font:15px Arial,sans-serif}
        .top{background:#142d4e;color:#fff;padding:22px max(5%,calc((100% - 900px)/2))}.top h1{margin:0 0 8px;font-size:25px}.top a{color:#fff}
        main{max-width:900px;margin:28px auto;padding:0 18px}.card{background:#fff;border-radius:12px;padding:24px;box-shadow:0 3px 14px #152b4512}
        .grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:16px}.field label{display:block;font-weight:bold;margin:0 0 7px}
        input,select,textarea{width:100%;padding:11px;border:1px solid #cbd5e1;border-radius:7px;font:inherit}
        textarea{min-height:84px;resize:vertical}.wide{grid-column:1/-1}.help{font-size:12px;color:#64748b;margin-top:5px}
        .actions{display:flex;gap:10px;margin-top:22px}.button{display:inline-block;border:0;border-radius:7px;padding:11px 16px;background:#1765c1;color:#fff;text-decoration:none;font-weight:bold;cursor:pointer}
        .button.secondary{background:#e8eef6;color:#173b64}.error{background:#ffeded;color:#991b1b;padding:12px;border-radius:8px;margin-bottom:18px}
        @media(max-width:620px){.grid{grid-template-columns:1fr}.wide{grid-column:auto}.top{padding:20px}}
    </style>
</head>
<body>
<header class="top"><h1><c:choose><c:when test="${editing}">Edit Customer</c:when><c:otherwise>Register Customer</c:otherwise></c:choose></h1>
    <a href="${pageContext.request.contextPath}/customers">Customer Management</a> · Signed in as <c:out value="${loggedInUser}"/>
</header>
<main>
    <section class="card">
        <c:if test="${not empty error}"><div class="error"><c:out value="${error}"/></div></c:if>
        <form method="post" action="${pageContext.request.contextPath}/customers${editing ? '/' : ''}${editing ? customer.id : ''}">
            <div class="grid">
                <div class="field"><label for="fullName">Full name *</label><input id="fullName" name="fullName" value="<c:out value='${customer.fullName}'/>" maxlength="120" required></div>
                <div class="field"><label for="mobileNumber">Mobile number *</label><input id="mobileNumber" name="mobileNumber" type="tel" value="<c:out value='${customer.mobileNumber}'/>" pattern="[+()0-9 .-]{7,20}" maxlength="20" required><div class="help">Use 7–20 digits or phone punctuation.</div></div>
                <div class="field"><label for="emailAddress">Email address *</label><input id="emailAddress" name="emailAddress" type="email" value="<c:out value='${customer.emailAddress}'/>" maxlength="254" required></div>
                <div class="field"><label for="gender">Gender</label><select id="gender" name="gender"><option value="">Not specified</option>
                    <option value="Female" <c:if test="${customer.gender eq 'Female'}">selected</c:if>>Female</option>
                    <option value="Male" <c:if test="${customer.gender eq 'Male'}">selected</c:if>>Male</option>
                    <option value="Non-binary" <c:if test="${customer.gender eq 'Non-binary'}">selected</c:if>>Non-binary</option>
                    <option value="Prefer not to say" <c:if test="${customer.gender eq 'Prefer not to say'}">selected</c:if>>Prefer not to say</option>
                </select></div>
                <div class="field wide"><label for="address">Address</label><textarea id="address" name="address" maxlength="255"><c:out value="${customer.address}"/></textarea></div>
                <div class="field"><label for="city">City</label><input id="city" name="city" value="<c:out value='${customer.city}'/>" maxlength="100"></div>
                <div class="field"><label for="state">State / province</label><input id="state" name="state" value="<c:out value='${customer.state}'/>" maxlength="100"></div>
                <div class="field"><label for="country">Country</label><input id="country" name="country" value="<c:out value='${customer.country}'/>" maxlength="100"></div>
                <div class="field"><label for="idProofType">ID proof type</label><select id="idProofType" name="idProofType"><option value="">Not provided</option>
                    <option value="Passport" <c:if test="${customer.idProofType eq 'Passport'}">selected</c:if>>Passport</option>
                    <option value="National ID" <c:if test="${customer.idProofType eq 'National ID'}">selected</c:if>>National ID</option>
                    <option value="Driver license" <c:if test="${customer.idProofType eq 'Driver license'}">selected</c:if>>Driver license</option>
                    <option value="Other" <c:if test="${customer.idProofType eq 'Other'}">selected</c:if>>Other</option>
                </select></div>
                <div class="field"><label for="idProofNumber">ID proof number</label><input id="idProofNumber" name="idProofNumber" value="<c:out value='${customer.idProofNumber}'/>" maxlength="80" autocomplete="off"></div>
                <c:if test="${editing}">
                    <div class="field"><label for="status">Customer status</label><select id="status" name="status">
                        <option value="ACTIVE" <c:if test="${customer.status eq 'ACTIVE'}">selected</c:if>>Active</option>
                        <option value="INACTIVE" <c:if test="${customer.status eq 'INACTIVE'}">selected</c:if>>Inactive</option>
                    </select></div>
                    <div class="field"><label>Registration date</label><input value="${customer.registrationDate}" readonly></div>
                </c:if>
            </div>
            <div class="actions"><button class="button" type="submit"><c:choose><c:when test="${editing}">Save Changes</c:when><c:otherwise>Register Customer</c:otherwise></c:choose></button>
                <a class="button secondary" href="${pageContext.request.contextPath}/customers">Cancel</a></div>
        </form>
    </section>
</main>
</body>
</html>
