package communication;

import com.google.gson.JsonObject;
import communication.security.AES;
import communication.security.AES_OLD;
import communication.security.RSA;
import core.ServiceLocator;
import data.Channel;
import data.DataLoader;
import exceptions.MalformedRequestException;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public abstract class ConnectionHandler extends Thread{

    // Communication objects
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;

    // Encryption objects
    private AES aes;

    //User data
    protected String username;
    protected Channel activeChannel;
    private String lastPrivateSenderUsername = "";

    protected DataLoader dl = ServiceLocator.getService(DataLoader.class);

    public ConnectionHandler(Socket client) {
        this.client = client;
    }

    public String getUsername() {return this.username;}
    public Channel getActiveChannel() {return  this.activeChannel;}
    public void setActiveChannel(Channel newChannel) {this.activeChannel = newChannel;}

    protected void initialiseStreams() {
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8));

            // setup the AES encryption of the communication
            RSA tunnel = getRSAHandshake();
            String aesSecret = in.readLine();
            String aesSignature = in.readLine();

            if (aesSecret == null || aesSignature == null) { // TO-DO: do something about this ! reads null on disconnect
                System.out.println("[Handler]Host disconnected!");
            }

            if(tunnel.verify(tunnel.decrypt(aesSecret), aesSignature)) {
                aes = new AES(tunnel.decrypt(aesSecret));
                System.out.println("AES setup finished!");
            }else{
                System.out.println("AES signature failed!");
                // TODO: 18-Nov-18 stop the connection and send status code
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private RSA getRSAHandshake() throws IOException, NoSuchAlgorithmException {
        RSA rsa = new RSA();
        // send the public key to the client
        out.println(rsa.getPublicKeyBase64());
        out.flush();

        // wait for the public key of the client
        String clientKey = in.readLine();
        if (clientKey == null) { // TO-DO: do something about this ! reads null on disconnect
            System.out.println("[Handler]Host disconnected!");
        }

        // register the client public key in the RSA object
        rsa.setRemotePublicKey(clientKey);
        System.out.println("RSA tunnel initialised!");
        return rsa;
    }

    protected Request readRequest() throws MalformedRequestException {
        String encrypted = null;
        try {
            encrypted = in.readLine();
        } catch (SocketException e) {
            //Kicks in when the client drops the connection unexpectedly
            System.out.println("[Handler]Client disconnected!");
            return new Request(RequestType.DISCONNECT, new JsonObject());

        } catch (IOException e) {
            System.out.println("[Handler]IOException in readRequest():");
            e.printStackTrace();
        }
        if (encrypted == null) { // reads null on disconnect e.g end of the stream
            System.out.println("[Handler]Client disconnected!");
            return new Request(RequestType.DISCONNECT, new JsonObject());
        }

        String decrypted = aes.decrypt(encrypted);
        System.out.println("IN:"+decrypted);

        return new Request(decrypted);
    }

    public void sendRequest(Request r) {
        send(r.toString());
    }

    protected synchronized void send(String msg) {
        System.out.println("OUT:"+msg);
        out.println(aes.encrypt(msg));
        out.flush();
    }

    public void stopConnection() {
        try {
            client.close();
            // readRequest() will send a DISCONNECT request to the RequestHandler because it reads null
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendServerMessage(String msg) {
        JsonObject payload = new JsonObject();
        payload.addProperty("action", "print");
        payload.addProperty("message", msg);
        Request response = new Request(RequestType.ACTION, payload);
        sendRequest(response);
    }

    public void setLastPrivateSenderUsername(String username) {
        this.lastPrivateSenderUsername = username;
    }

    public String getLastPrivateSenderUsername() {
        return this.lastPrivateSenderUsername;
    }
}
