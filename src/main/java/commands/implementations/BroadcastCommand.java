package commands.implementations;

import commands.CommandExecutor;
import commands.CommandSender;
import communication.ConnectionRequestHandler;
import core.Console;
import core.ServerWrapper;
import core.ServiceLocator;
import data.Channel;
import data.DataLoader;

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

            // check if the channel exists
            if(server.getChannel(args[0]) != null) {

                // put together the message
                StringBuilder msg = new StringBuilder();
                for(int i = 1; i < args.length; i++) {
                    msg.append(args[i]).append(" ");
                }
                // broadcast the message
                server.getChannel(args[0]).broadcastPrint(msg.toString());

                console.print("Message broadcasted to: "+args[0]);
            }else{
                // check if the argument is all channels
                if(args[0].equals("all")) {

                    // put together the message
                    StringBuilder msg = new StringBuilder();
                    for(int i = 1; i < args.length; i++) {
                        msg.append(args[i]).append(" ");
                    }

                    for(Channel ch : server.getChannels()) {
                        ch.broadcastPrint(msg.toString());
                        console.print("Message broadcasted to: "+ch.getName());
                    }

                }else{
                    console.print(dl.getMessage("cl-invalid-channel"));
                }
            }
        }


        if(sender instanceof ConnectionRequestHandler) {
            ConnectionRequestHandler client = (ConnectionRequestHandler) sender;
            client.sendServerMessage("NEEDS SUDO IMPLEMENTATION");
        }
    }
}
