package net.himadri.scmt.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("import")
public interface VersenyzoCSVUploadService extends RemoteService {
    String getHeader();

    String importVersenyzok(long versenyId, String fileContent);
}
