package logger;

import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentLinkedQueue;

import static main.Main.USER_HOME;

public class Logger implements System.Logger, Runnable {
    private Log log;
    private Queue<Log.Message> buffer;
    public boolean stop;

    public Logger() {
        String log_folder = USER_HOME + "/.netXchange/logs";
        String log_file = "comprehensive.log";
        System.out.println("Log Path: " + log_folder + "/" + log_file);
        //Ensure netXchange folder exists and is set up
        File logFolder = new File(log_folder);
        File logFile = new File(log_folder+"/"+log_file);
        if (!logFolder.exists()) {
            logFolder.mkdirs();
        }
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        log = new Log(log_folder+"/"+log_file);
        buffer = new ConcurrentLinkedQueue<>();
        stop = true;
    }

    @Override
    public String getName() {
        return "netXchange logger";
    }

    @Override
    public boolean isLoggable(Level level) {
        return switch (level) {
            case Level.INFO, Level.WARNING, Level.ERROR, Level.ALL -> true;
            default -> false;
        };
    }

    @Override
    public void log(Level level, ResourceBundle resourceBundle, String s, Throwable throwable) {
        log(level, s);
    }

    @Override
    public void log(Level level, ResourceBundle resourceBundle, String s, Object... objects) {
        log(level, s);
    }

    public void log(Level level, String s) {
        switch (level) {
            case Level.INFO:
                buffer.add(new Log.Message(s, Log.Message.Priority.INFO));
                break;
            case Level.WARNING:
                buffer.add(new Log.Message(s, Log.Message.Priority.WARN));
                break;
            case Level.ERROR:
                buffer.add(new Log.Message(s, Log.Message.Priority.ERROR));
                break;
            case Level.ALL:
                buffer.add(new Log.Message(s, Log.Message.Priority.CRITICAL));
                break;
            default:
                throw new RuntimeException("Unknown Priority " + level);
        }
    }

    public void info(String s) {
        log(Level.INFO, s);
    }

    public void warn(String s) {
        log(Level.WARNING, s);
    }

    public void error(String s) {
        log(Level.ERROR, s);
    }

    public void crit(String s) {
        log(Level.ALL, s);
    }

    public boolean isFlushed() {
        return buffer.isEmpty();
    }

    public boolean save_clear() {
        boolean out = log.save();
        log.clear();
        return out;
    }

    @Override
    public void run() {
        stop = false;
        while (!stop) {
            while (!buffer.isEmpty()) {
                Log.Message m = buffer.poll();
                log.push(m);
                log.printmessage(-1);
            }
            log.save();
            log.clear();
        }
    }
}
