package communication.handlers;

import commands.CommandSender;
import communication.ConnectionHandler;
import communication.Request;
import data.ANSI;

public class MessageRequestHandler extends RequestHandler implements CommandSender {

    MessageRequestHandler(ConnectionHandler client) {
        super(client);
    }

    @Override
    public void handle(Request r) {
        if(client.getClientData().isMuted()) return;

        String message = r.getContent().get("message").getAsString();

        if(message.startsWith("/")) {
            // dispatch the command
            String name = message.split(" ")[0].replaceAll("/", "");
            String[] args;
            if(message.length() > name.length()+1) {
                args = message.substring(name.length()+2).split(" ");
            }else{
                args = new String[0];
            }
            server.dispatchCommand(this, name, args);

            if(server.getConsole().getMode().equals("default"))
                server.getConsole().print(ANSI.CYAN+clientData.getUsername()+" issued a command: /"+name);
            if(server.getConsole().getMode().equals("commands-only"))
                server.getConsole().print(ANSI.CYAN+clientData.getUsername()+" issued a command: "+message);
        }else{
            clientData.getActiveChannel().broadcastMsg(client, message); // forward the message
            if(server.getConsole().getMode().equals("default"))
                server.getConsole().print(ANSI.CYAN+clientData.getUsername()+":"+message);
        }
    }
}
