package commands;

public class Command {

    private CommandSender sender;
    private String name;
    private String[] args;

    public Command(CommandSender sender, String name, String[] args) {
        this.sender = sender;
        this.name = name;
        this.args = args;
    }

    public CommandSender getSender() {
        return sender;
    }

    public String getName() {
        return name;
    }

    public String[] getArgs() {
        return args;
    }
}
