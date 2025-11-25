package main;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

import com.swiftcryptollc.crypto.interfaces.KyberPublicKey;
import com.swiftcryptollc.crypto.provider.KyberEncrypted;
import com.swiftcryptollc.crypto.provider.KyberJCE;
import crypto.AES;
import net.InputWrapper;
import net.OutputWrapper;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;

public class Main {

    public static boolean run;
    public static Socket server;
    public static AES aes;

    public static byte[] init(X509EncodedKeySpec keySpec) {
        Security.setProperty("crypto.policy", "unlimited");
        Security.addProvider(new KyberJCE());
        try {
            KeyPair keys = KeyPairGenerator.getInstance("Kyber1024").generateKeyPair();
            KeyAgreement keyAgreement = KeyAgreement.getInstance("Kyber");
            keyAgreement.init(keys.getPrivate());
            KeyFactory kf = KeyFactory.getInstance("Kyber");
            KyberEncrypted kyberEncrypted = (KyberEncrypted) keyAgreement.doPhase((KyberPublicKey) kf.generatePublic(keySpec), true);
            aes = new AES(new SecretKeySpec(kyberEncrypted.getSecretKey().getS(), "AES"));
            return kyberEncrypted.getCipherText().getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        //Register the SIGINT HANDLER
        Signal.handle(new Signal("INT"), new SignalHandler() {
            public void handle(Signal sig) {
                System.out.println("SIGINT received. Initiating shutdown...");
                run = false;
                try { server.close(); } catch (IOException ignore) {}
                System.exit(0);
            }
        });

        try {
            server = new Socket("127.0.0.1", 31415);
            InputWrapper in = new InputWrapper(new BufferedInputStream(server.getInputStream()));
            OutputWrapper out = new OutputWrapper(new BufferedOutputStream(server.getOutputStream()));
            BufferedReader usr = new BufferedReader(new InputStreamReader(System.in));
            run = true;
            byte[] cipherText = init(new X509EncodedKeySpec(in.readBytes()));
            if (cipherText != null) {
                out.writeBytes(cipherText);
                out.out.flush();
            } else {
                throw new RuntimeException("cipherText is NULL!");
            }
            try {
                byte[] baad = new byte[1024];
                new SecureRandom().nextBytes(baad);
                String aad = new String(baad, "UTF-16");
                out.writeBytes(aes.encrypt(aad));
                out.out.flush();
                aes.setAAD(aad);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread t = new Thread(() -> {
                try {
                    while (run) {
                        System.out.println(aes.decryptWithAAD(in.readBytes()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.getMessage().equals("EOF")) {
                        run = false;
                    }
                }
            });
            t.start();
            while (run) {
                String line = usr.readLine();
                out.writeBytes(aes.encryptWithAAD(line));
                out.out.flush();
            }
            server.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
