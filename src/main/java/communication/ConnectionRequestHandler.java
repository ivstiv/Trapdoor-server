package communication;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import commands.CommandSender;
import commands.SudoSession;
import data.ANSI;
import data.Config;
import data.ChannelType;
import exceptions.MalformedRequestException;

import java.net.Socket;
import java.util.Arrays;

public class ConnectionRequestHandler extends ConnectionHandler implements CommandSender {

    public ConnectionRequestHandler(Socket client) {
        super(client);
    }

    @Override
    public void run() {
        //server.getConsole().print("Starting Handler thread:"+this.toString());
        initialiseStreams();

        while (true) {
            Request r = null;
            try {
                r = readRequest();
            } catch (MalformedRequestException e) {
                e.printStackTrace();
                continue; // skip the iteration if the request is invalid
            }

            // send confirmation if it is not disconnect request
            JsonObject content = new JsonObject();
            content.addProperty("code", 100);
            content.addProperty("timestamp", r.getTimestamp());
            Request confirmation = new Request(RequestType.RESPONSE, content);
            sendRequest(confirmation);

            switch(r.getType()) {
                case MSG:

                    if(getClientData().isMuted()) break;

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
                        clientData.getActiveChannel().broadcastMsg(this, message); // forward the message
                        if(server.getConsole().getMode().equals("default"))
                            server.getConsole().print(ANSI.CYAN+clientData.getUsername()+":"+message);
                    }
                    break;
                case ACTION:
                    String action = r.getContent().get("action").getAsString();

                    /* SUDO CONFIRMATION PASSWORD */
                    if(action.equals("confirm_sudo")) {
                        String password = r.getContent().get("sudo_password").getAsString();

                        // check if there is a sudo session
                        if(!getClientData().hasSudoSession()) {
                            sendServerErrorMessage(dl.getMessage("invalid-sudo-session"));
                            break;
                        }

                        SudoSession session = getClientData().getSudoSession();

                        // check if password matches
                        if(!session.authenticate(password)) {
                            sendServerErrorMessage(dl.getMessage("invalid-sudo-pass"));
                            getClientData().destroySudoSession();
                            break;
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



                        /*
                        try {
                            SudoSession session = server.getSudoSession(sessionId);
                            if(session.verifyPassword(password)) {

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
                                    server.getConsole().print(ANSI.BG_RED+ANSI.WHITE+clientData.getUsername()+" issued a command: /"+cmd);
                                }

                            }else{
                                sendServerMessage(dl.getMessage("invalid-sudo-pass"));
                            }

                        } catch (Exception e) {
                            sendServerMessage(dl.getMessage("invalid-sudo-session"));
                        }
                        */
                    }
                    break;
                case DISCONNECT:
                    // may be close the streams first
                    if(clientData.getUsername() != null)
                        if(server.getConsole().getMode().equals("default"))
                            server.getConsole().print(ANSI.CYAN+clientData.getUsername()+" left the server.");
                    server.removeConnectedClient(this);
                    return;
                case CONNECT:
                    String username = r.getContent().get("username").getAsString();
                    String password = r.getContent().get("password").getAsString();

                    // TODO: 04-Feb-19 check if the username is a-zA-Z0-9 regex

                    // check if the server is full
                    if(server.getConnectedClients().size() >= Config.getInt("slots")) {
                        JsonObject payload = new JsonObject();
                        payload.addProperty("code", 203);
                        Request req = new Request(RequestType.RESPONSE, payload);
                        sendRequest(req);
                        break;
                    }

                    //server password check
                    if(!Config.getString("password").isEmpty()) {
                        if(!Config.getString("password").equals(password)) {
                            JsonObject payload = new JsonObject();
                            payload.addProperty("code", 200);
                            Request req = new Request(RequestType.RESPONSE, payload);
                            sendRequest(req);
                            if(server.getConsole().getMode().equals("default"))
                                server.getConsole().print(ANSI.CYAN+"Client tried to login with wrong password: "+username);
                            break;
                        }
                    }

                    // forbidden username check
                    if(Config.getJsonArray("forbidden_usernames").contains(new JsonPrimitive(username))) {
                        JsonObject payload = new JsonObject();
                        payload.addProperty("code", 202);
                        Request req = new Request(RequestType.RESPONSE, payload);
                        sendRequest(req);
                        if(server.getConsole().getMode().equals("default"))
                            server.getConsole().print(ANSI.CYAN+"Client tried to login with forbidden username: "+username);
                        break;
                    }

                    // existing username check
                    if(server.isUserOnline(username)) {
                        JsonObject payload = new JsonObject();
                        payload.addProperty("code", 201);
                        Request req = new Request(RequestType.RESPONSE, payload);
                        sendRequest(req);
                        break;
                    }

                    // send motd
                    JsonObject payload = new JsonObject();
                    payload.addProperty("action", "print");
                    payload.addProperty("message", Config.getString("motd"));
                    Request motd = new Request(RequestType.ACTION, payload);
                    sendRequest(motd);

                    // setup the user's channel
                    clientData.setUsername(username);
                    clientData.setActiveChannel(server.getChannel(ChannelType.DEFAULT));
                    clientData.getActiveChannel().addClient(this);
                    server.addConnectedClient(this);
                    String msg = String.format("%s%s %s",
                            dl.getMessage("prefix"), clientData.getUsername(), dl.getMessage("join-server"));
                    server.getChannel(ChannelType.DEFAULT).broadcastPrint(msg);

                    // update status bar
                    payload = new JsonObject();
                    payload.addProperty("action", "update_statusbar");
                    payload.addProperty("channel", clientData.getActiveChannel().getName());
                    Request statusBar = new Request(RequestType.ACTION, payload);
                    sendRequest(statusBar);

                    // console print
                    if(server.getConsole().getMode().equals("default"))
                        server.getConsole().print(ANSI.CYAN+clientData.getUsername()+" joined the server.");
                    break;
            }
        }
    }
}
