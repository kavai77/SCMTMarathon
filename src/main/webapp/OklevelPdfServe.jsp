<%@ page import="com.google.appengine.api.blobstore.BlobKey" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ page import="com.googlecode.objectify.Objectify" %>
<%@ page import="net.himadri.scmt.client.entity.OklevelPdfBlob" %>
<%@ page import="net.himadri.scmt.server.ObjectifyUtils" %>

<%
    Long versenyId = Long.parseLong(request.getParameter("versenyId"));
    Objectify ofy = ObjectifyUtils.beginObjectify();
    OklevelPdfBlob oklevelPdfBlob = ofy.query(OklevelPdfBlob.class).filter("versenyId", versenyId).get();
    if (oklevelPdfBlob != null) {
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        BlobKey blobKey = new BlobKey(oklevelPdfBlob.getUploadedPdfBlobKey());
        response.setContentType("application/pdf");
        blobstoreService.serve(blobKey, response);
    } else {
        response.getWriter().println("Nincs oklevél feltöltve ehhez a versenyhez");
    }
%>
