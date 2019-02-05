package commands;

import data.Config;

import java.security.SecureRandom;
import java.util.Base64;

public class SudoSession {

    private final String id;
    private final String[] command;
    private final long creationTime;
    private final CommandSender sender;

    public SudoSession(String[] command, CommandSender sender) {
        this.id = generateRandomId();
        this.command = command;
        this.creationTime = System.currentTimeMillis();
        this.sender = sender;
    }

    public boolean verifyPassword(String pass) {
        return Config.getString("sudo_password").equals(pass);
    }

    public String getId() {
        return id;
    }

    public String[] getCommand() {
        return command;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public CommandSender getSender() {
        return sender;
    }

    private String generateRandomId() {
        byte[] array = new byte[10];
        new SecureRandom().nextBytes(array);
        return Base64.getEncoder().encodeToString(array);
    }
}
