package data;

/*
    Class for encapsulating all data of the client connection.
 */

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ConnectionData {

    private String username, lastPrivateSenderUsername = "";
    private Channel activeChannel;
    private Set<String> blockedUsernames = new HashSet<>();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLastPrivateSenderUsername() {
        return lastPrivateSenderUsername;
    }

    public void setLastPrivateSenderUsername(String lastPrivateSenderUsername) {
        this.lastPrivateSenderUsername = lastPrivateSenderUsername;
    }

    public Channel getActiveChannel() {
        return activeChannel;
    }

    public void setActiveChannel(Channel activeChannel) {
        this.activeChannel = activeChannel;
    }

    public Set<String> getBlockedUsernames() {
        return Collections.unmodifiableSet(blockedUsernames);
    }

    public void blockUsername(String username) {
        this.blockedUsernames.add(username);
    }

    public void unblockUsername(String username) {
        this.blockedUsernames.remove(username);
    }
}
