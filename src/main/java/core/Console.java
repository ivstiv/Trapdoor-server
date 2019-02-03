package core;

import commands.CommandSender;
import data.ANSI;
import data.DataLoader;
import org.jline.reader.*;
import org.jline.utils.AttributedString;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Console extends Thread implements CommandSender {

    private final ServerWrapper server;
    private LineReader reader = LineReaderBuilder.builder().build();

    private volatile String mode = "default";
    private String[] array = {"default", "traffic", "debug", "commands-only"};
    private List<String> availableModes = Arrays.asList(array);

    private volatile boolean running = true;


    public Console(ServerWrapper server) {
        this.server = server;
    }

    @Override
    public void run() {
        DataLoader dl = ServiceLocator.getService(DataLoader.class);

        String prompt = ANSI.GREEN+"["+getMode()+"] >>";
        while (running) {
            String cmd = null;
            try {
                cmd = reader.readLine(prompt);
            } catch (UserInterruptException | EndOfFileException e) {
                e.printStackTrace();
            }

            // dispatch the command
            cmd = cmd.replaceAll("/", "");
            String name = cmd.split(" ")[0];
            String[] args = null;

            if(cmd.length() > name.length()+1) {
                args = cmd.substring(name.length()+1).split(" ");
            }else{
                args = new String[0];
            }

            if(name.equals("mode")) {
                //check for args
                if(args.length < 1) {
                    print(dl.getMessage("cl-missing-argument"));
                    continue;
                }

                // check if argument is valid
                if(!availableModes.contains(args[0])) {
                    print(dl.getMessage("cl-invalid-argument"));
                    continue;
                }
                // set the new mode of the console
                setMode(args[0]);
                prompt = ANSI.GREEN+"["+args[0]+"] >>";
                print("Switched to mode: "+args[0]);

            }else{
                server.dispatchCommand(this, name, args);
            }






            /*
            print(ANSI.CYAN+"User123 issued a command /help");
            print(ANSI.CYAN+"User123 issued a command "+ANSI.WHITE+ANSI.BG_RED+"/sudo /ban user4");
            printError(ANSI.CYAN+"[global]User123 ko staa");
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
            */
        }
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void print(String line) {
        // append time and format the message
        LocalDateTime now = LocalDateTime.now();
        String time = now.format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH));
        String msgFormat = String.format("[%s] %s%s", time, line, ANSI.RESET);

        reader.printAbove(new AttributedString(msgFormat));
    }

    public void printError(String line) {
        print(ANSI.BG_RED+ANSI.BOLD+"[ERROR]"+line);
    }

    public void stopConsole() {
        running = false;
        reader.getTerminal().reader().shutdown();
        reader.getTerminal().writer().close();
    }
}
