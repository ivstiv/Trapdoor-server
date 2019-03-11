package commands.implementations;

import com.google.gson.JsonObject;
import commands.CommandExecutor;
import commands.CommandSender;
import communication.Request;
import communication.ConnectionHandler;
import communication.RequestType;
import communication.handlers.RequestHandler;
import core.Console;
import core.ServiceLocator;
import data.DataLoader;

public class HelpCommand implements CommandExecutor {

    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        DataLoader dl = ServiceLocator.getService(DataLoader.class);

        if(sender instanceof RequestHandler) {
            RequestHandler handler = (RequestHandler) sender;
            ConnectionHandler client = handler.getClient();
            client.sendMessage(dl.getMessage("help"));
        }

        if(sender instanceof Console) {
            Console console = (Console) sender;
            console.print(dl.getMessage("cl-help"));
        }
    }
}
