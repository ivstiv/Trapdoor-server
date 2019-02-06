package commands;

import data.Config;

import java.security.SecureRandom;
import java.util.Base64;

public class SudoSession {

    private final String id;
    private final String[] command;
    private boolean isAuthenticated = false;

    public SudoSession(String[] command) {
        this.id = generateRandomId();
        this.command = command;
    }

    public boolean isAuthenticated() {
        return this.isAuthenticated;
    }

    public boolean authenticate(String password) {
        if(Config.getString("sudo_password").equals(password))
            isAuthenticated = true;
        return isAuthenticated;
    }

    public String getId() {
        return id;
    }

    public String[] getCommand() {
        return command;
    }

    private String generateRandomId() {
        byte[] array = new byte[10];
        new SecureRandom().nextBytes(array);
        return Base64.getEncoder().encodeToString(array);
    }
}
