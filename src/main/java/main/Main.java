package main;

import com.swiftcryptollc.crypto.interfaces.KyberPublicKey;
import com.swiftcryptollc.crypto.provider.KyberJCE;
import com.swiftcryptollc.crypto.provider.KyberKeyFactory;
import logger.Logger;
import server.SocketHandler;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

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

    //Clients
    public static final ArrayList<SocketHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        init();

        try {
            socket = new ServerSocket(PORT);
            while (run) {
                Socket client = socket.accept();
                clients.add(new SocketHandler(client));
            }
        } catch (IOException e) {
            if (!run)
                logger.info("Socket Terminated due to SIGINT");
            else
                logger.crit("Unable to bind to socket " + PORT);
        }

        shutdown();
    }

    public static void init() {
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

        //Initialize KyberJCE
        Security.setProperty("crypto.policy", "unlimited");
        Security.addProvider(new KyberJCE());
        if (keysExist()) {
            keys = loadKeyPair();
        } else {
            try {
                keys = KeyPairGenerator.getInstance("Kyber1024").generateKeyPair();
            } catch (NoSuchAlgorithmException e) {
                logger.log(System.Logger.Level.ALL, e.getMessage());
            }
            saveKeyPair(keys);
        }
    }

    public static void shutdown() {
        logger.crit("...SERVER IS SHUTTING DOWN!");

        //Clear all the clients
        for (SocketHandler client : clients) {
            client.close();
        }

        //Wait for the logger to flush the log
        while (!logger.isFlushed()) {continue;}

        //Stop the Logger & Exit
        synchronized (logThread) {logger.stop = true;}
        System.exit(0);
    }

    public static boolean saveKeyPair(KeyPair pair) {
        //Existence Checks
        File folder = new File(USER_HOME+"/.netXchange/keys");
        File pub = new File(USER_HOME+"/.netXchange/keys/single.pub");
        File pri = new File(USER_HOME+"/.netXchange/keys/single");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        if (!pub.exists()) {
            try {
                pub.createNewFile();
            } catch (IOException e) {
                logger.warn("Do not have permissions to access netXchange files.");
                return false;
            }
        }

        if (!pri.exists()) {
            try {
                pri.createNewFile();
            } catch (IOException e) {
                logger.warn("Do not have permissions to access netXchange files.");
                return false;
            }
        }

        try (FileOutputStream fos = new FileOutputStream(pub)) {
            fos.write(pair.getPublic().getEncoded());
        } catch (FileNotFoundException ignore) {
            logger.crit("Existentiality Breakdown!");
            return false;
        } catch (IOException e) {
            logger.error("Unable to write key due to " + e.getMessage());
        }

        try (FileOutputStream fos = new FileOutputStream(pri)) {
            fos.write(pair.getPrivate().getEncoded());
        } catch (FileNotFoundException ignore) {
            logger.crit("Existentiality Breakdown!");
            return false;
        } catch (IOException e) {
            logger.error("Unable to write key due to " + e.getMessage());
        }
        return true;
    }

    public static KeyPair loadKeyPair() {
        File pub = new File(USER_HOME+"/.netXchange/keys/single.pub");
        File pri = new File(USER_HOME+"/.netXchange/keys/single");
        if (pub.exists() && pri.exists()) {
            try {
                byte[] keybytes = Files.readAllBytes(pub.toPath());
                KeyFactory kf = KeyFactory.getInstance("Kyber");
                PublicKey pke = kf.generatePublic(new X509EncodedKeySpec(keybytes));
                keybytes = Files.readAllBytes(pri.toPath());
                PrivateKey pki = kf.generatePrivate(new PKCS8EncodedKeySpec(keybytes));
                return new KeyPair(pke, pki);
            } catch (IOException e) {
                logger.error("Cannot Access netXchange key files!");
                return null;
            } catch (NoSuchAlgorithmException e) {
                logger.error("Missing Kyber implementation!");
                return null;
            } catch (InvalidKeySpecException e) {
                logger.error("Public or Private Key has been corrupted!");
                return null;
            }
        } else {
            logger.warn("Keys do not exist, but system still attempted load!");
            return null;
        }
    }

    public static boolean keysExist() {
        File pub = new File(USER_HOME+"/.netXchange/keys/single.pub");
        File pri = new File(USER_HOME+"/.netXchange/keys/single");
        return pub.exists() && pri.exists();
    }
}