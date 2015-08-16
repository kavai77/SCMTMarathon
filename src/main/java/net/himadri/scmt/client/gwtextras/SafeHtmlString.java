package net.himadri.scmt.client.gwtextras;

import com.google.gwt.safehtml.shared.SafeHtml;

public class SafeHtmlString implements SafeHtml {
    private String html;

    public SafeHtmlString(String html) {
        if(html == null) {
            throw new NullPointerException("html is null");
        } else {
            this.html = html;
        }
    }

    public SafeHtmlString() {
    }

    public String asString() {
        return this.html;
    }
}
