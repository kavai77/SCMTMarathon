package net.himadri.scmt.client;

public class LocaleCollator {

    public static final LocaleCollator getInstance() {
        return instance;
    }

    private static final LocaleCollator instance = new LocaleCollator();

    public native int compare( String source, String target ) /*-{
         return source.localeCompare( target );
    }-*/;
}
