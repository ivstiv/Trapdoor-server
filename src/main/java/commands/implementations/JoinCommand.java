package commands.implementations;

import com.google.gson.JsonObject;
import commands.CommandExecutor;
import commands.CommandSender;
import communication.Request;
import communication.RequestHandler;
import communication.RequestType;
import core.ServerWrapper;
import core.ServiceLocator;
import data.Channel;
import data.ChannelType;
import data.DataLoader;

public class JoinCommand implements CommandExecutor {

    private ServerWrapper server = ServiceLocator.getService(ServerWrapper.class);
    private DataLoader dl = ServiceLocator.getService(DataLoader.class);

    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        if(sender instanceof RequestHandler) {
            RequestHandler client = (RequestHandler) sender;

            // check if argument exists
            if(args.length < 1) {
                client.sendServerMessage(dl.getMessage("missing-argument"));
                return;
            }

            // check if channel exists
            boolean exists = server.getChannels()
                    .parallelStream()
                    .anyMatch(channel -> channel.getName().equals(args[0]));

            if(exists) {
                Channel newChannel = server.getChannel(args[0]);
                // check if channel is private and if yes check for password argument
                if(newChannel.getType() == ChannelType.PRIVATE) {
                    //check if password argument exists
                    if(args.length > 1) {
                        // check if the supplied password matches the defined password in the config
                        if(newChannel.getPassword().equals(args[1])) {
                            switchChannel(client, newChannel);
                        }else{
                            client.sendServerMessage(dl.getMessage("wrong-pass"));
                            return;
                        }
                    }else{
                        client.sendServerMessage(dl.getMessage("missing-argument"));
                        return;
                    }
                }else{
                    switchChannel(client, newChannel);
                }
            }else{
                client.sendServerMessage(dl.getMessage("unknown-channel"));
                return;
            }
        }
    }

    // when we have multiple command senders just overload the method
    private void switchChannel(RequestHandler client, Channel newChannel) {
        Channel oldChannel = client.getActiveChannel();

        // check if the user is not already in this channel
        if(oldChannel.getName().equals(newChannel.getName())) {
            client.sendServerMessage(dl.getMessage("already-in"));
            return;
        }

        // switch the channel
        oldChannel.removeClient(client.getUsername());
        newChannel.addClient(client);
        client.setActiveChannel(newChannel);

        // update the status bar
        JsonObject payload = new JsonObject();
        payload.addProperty("action", "update_statusbar");
        payload.addProperty("channel", newChannel.getName());
        Request statusBar = new Request(RequestType.ACTION, payload);
        client.sendRequest(statusBar);

        // broadcast to everyone in the old channel
        String msg = String.format("%s%s %s",
                dl.getMessage("prefix"), client.getUsername(), dl.getMessage("left-channel"));
        oldChannel.broadcastPrint(msg);

        //broadcast to everyone in the new channel
        msg = String.format("%s%s %s",
                dl.getMessage("prefix"), client.getUsername(), dl.getMessage("join-channel"));
        newChannel.broadcastPrint(msg);

    }
}
