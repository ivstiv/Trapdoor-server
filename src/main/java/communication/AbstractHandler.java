package communication;

import com.google.gson.JsonObject;
import communication.security.AES;
import communication.security.RSA;
import data.Channel;
import data.Request;
import data.RequestType;
import data.User;
import exceptions.MalformedRequestException;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public abstract class AbstractHandler extends Thread{

    // Communication objects
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;

    // Encryption objects
    private AES aes;

    //User data
    private String username;
    private Channel activeChannel;

    public AbstractHandler(Socket client) {
        this.client = client;
        initialiseStreams();
    }

    protected void initialiseStreams() {
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8));

            // setup the AES encryption of the communication
            RSA tunnel = getRSAHandshake();
            String aesSecret = in.readLine();
            if (aesSecret == null) { // TO-DO: do something about this ! reads null on disconnect
                System.out.println("[Handler]Host disconnected!");
            }
            aes = new AES(tunnel.decrypt(aesSecret));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private RSA getRSAHandshake() throws IOException, NoSuchAlgorithmException {
        RSA rsa = new RSA();
        // send the public key to the server
        out.println(rsa.getPublicKeyBase64());
        out.flush();

        // wait for the public key of the server
        String serverKey = in.readLine();
        if (serverKey == null) { // TO-DO: do something about this ! reads null on disconnect
            System.out.println("[Handler]Host disconnected!");
        }

        // register the server public key in the RSA object
        rsa.setRemotePublicKey(serverKey);
        System.out.println("RSA tunnel initialised!");
        return rsa;
    }

    protected Request readRequest() throws MalformedRequestException {
        String encrypted = null;
        try {
            encrypted = in.readLine();
        }catch (SocketException e) {
            //Kicks in when the client drops the connection unexpectedly
            System.out.println("[Handler]Client disconnected unexpectedly!");
            return new Request(RequestType.DISCONNECT, new JsonObject());
        }catch (IOException e) {
            System.out.println("[Handler]IOException in readRequest():");
                e.printStackTrace();
            }
        if (encrypted == null) { // TO-DO: do something about this ! reads null on disconnect
            System.out.println("[Handler]Client disconnected!");
            return new Request(RequestType.DISCONNECT, new JsonObject());
        }
        String decrypted = aes.decrypt(encrypted);
        System.out.println("INCOMING:"+decrypted);

        return new Request(decrypted);
    }

    protected void sendRequest(Request r) {
        send(r.toString());
    }

    protected synchronized void send(String msg) {
        System.out.println("OUTGOING:"+msg);
        out.println(aes.encrypt(msg));
        out.flush();
    }

}
