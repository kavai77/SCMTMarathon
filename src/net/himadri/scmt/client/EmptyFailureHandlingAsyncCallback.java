package net.himadri.scmt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.09.01. 17:27
 */
public class EmptyFailureHandlingAsyncCallback<T> implements AsyncCallback<T> {
    @Override
    public void onFailure(Throwable throwable) {
            SCMTMarathon.commonFailureHandling(throwable);
    }

    @Override
    public void onSuccess(T aVoid) {
    }
}
