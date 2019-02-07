package commands.implementations;

import com.google.gson.JsonObject;
import commands.CommandExecutor;
import commands.CommandSender;
import communication.ConnectionRequestHandler;
import communication.Request;
import communication.RequestType;
import core.Console;
import core.ServerWrapper;
import core.ServiceLocator;
import data.ANSI;
import data.DataLoader;

public class RespondCommand implements CommandExecutor {

    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        ServerWrapper server = ServiceLocator.getService(ServerWrapper.class);
        DataLoader dl = ServiceLocator.getService(DataLoader.class);

        if (sender instanceof ConnectionRequestHandler) {
            ConnectionRequestHandler client = (ConnectionRequestHandler) sender;

            // check if argument exists
            if(args.length < 1) {
                client.sendServerErrorMessage(dl.getMessage("missing-argument"));
                return;
            }

            // check if the client has a "last sender"
            if(!client.getClientData().getLastPrivateSenderUsername().isEmpty()) {

                // getting the variables here to prevent nesting down there :D
                String senderUsername = client.getClientData().getUsername();
                String receiverUsername = client.getClientData().getLastPrivateSenderUsername();

                // check if the receiver is online
                if(server.isUserOnline(receiverUsername)) {

                    ConnectionRequestHandler receiver = server.getConnectedClients()
                            .stream()
                            .filter(cl -> cl.getClientData().getUsername().equals(receiverUsername))
                            .findFirst()
                            .get();

                    // check if receiver has blocked the sender
                    if(receiver.getClientData().getBlockedUsernames().contains(senderUsername)) {
                        // notify the sender that he can't send messages to the receiver
                        client.sendServerMessage(dl.getMessage("cant-send"));

                    }else{
                        // put together the message
                        String message = buildMessage("", args);

                        // send the message to the receiver
                        JsonObject payload = new JsonObject();
                        payload.addProperty("sender", senderUsername);
                        payload.addProperty("receiver", receiverUsername);
                        payload.addProperty("message", message);
                        Request req = new Request(RequestType.PRIVATE_MSG, payload);

                        receiver.sendRequest(req);
                        receiver.getClientData().setLastPrivateSenderUsername(senderUsername); // setup the name for the /r command

                        // echo the message to the sender
                        client.sendRequest(req);

                        //echo to the console
                        if(server.getConsole().getMode().equals("default"))
                            server.getConsole().print(ANSI.CYAN+senderUsername+" -> "+receiverUsername+":"+message);
                    }
                }else{
                    // recipient is offline
                    client.sendServerErrorMessage(receiverUsername+" "+dl.getMessage("offline"));
                }
            }else{
                // nobody to respond to
                client.sendServerErrorMessage(dl.getMessage("nobody"));
            }
        }

        if(sender instanceof Console) {
            server.getConsole().print(dl.getMessage("cl-unknown-cmd"));
        }
    }
}
