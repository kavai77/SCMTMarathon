package net.himadri.scmt.client.callback;

import com.google.gwt.user.client.rpc.AsyncCallback;
import net.himadri.scmt.client.SCMTMarathon;

/**
* Created by Kávai on 2015.08.10..
*/
public abstract class CommonAsyncCallback<T> implements AsyncCallback<T> {
    @Override
    public void onFailure(Throwable throwable) {
        SCMTMarathon.commonFailureHandling(throwable);
    }
}
