package commands.implementations;

import com.google.gson.JsonObject;
import commands.CommandExecutor;
import commands.CommandSender;
import communication.Request;
import communication.ConnectionRequestHandler;
import communication.RequestType;
import core.Console;
import core.ServiceLocator;
import data.ANSI;
import data.DataLoader;

public class HelpCommand implements CommandExecutor {

    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        if(sender instanceof ConnectionRequestHandler) {
            DataLoader dl = ServiceLocator.getService(DataLoader.class);
            ConnectionRequestHandler client = (ConnectionRequestHandler) sender;

            JsonObject payload = new JsonObject();
            payload.addProperty("action", "print");
            payload.addProperty("message", dl.getMessage("help"));
            Request response = new Request(RequestType.ACTION, payload);
            client.sendRequest(response);
            return;

        }else if(sender instanceof Console) {

            Console console = (Console) sender;
            console.print(ANSI.GREEN+"HELP COMMAND");

        }
    }
}
