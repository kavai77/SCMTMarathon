<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ page import="net.himadri.scmt.server.MarathonServiceImpl" %>

<%
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
%>

<html>
<head>
    <title>Oklevél feltöltés</title>
</head>
<body>
<form action="<%= blobstoreService.createUploadUrl("/") %>" method="post" enctype="multipart/form-data">
    <%pageContext.setAttribute("listOfVersenyek", new MarathonServiceImpl().getVersenyek());%>
    <select name="versenyek">
        <c:forEach var="option" items="${listOfVersenyek}" >
            <option value="<c:out value="${option.id}"/>"><c:out value="${option.nev}"/></option>
        </c:forEach>
    </select>
    <input type="submit" value="Submit">
</form>
</body>
</html>