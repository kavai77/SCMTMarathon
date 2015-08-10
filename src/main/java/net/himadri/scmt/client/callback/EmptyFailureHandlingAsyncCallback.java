package net.himadri.scmt.client.callback;

import net.himadri.scmt.client.SCMTMarathon;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.09.01. 17:27
 */
public class EmptyFailureHandlingAsyncCallback<T> extends CommonAsyncCallback<T> {
    @Override
    public void onFailure(Throwable throwable) {
            SCMTMarathon.commonFailureHandling(throwable);
    }

    @Override
    public void onSuccess(T aVoid) {
    }
}
