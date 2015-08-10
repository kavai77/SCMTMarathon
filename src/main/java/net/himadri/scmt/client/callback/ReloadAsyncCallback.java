package net.himadri.scmt.client.callback;

import com.google.gwt.user.client.Window;

/**
* Created by Kávai on 2015.08.10..
*/
public class ReloadAsyncCallback extends CommonAsyncCallback<Void> {
    @Override
    public void onSuccess(Void aVoid) {
        Window.Location.reload();
    }
}
