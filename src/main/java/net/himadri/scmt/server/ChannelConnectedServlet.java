package net.himadri.scmt.server;

import com.google.appengine.api.channel.ChannelPresence;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.googlecode.objectify.Objectify;
import net.himadri.scmt.client.entity.ClientChannel;

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
    private static Objectify ofy = ObjectifyUtils.beginObjectify();
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ChannelService channelService = ChannelServiceFactory.getChannelService();
        ChannelPresence presence = channelService.parsePresence(request);
        long clientId = Long.parseLong(presence.clientId());
        ClientChannel channel = ofy.get(ClientChannel.class, clientId);
        if (presence.isConnected()) {
            if (!channel.isConnected()) {
                channel.setConnected(true);
                ofy.put(channel);
            }
        } else {
            if (channel.isConnected()) {
                channel.setConnected(false);
                ofy.put(channel);
            }
        }
    }
}
