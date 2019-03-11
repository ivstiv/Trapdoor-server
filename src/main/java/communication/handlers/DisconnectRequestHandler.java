package communication.handlers;

import commands.CommandSender;
import communication.ConnectionHandler;
import communication.Request;
import data.ANSI;

public class DisconnectRequestHandler extends RequestHandler implements CommandSender {

    DisconnectRequestHandler(ConnectionHandler client) {
        super(client);
    }

    @Override
    public void handle(Request r) {
        client.stopConnection();
        if(clientData.getUsername() != null)
            if(server.getConsole().getMode().equals("default"))
                server.getConsole().print(ANSI.CYAN+clientData.getUsername()+" left the server.");
        server.removeConnectedClient(client);
    }
}
