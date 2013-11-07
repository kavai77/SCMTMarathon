package net.himadri.scmt.client.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class OklevelPdfBlob {
    @Id
    private Long id;
    private Long versenyId;
    private String uploadedPdfBlobKey;
    private Long size;

    public OklevelPdfBlob() {
    }

    public OklevelPdfBlob(Long versenyId, String uploadedPdfBlobKey, Long size) {
        this.versenyId = versenyId;
        this.uploadedPdfBlobKey = uploadedPdfBlobKey;
        this.size = size;
    }

    public Long getId() {
        return id;
    }

    public Long getVersenyId() {
        return versenyId;
    }

    public String getUploadedPdfBlobKey() {
        return uploadedPdfBlobKey;
    }

    public Long getSize() {
        return size;
    }
}
