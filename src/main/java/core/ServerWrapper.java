package core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerWrapper {
    public void start() {

        Thread requestReceiver = new Thread() {
            public void run() {

                ServerSocket ss = null;

                try {
                    ss = new ServerSocket(Config.getInt("port"));    // opening the socket
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.printf("Request receiver running at port %d . . .\n", Config.getInt("port"));
                Socket socket;
                RequestHandler client;
                // TODO: 17-Nov-18 create The handler
                try {
                    while (true) {
                        socket = ss.accept();
                        client = new RequestHandler(socket);
                        client.start();
                        System.out.println("Connected client");
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        };
        requestReceiver.start();
    }
}
}
