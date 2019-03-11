package commands.implementations;

import com.google.gson.JsonPrimitive;
import commands.CommandExecutor;
import commands.CommandSender;
import commands.SudoSession;
import communication.ConnectionHandler;
import communication.handlers.RequestHandler;
import core.Console;
import core.ServerWrapper;
import core.ServiceLocator;
import data.Config;
import data.DataLoader;

public class IpunbanCommand implements CommandExecutor {
    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        DataLoader dl = ServiceLocator.getService(DataLoader.class);

        if(sender instanceof Console) {
            Console console = (Console) sender;

            if(args.length < 1) {
                console.print(dl.getMessage("cl-missing-argument"));
                return;
            }

            String ip = args[0];

            // check if the user is banned
            if(Config.getJsonArray("forbidden_ips").contains(new JsonPrimitive(ip))) {
                // remove the username from the file and config
                Config.getJsonArray("forbidden_ips").remove(new JsonPrimitive(ip));
                Config.updateFile();
                // echo an answer
                console.print(ip+" "+dl.getMessage("cl-unbanned"));
            }else{
                console.print(ip+" "+dl.getMessage("cl-not-banned"));
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
            if(args.length < 1) {
                client.sendPrefixedErrorMessage(dl.getMessage("missing-argument"));
                return;
            }

            String ip = args[0];

            // check if the user is banned
            if(Config.getJsonArray("forbidden_ips").contains(new JsonPrimitive(ip))) {
                // remove the username from the file and config
                Config.getJsonArray("forbidden_ips").remove(new JsonPrimitive(ip));
                Config.updateFile();
                // echo an answer
                client.sendPrefixedMessage(ip+" "+dl.getMessage("cl-unbanned"));
            }else{
                client.sendPrefixedErrorMessage(ip+" "+dl.getMessage("cl-not-banned"));
            }
        }
    }
}
