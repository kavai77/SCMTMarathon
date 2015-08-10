package net.himadri.scmt.client.entity;

import com.google.gwt.user.client.rpc.IsSerializable;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.06.02. 14:29
 */
@Entity public class PageProfile implements IsSerializable
{
    private int alignment;
    private String fontFamily;
    @Id private String id;
    private int size;
    private float xAxis, yAxis;
    private boolean printProfile;

    @SuppressWarnings({ "UnusedDeclaration" })
    public PageProfile()
    {
    }

    public PageProfile(String id, int alignment, String fontFamily, int size, float xAxis, float yAxis, boolean printProfile) {
        this.id = id;
        this.alignment = alignment;
        this.fontFamily = fontFamily;
        this.size = size;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.printProfile = printProfile;
    }

    public int getAlignment()
    {
        return alignment;
    }

    public void setAlignment(int alignment)
    {
        this.alignment = alignment;
    }

    public String getFontFamily()
    {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily)
    {
        this.fontFamily = fontFamily;
    }

    public String getId()
    {
        return id;
    }

    public int getSize()
    {
        return size;
    }

    public void setSize(int size)
    {
        this.size = size;
    }

    public float getxAxis()
    {
        return xAxis;
    }

    public float getyAxis()
    {
        return yAxis;
    }

    public void setxAxis(float xAxis)
    {
        this.xAxis = xAxis;
    }

    public void setyAxis(float yAxis)
    {
        this.yAxis = yAxis;
    }

    public boolean isPrintProfile() {
        return printProfile;
    }

    public void setPrintProfile(boolean printProfile) {
        this.printProfile = printProfile;
    }
}
