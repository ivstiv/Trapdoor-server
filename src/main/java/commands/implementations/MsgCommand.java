package commands.implementations;

import com.google.gson.JsonObject;
import commands.CommandExecutor;
import commands.CommandSender;
import communication.Request;
import communication.RequestHandler;
import communication.RequestType;
import core.ServerWrapper;
import core.ServiceLocator;
import data.DataLoader;

import java.util.Optional;

public class MsgCommand implements CommandExecutor {



    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        if (sender instanceof RequestHandler) {

            ServerWrapper server = ServiceLocator.getService(ServerWrapper.class);
            DataLoader dl = ServiceLocator.getService(DataLoader.class);

            RequestHandler client = (RequestHandler) sender;

            // check if arguments exists
            if(args.length < 2) {
                client.sendServerMessage(dl.getMessage("missing-argument"));
                return;
            }

            //check if the user tries to send it to himself
            if(client.getUsername().equals(args[0])) {
                client.sendServerMessage(dl.getMessage("cant-msg"));
                return;
            }

            // check if receiver is online
            if(server.isUserOnline(args[0])) {

                // put together the message
                StringBuilder msg = new StringBuilder();
                for(int i = 1; i < args.length; i++) {
                    msg.append(args[i]+" ");
                }

                // send the message to the receiver
                JsonObject payload = new JsonObject();
                payload.addProperty("sender", client.getUsername());
                payload.addProperty("receiver", args[0]);
                payload.addProperty("message", msg.toString());
                Request req = new Request(RequestType.PRIVATE_MSG, payload);

                server.getConnectedClients()
                        .stream()
                        .filter(cl -> cl.getUsername().equals(args[0]))
                        .findFirst()
                        .get()
                        .sendRequest(req);

                // echo the message to the sender
                client.sendRequest(req);
            }else{
                String msg = String.format("%s%s %s",
                        dl.getMessage("prefix"), args[0], dl.getMessage("offline"));
                client.sendServerMessage(msg);
            }
        }
    }
}
