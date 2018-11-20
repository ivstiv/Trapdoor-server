package commands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;

// class that holds all registered commands on load
public class CommandRegister {

    private BlockingQueue<Command> dispatchedCommands = new LinkedBlockingDeque<>();
    private HashMap<String, CommandExecutor> commands = new HashMap<>();
    private Thread commandListener;

    public CommandRegister() {
        commandListener = new Thread(runCommandListener());
        commandListener.start();
    }

    public Thread getCommandListener() {
        return commandListener;
    }

    public void registerCommand(String command, CommandExecutor executor) {
        commands.put(command, executor);
    }

    public Set<String> registeredCommands() {
        return commands.keySet();
    }

    //return true if the command exists
    public boolean dispatch(Command cmd) {
        if(commands.containsKey(cmd.getName())) {
            dispatchedCommands.add(cmd);
            return true;
        }else{
            return false;
        }
    }

    private Runnable runCommandListener() {
        return () -> {
            while(true) {
                try {
                    Command cmd = dispatchedCommands.take();
                    commands.get(cmd.getName()).onCommand(cmd.getSender(), cmd.getName(), cmd.getArgs());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };
    }
}
