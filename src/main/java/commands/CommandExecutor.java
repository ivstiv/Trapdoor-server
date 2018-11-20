package commands;

public interface CommandExecutor {

    void onCommand(CommandSender sender, String command, String[] args);
}
