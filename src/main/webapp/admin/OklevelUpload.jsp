<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>

<%
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
%>

<html>
<head>
    <title>Oklevél feltöltés</title>
</head>
<body>
<form action="<%= blobstoreService.createUploadUrl("/scmtmarathon/OklevelUploadHandler") %>" method="post" enctype="multipart/form-data">
    <jsp:useBean id="marathonService" class="net.himadri.scmt.server.MarathonServiceImpl" />
    <p><select name="versenyId">
        <c:forEach var="option" items="${marathonService.versenyek}" >
            <jsp:useBean id="dateValue" class="java.util.Date" />
            <jsp:setProperty name="dateValue" property="time" value="${option.raceStartTime}" />
            <option value="${option.id}">${option.nev} <fmt:formatDate value="${dateValue}" pattern="yyyy-MM-dd" /></option>
        </c:forEach>
    </select>
    <p><input type="file" name="pdfFile">
    <p><input type="submit" value="Feltöltés">
</form>
</body>
</html>