package communication.handlers;

import commands.CommandSender;
import commands.SudoSession;
import communication.ConnectionHandler;
import communication.Request;
import data.ANSI;

import java.util.Arrays;

public class ActionRequestHandler extends RequestHandler implements CommandSender {

    ActionRequestHandler(ConnectionHandler client) {
        super(client);
    }

    @Override
    public void handle(Request r) {
        String action = r.getContent().get("action").getAsString();

        /* SUDO CONFIRMATION PASSWORD */
        if(action.equals("confirm_sudo")) {
            String password = r.getContent().get("sudo_password").getAsString();

            // check if there is a sudo session
            if(!clientData.hasSudoSession()) {
                client.sendPrefixedErrorMessage(dl.getMessage("invalid-sudo-session"));
                return;
            }

            SudoSession session = clientData.getSudoSession();

            // check if password matches
            if(!session.authenticate(password)) {
                client.sendPrefixedErrorMessage(dl.getMessage("invalid-sudo-pass"));
                clientData.destroySudoSession();
                return;
            }

            // if there is a session and the password matches dispatch the command of the session
            String name = session.getCommand()[0].replaceAll("/", "");
            String[] args;
            if(session.getCommand().length > 1) {
                args = Arrays.copyOfRange(session.getCommand(), 1, session.getCommand().length);
            }else{
                args = new String[0];
            }

            server.dispatchCommand(this, name, args);

            if(server.getConsole().getMode().equals("default"))
                server.getConsole().print(ANSI.BG_RED+ANSI.WHITE+clientData.getUsername()+" issued a command: /"+name);
            if(server.getConsole().getMode().equals("commands-only")) {
                String cmd = String.join(" ", session.getCommand());
                server.getConsole().print(ANSI.BG_RED + ANSI.WHITE + clientData.getUsername() + " issued a command: " + cmd);
            }
        }
    }
}
