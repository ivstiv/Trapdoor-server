package commands;

public interface CommandExecutor {

    void onCommand(CommandSender sender, String command, String[] args);

    // to prevent code repetition in the implementations
    default String buildMessage(String prefix, String[] strings) {
        StringBuilder msg = new StringBuilder(prefix);
        for(String s : strings) {
            msg.append(s).append(" ");
        }
        return msg.toString().trim();
    }
}
