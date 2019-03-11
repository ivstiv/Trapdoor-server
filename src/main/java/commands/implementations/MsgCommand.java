package commands.implementations;

import com.google.gson.JsonObject;
import commands.CommandExecutor;
import commands.CommandSender;
import communication.Request;
import communication.ConnectionHandler;
import communication.RequestType;
import communication.handlers.RequestHandler;
import core.Console;
import core.ServerWrapper;
import core.ServiceLocator;
import data.ANSI;
import data.DataLoader;

import java.util.Arrays;

public class MsgCommand implements CommandExecutor {



    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        ServerWrapper server = ServiceLocator.getService(ServerWrapper.class);
        DataLoader dl = ServiceLocator.getService(DataLoader.class);

        if(sender instanceof RequestHandler) {
            RequestHandler handler = (RequestHandler) sender;
            ConnectionHandler client = handler.getClient();

            // check if arguments exists
            if(args.length < 2) {
                client.sendPrefixedErrorMessage(dl.getMessage("missing-argument"));
                return;
            }

            //check if the user tries to send it to himself
            if(client.getClientData().getUsername().equals(args[0])) {
                client.sendPrefixedErrorMessage(dl.getMessage("cant-msg"));
                return;
            }

            // check if receiver is online
            if(server.isUserOnline(args[0])) {

                String recipient = args[0];
                String[] msg = Arrays.copyOfRange(args, 1, args.length);

                ConnectionHandler receiver = server.getConnectedClients()
                        .stream()
                        .filter(cl -> cl.getClientData().getUsername().equals(recipient))
                        .findFirst()
                        .get();

                // check if receiver has blocked the sender
                if(receiver.getClientData().getBlockedUsernames().contains(client.getClientData().getUsername())) {
                    // notify the sender that he can't send messages to the receiver
                    client.sendPrefixedErrorMessage(dl.getMessage("cant-send"));

                }else{
                    // put together the message
                    String message = buildMessage("", msg);

                    // send the message to the receiver
                    JsonObject payload = new JsonObject();
                    payload.addProperty("sender", client.getClientData().getUsername());
                    payload.addProperty("receiver", recipient);
                    payload.addProperty("message", message);
                    Request req = new Request(RequestType.PRIVATE_MSG, payload);

                    receiver.sendRequest(req);
                    receiver.getClientData().setLastPrivateSenderUsername(client.getClientData().getUsername()); // setup the name for the /r command

                    // echo the message to the sender
                    client.sendRequest(req);
                    //echo to the console
                    if(server.getConsole().getMode().equals("default"))
                        server.getConsole().print(ANSI.CYAN+client.getClientData().getUsername()+" -> "+args[0]+":"+message);
                }
            }else{
                client.sendPrefixedErrorMessage(args[0]+" "+dl.getMessage("offline"));
            }
        }

        if(sender instanceof Console) {
            server.getConsole().print(dl.getMessage("cl-unknown-cmd"));
        }
    }
}
