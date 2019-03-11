package commands.implementations;

import commands.CommandExecutor;
import commands.CommandSender;
import commands.SudoSession;
import communication.ConnectionHandler;
import communication.handlers.RequestHandler;
import core.Console;
import core.ServerWrapper;
import core.ServiceLocator;
import data.Channel;
import data.DataLoader;

import java.util.Arrays;

public class BroadcastCommand implements CommandExecutor {

    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {


        DataLoader dl = ServiceLocator.getService(DataLoader.class);
        ServerWrapper server = ServiceLocator.getService(ServerWrapper.class);

        if(sender instanceof Console) {
            Console console = (Console) sender;

            if (args.length < 2) {
                console.print(dl.getMessage("cl-missing-argument"));
                return;
            }

            String channel = args[0];
            String[] msg = Arrays.copyOfRange(args, 1, args.length);

            // check if the channel exists
            if(server.getChannel(channel) != null) {

                // put together the message
                String message = buildMessage("", msg);

                // broadcast the message
                server.getChannel(channel).broadcastPrint(message);

                console.print(dl.getMessage("msg-broadcasted")+channel);
            }else{
                // check if the argument is all channels
                if(channel.equals("all")) {

                    // put together the message
                    String message = buildMessage("", msg);

                    for(Channel ch : server.getChannels()) {
                        ch.broadcastPrint(message);
                        console.print(dl.getMessage("msg-broadcasted")+ch.getName());
                    }

                }else{
                    console.print(dl.getMessage("cl-invalid-channel"));
                }
            }
        }


        if(sender instanceof RequestHandler) {
            RequestHandler handler = (RequestHandler) sender;
            ConnectionHandler client = handler.getClient();

            // check if there is a sudo session
            if(!client.getClientData().hasSudoSession()) {
                client.sendPrefixedErrorMessage(dl.getMessage("perm-denied"));
                return;
            }

            SudoSession session = client.getClientData().getSudoSession();

            // check if it is authenticated
            if(!session.isAuthenticated()) {
                client.sendPrefixedErrorMessage(dl.getMessage("perm-denied"));
                client.getClientData().destroySudoSession();
                return;
            }

            // check for arguments
            if(args.length < 2) {
                client.sendPrefixedErrorMessage(dl.getMessage("missing-argument"));
                return;
            }

            String channel = args[0];
            String[] msg = Arrays.copyOfRange(args, 1, args.length);

            // check if the channel exists
            if(server.getChannel(channel) != null) {

                // put together the message
                String message = buildMessage("", msg);

                // broadcast the message
                server.getChannel(channel).broadcastPrint(message);

                client.sendPrefixedMessage(dl.getMessage("msg-broadcasted")+channel);
            }else{
                // check if the argument is all channels
                if(channel.equals("all")) {

                    // put together the message
                    String message = buildMessage("", msg);

                    for(Channel ch : server.getChannels()) {
                        ch.broadcastPrint(message);
                        client.sendPrefixedMessage(dl.getMessage("msg-broadcasted")+ch.getName());
                    }

                }else{
                    client.sendPrefixedErrorMessage(dl.getMessage("invalid-channel"));
                }
            }
        }
    }
}
