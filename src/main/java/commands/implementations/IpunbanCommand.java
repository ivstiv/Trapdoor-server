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

public class IpunbanCommand implements CommandExecutor {
    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        DataLoader dl = ServiceLocator.getService(DataLoader.class);
        ServerWrapper server = ServiceLocator.getService(ServerWrapper.class);

        if(sender instanceof Console) {
            Console console = (Console) sender;

            if(args.length < 1) {
                console.print(dl.getMessage("cl-missing-argument"));
                return;
            }

            // check if the user is banned
            if(Config.getJsonArray("forbidden_ips").contains(new JsonPrimitive(args[0]))) {
                // remove the username from the file and config
                Config.getJsonArray("forbidden_ips").remove(new JsonPrimitive(args[0]));
                Config.updateFile();
                // echo an answer
                console.print(args[0]+" "+dl.getMessage("cl-unbanned"));
                return;
            }else{
                console.print(args[0]+" "+dl.getMessage("cl-not-banned"));
                return;
            }
        }

        if(sender instanceof ConnectionRequestHandler) {
            ConnectionRequestHandler client = (ConnectionRequestHandler) sender;
            client.sendServerMessage("NEEDS SUDO IMPLEMENTATION");
        }
    }
}
