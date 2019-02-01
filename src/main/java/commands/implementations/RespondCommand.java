package commands.implementations;

import com.google.gson.JsonObject;
import commands.CommandExecutor;
import commands.CommandSender;
import communication.ConnectionRequestHandler;
import communication.Request;
import communication.RequestType;
import core.ServerWrapper;
import core.ServiceLocator;
import data.DataLoader;

public class RespondCommand implements CommandExecutor {

    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        if (sender instanceof ConnectionRequestHandler) {

            ConnectionRequestHandler client = (ConnectionRequestHandler) sender;

            ServerWrapper server = ServiceLocator.getService(ServerWrapper.class);
            DataLoader dl = ServiceLocator.getService(DataLoader.class);

            // check if argument exists
            if(args.length < 1) {
                client.sendServerMessage(dl.getMessage("missing-argument"));
                return;
            }

            // check if the client has a "last sender"
            if(!client.getLastPrivateSenderUsername().isEmpty()) {
                // check if the receiver is online
                if(server.isUserOnline(client.getLastPrivateSenderUsername())) {

                    // put together the message
                    StringBuilder msg = new StringBuilder();
                    for(int i = 0; i < args.length; i++) {
                        msg.append(args[i]).append(" ");
                    }

                    // send the message to the receiver
                    JsonObject payload = new JsonObject();
                    payload.addProperty("sender", client.getUsername());
                    payload.addProperty("receiver", client.getLastPrivateSenderUsername());
                    payload.addProperty("message", msg.toString());
                    Request req = new Request(RequestType.PRIVATE_MSG, payload);

                    ConnectionRequestHandler receiver = server.getConnectedClients()
                            .stream()
                            .filter(cl -> cl.getUsername().equals(client.getLastPrivateSenderUsername()))
                            .findFirst()
                            .get();

                    receiver.sendRequest(req);
                    receiver.setLastPrivateSenderUsername(client.getUsername()); // setup the name for the /r command

                    // echo the message to the sender
                    client.sendRequest(req);


                }else{
                    // recipient is offline
                    String msg = String.format("%s%s %s",
                            dl.getMessage("prefix"), args[0], dl.getMessage("offline"));
                    client.sendServerMessage(msg);
                }
            }else{
                // nobody to respond to
                client.sendServerMessage(dl.getMessage("nobody"));
            }
        }
    }
}
