package commands.implementations;

import com.google.gson.JsonPrimitive;
import commands.CommandExecutor;
import commands.CommandSender;
import communication.ConnectionRequestHandler;
import core.Console;
import core.ServerWrapper;
import core.ServiceLocator;
import data.Config;
import data.DataLoader;

import java.util.Optional;

public class IpbanCommand implements CommandExecutor {
    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        DataLoader dl = ServiceLocator.getService(DataLoader.class);
        ServerWrapper server = ServiceLocator.getService(ServerWrapper.class);

        if (sender instanceof Console) {
            Console console = (Console) sender;

            if (args.length < 1) {
                console.print(dl.getMessage("cl-missing-argument"));
                return;
            }

            // add the ip to forbidden ips
            if (!Config.getJsonArray("forbidden_ips").contains(new JsonPrimitive(args[0]))) {
                // add the name to the list
                Config.getJsonArray("forbidden_ips").add(args[0]);
                Config.updateFile();

                // check if there is a connected client with that ip and kick it
                Optional<ConnectionRequestHandler> targetUser = server.getConnectedClients()
                        .stream()
                        .filter(cl -> cl.getClientData().getIp().equals(args[0]))
                        .findFirst();

                if(targetUser.isPresent()) {
                    console.print("kick it");
                }
            }else{
                console.print(args[0]+" "+dl.getMessage("cl-already-banned"));
                return;
            }
        }

        if(sender instanceof ConnectionRequestHandler) {
            ConnectionRequestHandler client = (ConnectionRequestHandler) sender;
            client.sendServerMessage("NEEDS SUDO IMPLEMENTATION");
        }
    }
}
