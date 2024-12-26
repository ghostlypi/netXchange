package server;

import com.swiftcryptollc.crypto.provider.KyberEncrypted;
import com.swiftcryptollc.crypto.provider.KyberSecretKey;
import main.Main;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class SocketHandler{

    private static final int PROTOCOL_VERSION = 1;

    public enum Status {
        OPEN,
        CLOSED
    }

    public Status status;
    private Socket socket;
    private ReadThread readAction;
    private Thread readThread;
    private WriteThread writeAction;
    private Thread writeThread;
    private Key secret;

    public SocketHandler(Socket s) {
        this.socket = s;
        try {
            BufferedInputStream in = new BufferedInputStream(this.socket.getInputStream());
            BufferedOutputStream out = new BufferedOutputStream(this.socket.getOutputStream());
            keyExchange(in, out);
            System.out.println(secret.getEncoded().length);
            readAction = new ReadThread(in, secret);
            readThread = new Thread(readAction);
            readThread.start();
            writeAction = new WriteThread(out, secret);
            writeThread = new Thread(writeAction);
            writeThread.start();
        } catch (IOException e) {
            Main.logger.error(e.getMessage());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Main.logger.warn("Non-Kyber Key has been sent");
        }
    }

    public boolean keyExchange(BufferedInputStream input, BufferedOutputStream output) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        this.status = Status.OPEN;
        int client_protocol = input.read();
        if (client_protocol != PROTOCOL_VERSION) {
            output.write("Protocol Mismatch, Aborting!\n".getBytes(StandardCharsets.UTF_16));
            output.write(0);
            this.status = Status.CLOSED;
            return false;
        } else {
            output.write("Protocol Match!\n".getBytes(StandardCharsets.UTF_16));
            output.write(1);
        }
        int pke_size = input.read();
        byte[] pke_bytes = new byte[pke_size];
        input.read(pke_bytes);
        KeyFactory kf = KeyFactory.getInstance("Kyber");
        PublicKey pke = kf.generatePublic(new X509EncodedKeySpec(pke_bytes));
        KeyAgreement ka = KeyAgreement.getInstance("Kyber");
        try {
            ka.init(Main.keys.getPrivate());
            KyberEncrypted ke = (KyberEncrypted) ka.doPhase(pke, true);
            output.write(ke.getCipherText().getEncoded());
            secret = ke.getSecretKey();
        } catch (InvalidKeyException e) {
            Main.logger.warn("Key Agreement Initialization failed. Unable to secure client communications");
            return false;
        }
        return true;
    }

    public void close() {
        try {
            readAction.close();
            writeAction.close();
            socket.close();
            this.status = Status.CLOSED;
        } catch (IOException e) {
            Main.logger.error("Error Closing Connection " + e.getMessage());
        }
    }
}
