package net.himadri.scmt.client.entity;

import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.06.02. 14:29
 */
public class PageProfile implements Serializable {
    @Id
    private String id;
    private float xAxis, yAxis;
    private int alignment;
    private String fontFamily;
    private int size;

    @SuppressWarnings({"UnusedDeclaration"})
    public PageProfile() {
    }

    public PageProfile(PageProfileId pageProfileId) {
        id = pageProfileId.name();
    }

    public String getId() {
        return id;
    }

    public float getxAxis() {
        return xAxis;
    }

    public void setxAxis(float xAxis) {
        this.xAxis = xAxis;
    }

    public float getyAxis() {
        return yAxis;
    }

    public void setyAxis(float yAxis) {
        this.yAxis = yAxis;
    }

    public int getAlignment() {
        return alignment;
    }

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
