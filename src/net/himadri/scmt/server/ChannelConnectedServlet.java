package net.himadri.scmt.server;

import com.google.appengine.api.channel.ChannelPresence;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.19. 22:19
 */
public class ChannelConnectedServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ChannelService channelService = ChannelServiceFactory.getChannelService();
        ChannelPresence presence = channelService.parsePresence(request);
        long clientId = Long.parseLong(presence.clientId());
        if (presence.isConnected()) {
            MarathonServiceImpl.channelIdSet.add(clientId);
        } else {
            MarathonServiceImpl.channelIdSet.remove(clientId);
        }
        MarathonServiceImpl.pendingChannelIdSet.remove(clientId);
    }
}
