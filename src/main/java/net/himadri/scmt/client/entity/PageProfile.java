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

    @SuppressWarnings({ "UnusedDeclaration" })
    public PageProfile()
    {
    }

    public PageProfile(PageProfileId pageProfileId)
    {
        id = pageProfileId.name();
    }

    public PageProfile(PageProfileId pageProfileId, float xAxis, float yAxis, int alignment, String fontFamily, int size)
    {
        id = pageProfileId.name();
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.alignment = alignment;
        this.fontFamily = fontFamily;
        this.size = size;
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
}
