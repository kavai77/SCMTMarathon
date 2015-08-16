package net.himadri.scmt.client.gwtextras;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.02.24. 8:16
 */
public class ImageButton extends Button {
    public ImageButton(String imageFile, String text) {
        super(getImgHtml(imageFile, text));
    }

    public ImageButton(String imageFile, String text, ClickHandler clickHandler) {
        super(getImgHtml(imageFile, text), clickHandler);
    }

    private static String getImgHtml(String imageFile, String text) {
        return "<img src='images/" + imageFile + "' style='vertical-align: middle;' width='22' height='22'> " + text;
    }
}
