package core;

import data.ChannelType;

public class Main {
    public static void main(String[] args) {
        ServerWrapper server = new ServerWrapper();
        ServiceLocator.initialiseService(server);
        server.start();
    }
}
