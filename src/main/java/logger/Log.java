package logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;

public class Log {
    private String file;
    private ArrayList<Message> log;

    static class Message {
        enum Priority {
            INFO,
            WARN,
            ERROR,
            CRITICAL
        }

        Priority priority;
        Timestamp timestamp;
        String msg;

        Message (String msg, Priority priority) {
            this.priority = priority;
            this.timestamp = new Timestamp(System.currentTimeMillis());
            this.msg = msg;
        }

        @Override
        public String toString() {
            return "[" + timestamp.toString() + "](" + priority.toString() + "):" + msg;
        }
    }

    public Log (String file) {
        this.file = file;
        this.log = new ArrayList<>();
    }

    public boolean push(String priority, String message) {
        switch (priority) {
            case "info":
                this.log.add(new Message(message, Message.Priority.INFO));
                return true;
            case "warn":
                this.log.add(new Message(message, Message.Priority.WARN));
                return true;
            case "err":
                this.log.add(new Message(message, Message.Priority.ERROR));
                return true;
            case "crit":
                this.log.add(new Message(message, Message.Priority.CRITICAL));
                return true;
            default:
                return false;
        }
    }

    public void push(Message m) {
        log.add(m);
    }

    public boolean save() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            for (Message m : log) {
                writer.write(m.toString());
                writer.newLine();
            }
            writer.flush();
            writer.close();
            push("warn","Saved log to file");
            return true;
        } catch (IOException e) {
            push("crit","Failed to save log to file!");
            return false;
        }
    }

    public void clear() {
        Message last = log.getLast();
        log.clear();
        log.add(last);
    }

    public void printmessage(int i) {
        //Colors
        String ANSI_RESET = "\u001B[0m";
        String ANSI_RED = "\u001B[1;31m";
        String ANSI_YELLOW = "\u001B[33m";
        String ANSI_PURPLE = "\u001B[41m\u001B[1;30m";

        //Fetch the Message
        Message msg = i == -1 ? log.getLast() : log.get(i);

        //Color the Print Based on Priority!
        switch (msg.priority) {
            case Message.Priority.WARN:
                System.out.println(ANSI_YELLOW + msg + ANSI_RESET);
                break;

            case Message.Priority.ERROR:
                System.out.println(ANSI_RED + msg + ANSI_RESET);
                break;

            case Message.Priority.CRITICAL:
                System.out.println(ANSI_PURPLE + msg + ANSI_RESET);
                break;

            default:
                System.out.println(msg);
                break;
        }
    }
}
