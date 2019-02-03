package commands.implementations;

import com.google.gson.JsonObject;
import commands.CommandExecutor;
import commands.CommandSender;
import communication.Request;
import communication.ConnectionRequestHandler;
import communication.RequestType;
import core.ServerWrapper;
import core.ServiceLocator;
import data.ANSI;
import data.DataLoader;

public class MsgCommand implements CommandExecutor {



    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        if (sender instanceof ConnectionRequestHandler) {

            ServerWrapper server = ServiceLocator.getService(ServerWrapper.class);
            DataLoader dl = ServiceLocator.getService(DataLoader.class);

            ConnectionRequestHandler client = (ConnectionRequestHandler) sender;

            // check if arguments exists
            if(args.length < 2) {
                client.sendServerMessage(dl.getMessage("missing-argument"));
                return;
            }

            //check if the user tries to send it to himself
            if(client.getClientData().getUsername().equals(args[0])) {
                client.sendServerMessage(dl.getMessage("cant-msg"));
                return;
            }

            // check if receiver is online
            if(server.isUserOnline(args[0])) {

                ConnectionRequestHandler receiver = server.getConnectedClients()
                        .stream()
                        .filter(cl -> cl.getClientData().getUsername().equals(args[0]))
                        .findFirst()
                        .get();

                // check if receiver has blocked the sender
                if(receiver.getClientData().getBlockedUsernames().contains(client.getClientData().getUsername())) {
                    // notify the sender that he can't send messages to the receiver
                    client.sendServerMessage(dl.getMessage("cant-send"));

                }else{
                    // put together the message
                    StringBuilder msg = new StringBuilder();
                    for(int i = 1; i < args.length; i++) {
                        msg.append(args[i]).append(" ");
                    }

                    // send the message to the receiver
                    JsonObject payload = new JsonObject();
                    payload.addProperty("sender", client.getClientData().getUsername());
                    payload.addProperty("receiver", args[0]);
                    payload.addProperty("message", msg.toString());
                    Request req = new Request(RequestType.PRIVATE_MSG, payload);

                    receiver.sendRequest(req);
                    receiver.getClientData().setLastPrivateSenderUsername(client.getClientData().getUsername()); // setup the name for the /r command

                    // echo the message to the sender
                    client.sendRequest(req);
                    //echo to the console
                    if(server.getConsole().getMode().equals("default"))
                        server.getConsole().print(ANSI.CYAN+client.getClientData().getUsername()+" -> "+args[0]+":"+msg.toString());
                }
            }else{
                String msg = String.format("%s%s %s",
                        dl.getMessage("prefix"), args[0], dl.getMessage("offline"));
                client.sendServerMessage(msg);
            }
        }
    }
}
