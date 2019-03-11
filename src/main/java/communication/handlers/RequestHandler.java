package communication.handlers;

import communication.ConnectionHandler;
import communication.Request;
import core.ServerWrapper;
import core.ServiceLocator;
import data.ConnectionData;
import data.DataLoader;

public class RequestHandler {

    protected final ConnectionHandler client;
    protected final ConnectionData clientData;
    protected final ServerWrapper server;
    protected final DataLoader dl = ServiceLocator.getService(DataLoader.class);

    public RequestHandler(ConnectionHandler client) {
        this.client = client;
        this.clientData = client.getClientData();
        this.server = client.getServer();
    }
    public void handle(Request r) {
        throw new UnsupportedOperationException();
    }
    public ConnectionHandler getClient() { return client; }
}
