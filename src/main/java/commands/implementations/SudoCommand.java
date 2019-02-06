package commands.implementations;

import com.google.gson.JsonObject;
import commands.CommandExecutor;
import commands.CommandSender;
import commands.SudoSession;
import communication.ConnectionRequestHandler;
import communication.Request;
import communication.RequestType;
import core.Console;
import core.ServiceLocator;
import data.DataLoader;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SudoCommand implements CommandExecutor {
    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        DataLoader dl = ServiceLocator.getService(DataLoader.class);

        if(sender instanceof ConnectionRequestHandler) {
            ConnectionRequestHandler client = (ConnectionRequestHandler) sender;

            // check if arguments exists
            if(args.length < 1) {
                client.sendServerMessage(dl.getMessage("missing-argument"));
                return;
            }

            SudoSession session = client.getClientData().initSudoSession(args);

            Stream<String> cmdStream = Arrays.stream(args);
            String cmd = cmdStream.collect(Collectors.joining(" "));

            // send action for password confirmation
            JsonObject payload = new JsonObject();
            payload.addProperty("action", "confirm_sudo");
            payload.addProperty("session_id", session.getId());
            payload.addProperty("session_cmd", cmd);
            Request confirmationReq = new Request(RequestType.ACTION, payload);
            client.sendRequest(confirmationReq);
            /*
             This will trigger the client to input password and it is going to be
             handled by the request handler as RequestType.ACTION sending password and id
             for executing the command in the corresponding session.
             */
        }


        if(sender instanceof Console) {
            Console console = (Console) sender;
            console.print(dl.getMessage("cl-unknown-cmd"));
        }
    }
}
