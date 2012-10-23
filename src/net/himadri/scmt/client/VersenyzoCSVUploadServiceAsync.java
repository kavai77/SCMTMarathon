package net.himadri.scmt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

public interface VersenyzoCSVUploadServiceAsync {
    void importVersenyzok(long versenyId, String fileContent, AsyncCallback<String> async);
}
