package commands.implementations;

import commands.CommandExecutor;
import commands.CommandSender;
import communication.ConnectionRequestHandler;
import core.Console;
import core.ServiceLocator;
import data.ANSI;
import data.DataLoader;

public class ColorsCommand implements CommandExecutor {
    @Override
    public void onCommand(CommandSender sender, String command, String[] args) {

        if(sender instanceof Console) {
            Console console = (Console) sender;
            console.print(ANSI.BOLD+"BOLD");
            console.print(ANSI.UNDERLINE+"UNDERLINE");
            console.print(ANSI.BLACK+"BLACK");
            console.print(ANSI.RED+"RED");
            console.print(ANSI.GREEN+"GREEN");
            console.print(ANSI.YELLOW+"YELLOW");
            console.print(ANSI.BLUE+"BLUE");
            console.print(ANSI.MAGENTA+"MAGENTA");
            console.print(ANSI.CYAN+"CYAN");
            console.print(ANSI.WHITE+"WHITE");
            console.print(ANSI.LIGHT_RED+"LIGHT RED");
            console.print(ANSI.LIGHT_GREEN+"LIGHT GREEN");
            console.print(ANSI.LIGHT_BLUE+"LIGHT BLUE");
            console.print(ANSI.LIGHT_CYAN+"LIGHT CYAN");
            console.print(ANSI.LIGHT_PURPLE+"LIGHT PURPLE");
            return;
        }

        if(sender instanceof ConnectionRequestHandler) {
            DataLoader dl = ServiceLocator.getService(DataLoader.class);
            ConnectionRequestHandler client = (ConnectionRequestHandler) sender;
            client.sendServerMessage(dl.getMessage("unknown-command"));
        }
    }
}
