package core;

import commands.CommandSender;
import data.ANSI;
import org.jline.reader.*;
import org.jline.utils.AttributedString;

public class Console extends Thread implements CommandSender {

    private final ServerWrapper server;

    LineReader reader = LineReaderBuilder.builder().build();

    public Console(ServerWrapper server) {
        this.server = server;
    }

    @Override
    public void run() {

        String prompt = ANSI.GREEN+"[default] >>";
        while (true) {
            String cmd = null;
            try {
                cmd = reader.readLine(prompt);
            } catch (UserInterruptException e) {
                e.printStackTrace();

            } catch (EndOfFileException e) {
                e.printStackTrace();
            }
            // dispatch the command
            String name = cmd.split(" ")[0].replaceAll("/", "");
            String[] args = null;
            if(cmd.length() > name.length()+1) {
                args = cmd.substring(name.length()+2).split(" ");
            }else{
                args = new String[0];
            }
            server.dispatchCommand(this, name, args);
            print(ANSI.CYAN+"User123 issued a command /help");
            print(ANSI.CYAN+"User123 issued a command "+ANSI.WHITE+ANSI.BG_RED+"/sudo /ban user4");
            print(ANSI.CYAN+"[global]User123 ko staa");
            print(ANSI.CYAN+"User5 issued a command /msg User123 opaa");
            print(ANSI.BOLD+"BOLD");
            print(ANSI.UNDERLINE+"UNDERLINE");
            print(ANSI.BLACK+"BLACK");
            print(ANSI.RED+"RED");
            print(ANSI.GREEN+"GREEN");
            print(ANSI.YELLOW+"YELLOW");
            print(ANSI.BLUE+"BLUE");
            print(ANSI.MAGENTA+"MAGENTA");
            print(ANSI.CYAN+"CYAN");
            print(ANSI.WHITE+"WHITE");
            print(ANSI.LIGHT_RED+"LIGHT RED");
            print(ANSI.LIGHT_GREEN+"LIGHT GREEN");
            print(ANSI.LIGHT_BLUE+"LIGHT BLUE");
            print(ANSI.LIGHT_CYAN+"LIGHT CYAN");
            print(ANSI.LIGHT_PURPLE+"LIGHT PURPLE");

        }
    }


    public void print(String line) {
        reader.printAbove(new AttributedString(line+ANSI.RESET));
    }
}
