package communication.handlers;

import communication.ConnectionHandler;
import communication.RequestType;

public final class RequestHandlerBuilder {

    private ConnectionHandler client;
    private RequestType[] supportedRequests = {RequestType.ACTION, RequestType.CONNECT, RequestType.DISCONNECT, RequestType.MSG};

    public RequestHandlerBuilder(ConnectionHandler client) {
        this.client = client;
    }

    public RequestHandler buildHandlerForRequest(RequestType type) {

        switch (type) {
            case MSG: return new MessageRequestHandler(client);
            case ACTION: return new ActionRequestHandler(client);
            case DISCONNECT: return new DisconnectRequestHandler(client);
            case CONNECT: return new ConnectRequestHandler(client);
            default: return new RequestHandler(client);
            // the default should never be reached if the type is checked with isSupported()
        }
    }

    public boolean isSupported(RequestType type) {
        for(RequestType req : supportedRequests)
            if(type.equals(req))
                return true;
        return false;
    }
}
