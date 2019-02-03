package commands.implementations;

import com.google.gson.JsonObject;
import commands.CommandExecutor;
import commands.CommandSender;
import communication.Request;
import communication.ConnectionRequestHandler;
import communication.RequestType;
import core.Console;
import core.ServerWrapper;
import core.ServiceLocator;
import data.Channel;
import data.DataLoader;

public class ChannelsCommand implements CommandExecutor {

    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        ServerWrapper server = ServiceLocator.getService(ServerWrapper.class);
        DataLoader dl = ServiceLocator.getService(DataLoader.class);


        if(sender instanceof ConnectionRequestHandler) {
            ConnectionRequestHandler client = (ConnectionRequestHandler) sender;

            StringBuilder channelsText = new StringBuilder(dl.getMessage("available-channels"));
            for(Channel ch : server.getChannels()) {
                String entry = String.format("\n~g   - [%s] %s %d", ch.getName(), ch.getType().toString(), ch.getClients().size());
                channelsText.append(entry);
            }

            JsonObject payload = new JsonObject();
            payload.addProperty("action", "print");
            payload.addProperty("message", channelsText.toString());
            Request response = new Request(RequestType.ACTION, payload);
            client.sendRequest(response);
            return;
        }

        if(sender instanceof Console) {
            StringBuilder channelsText = new StringBuilder(dl.getMessage("cl-available-channels"));
            for(Channel ch : server.getChannels()) {
                String entry = String.format("\n   - [%s] %s %d", ch.getName(), ch.getType().toString(), ch.getClients().size());
                channelsText.append(entry);
            }
            server.getConsole().print(channelsText.toString());
        }
    }
}
