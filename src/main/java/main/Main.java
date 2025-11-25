package main;


import com.swiftcryptollc.crypto.provider.KyberJCE;
import directory.Directory;
import logger.Logger;
import net.SocketHandler;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.UUID;

/** WARNING: NOT FOR PRODUCTION USE
 *
 *      Feel free to audit this program, make PRs and otherwise contribute to its security.
 * */

public class Main {

    //System Properties
    public static final String USER_HOME = System.getProperty("user.home");
    public static final int PORT = Integer.parseInt(System.getenv("NETXCHANGE_PORT"));

    //Server variables
    public static boolean run = true;
    public static KeyPair keys = null;
    private static ServerSocket socket = null;

    //Logger
    public static final Logger logger = new Logger();
    public static final Thread logThread = new Thread(logger);

    //User Directory
    public static final Directory directory = new Directory();

    //Clients
    public static final HashMap<String, SocketHandler> clients = new HashMap<>();

    public static void main(String[] args) {
        init();

        try {
            while (run) {
                Socket client = socket.accept();
                String addr = (client.getInetAddress().toString() + ":" + client.getPort()).substring(1);
                logger.info("Gained a connection from: " + addr);
                clients.put(addr, new SocketHandler(addr, client));
                clients.get(addr).start();
            }
        } catch (IOException e) {
            logger.crit(e.getMessage());
        }

        shutdown();
    }

    public static void init() {
        //Register Kyber Implementation
        Security.setProperty("crypto.policy", "unlimited");
        Security.addProvider(new KyberJCE());

        //Start Logger Thread
        logThread.start();
        logger.error("SERVER IS STARTING...");

        //Register the SIGINT HANDLER
        Signal.handle(new Signal("INT"), new SignalHandler() {
            public void handle(Signal sig) {
                logger.warn("SIGINT received. Initiating shutdown...");
                run = false;
                try {socket.close();} catch (IOException ignore) {}
            }
        });

        try {
            socket = new ServerSocket(31415);
        } catch (IOException e) {
            logger.crit("Unable to bind to socket " + PORT);
            shutdown();
        }
    }

    public static void shutdown() {
        logger.crit("...SERVER IS SHUTTING DOWN!");

        //Clear all the clients
        for (SocketHandler client : clients.values()) {
            client.close();
            try {Thread.sleep(2);} catch (InterruptedException ignore){}
        }



        //Wait for the logger to flush the log
        while (!logger.isFlushed()) {}
        logger.save_clear();

        //Stop the Logger & Exit
        synchronized (logThread) {logger.stop = true;}
        System.exit(0);
    }
}