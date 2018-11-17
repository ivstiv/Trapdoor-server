package communication;

import communication.security.AES;
import communication.security.RSA;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public abstract class AbstractHandler extends Thread{

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;

    private AES aes;

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

    protected String readRequest() throws IOException{
        String encrypted = in.readLine();
        if(encrypted == null) return null; // will be "caught" on upper stage

        return aes.decrypt(encrypted);
    }

    protected synchronized void send(String msg) {
        System.out.println("Sending:"+msg);
        out.println(aes.encrypt(msg));
        out.flush();
    }

}
