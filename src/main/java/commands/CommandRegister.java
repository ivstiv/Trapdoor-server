package commands;

import communication.ConnectionRequestHandler;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

// class that holds all registered executors on load
public class CommandRegister {

    private BlockingQueue<Command> dispatchedCommands = new LinkedBlockingDeque<>();
    private HashMap<String, CommandExecutor> executors = new HashMap<>();
    private Thread commandListener;
    private volatile boolean running = true;

    public CommandRegister() {
        commandListener = new Thread(runCommandListener());
        commandListener.start();
    }

    public Thread getCommandListener() {
        return commandListener;
    }

    public void registerCommand(String command, CommandExecutor executor) {
        executors.put(command, executor);
    }
    public void unregisterCommand(String command) {
        executors.remove(command);
    }

    public Set<String> registeredCommands() {
        return executors.keySet();
    }

    //return true if the command exists
    public boolean dispatch(Command cmd) {
        if(executors.containsKey(cmd.getName())) {
            dispatchedCommands.add(cmd);
            return true;
        }else{
            return false;
        }
    }

    private Runnable runCommandListener() {
        return () -> {
            while(running) {
                try {
                    Command cmd = dispatchedCommands.take();
                    executors.get(cmd.getName()).onCommand(cmd.getSender(), cmd.getName(), cmd.getArgs());

                    if(cmd.getName().equals("sudo")) continue; // do not delete the sudo session after a /sudo command
                    if(cmd.getSender() instanceof ConnectionRequestHandler) {
                        ConnectionRequestHandler client = (ConnectionRequestHandler) cmd.getSender();
                        // clean the sudo session
                        if(client.getClientData().hasSudoSession())
                            client.getClientData().destroySudoSession();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };
    }

    public void stopListeners() {
        running = false;
    }
}
