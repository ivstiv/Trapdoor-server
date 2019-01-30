package commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import communication.AbstractHandler;
import communication.Request;
import communication.RequestHandler;
import communication.RequestType;
import core.ServerWrapper;
import core.ServiceLocator;
import data.Channel;

public class ChannelsCommand implements CommandExecutor{

    private static ServerWrapper server = ServiceLocator.getService(ServerWrapper.class);

    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        if(sender instanceof RequestHandler) {

            RequestHandler client = (RequestHandler) sender;

            StringBuilder channelsText = new StringBuilder("&1&dAvailable channels:");
            for(Channel ch : server.getChannels()) {
                String entry = String.format("\n&g   - [%s] %s %d", ch.getName(), ch.getType().toString(), ch.getClients().size());
                channelsText.append(entry);
            }

            JsonObject payload = new JsonObject();
            payload.addProperty("action", "print");
            payload.addProperty("message", channelsText.toString());
            Request response = new Request(RequestType.ACTION, payload);
            client.sendRequest(response);
        }
        // TODO: 20-Nov-18 add options for ConsoleSender later
    }
}
