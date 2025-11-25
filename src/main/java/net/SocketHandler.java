package net;

import com.swiftcryptollc.crypto.interfaces.KyberPrivateKey;
import com.swiftcryptollc.crypto.provider.*;
import crypto.AES;
import json.Json;
import main.Main;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.util.HashMap;
import java.util.UUID;

import static main.Main.logger;

public class SocketHandler extends Thread{

    private Socket client;
    public final String address;
    public final InputWrapper in;
    public final OutputWrapper out;
    public AES aes;
    private String aad;

    public SocketHandler(String addr, Socket client) throws IOException {
        this.client = client;
        this.address = addr;
        this.in = new InputWrapper(new BufferedInputStream(this.client.getInputStream()));
        this.out = new OutputWrapper(new BufferedOutputStream(this.client.getOutputStream()));
        handshake();
    }

    @Override
    public void run() {
        try {
            while (true) {
                String line = aes.decryptWithAAD(in.readBytes());

                //Parse some JSON and Send it to the right person
                /**
                 *  {
                 *      from:"<sender-uuid>", //Inspect this and verify that the sender details are accurate (based on aad info).
                 *      to:"<receiver-uuid>", //Use UUID 0 for receiver UUID to address the Server
                 *      content-type:"", //Standard Content type strings which will be passed to client
                 *      message:"<message-body>"
                 *  }
                 * */
                HashMap<String, Object> message = (HashMap<String, Object>) Json.parseObject(line);
                if (!((String) message.get("from")).equals(Main.directory.get(aad))) {
                    //This is really bad and may be malicious
                } else if (!Main.directory.contains(UUID.fromString((String) message.get("to")))) {
                    //Somebody made a Typo
                } else {
                    // Send it to the right person
                    out.writeBytes(aes.encryptWithAAD(line));
                    out.out.flush();
                }
            }
        } catch (Exception e) {
            if (!e.getMessage().equals("EOF"))
                logger.error(e.getMessage());
            this.close();
        }
    }

    private void handshake() {
        try {
            KeyPair keys = KeyPairGenerator.getInstance("Kyber1024").generateKeyPair();
            out.writeBytes(keys.getPublic().getEncoded());
            out.out.flush();
            KeyAgreement keyAgreement = KeyAgreement.getInstance("Kyber");
            keyAgreement.init((KyberPrivateKey) keys.getPrivate());
            KyberDecrypted kyberDecrypted = (KyberDecrypted) keyAgreement.doPhase(new KyberCipherText(in.readBytes()), true);
            aes = new AES(new SecretKeySpec(kyberDecrypted.getSecretKey().getS(), "AES"));
            aad = aes.decryptWithAAD(in.readBytes());
            aes.setAAD(aad);
            synchronized (Main.directory) {
                Main.directory.getOrDefault(aad);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public boolean close() {
        try {
            client.close();
            main.Main.clients.remove(address);
            synchronized (logger) {
                logger.info("Socket " + address + " was closed.");
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
