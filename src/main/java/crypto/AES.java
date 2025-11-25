package crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;

public class AES {

    private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private SecureRandom random;
    private SecretKey key;
    private String aad;

    public AES (SecretKey key) {
        this.random = new SecureRandom();
        this.key = key;
    }

    public byte[] generateIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        random.nextBytes(iv);
        return iv;
    }

    public void setAAD(String aad) {
        this.aad = aad;
    }


    public byte[] encrypt(String plaintext) throws Exception {
        // Generate random IV
        byte[] iv = generateIV();

        // Initialize cipher
        Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

        // Encrypt
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes("UTF-16"));

        // Combine IV + ciphertext (includes auth tag)
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
        byteBuffer.put(iv);
        byteBuffer.put(ciphertext);

        return byteBuffer.array();
    }

    public byte[] encryptWithAAD(String plaintext) throws Exception {
        byte[] iv = generateIV();

        Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

        // Add AAD (Additional Authenticated Data)
        cipher.updateAAD(aad.getBytes("UTF-16"));

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes("UTF-16"));

        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
        byteBuffer.put(iv);
        byteBuffer.put(ciphertext);

        return byteBuffer.array();
    }

    public String decrypt(byte[] encryptedData) throws Exception {
        // Extract IV
        ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedData);
        byte[] iv = new byte[GCM_IV_LENGTH];
        byteBuffer.get(iv);

        // Extract ciphertext + tag
        byte[] ciphertext = new byte[byteBuffer.remaining()];
        byteBuffer.get(ciphertext);

        // Initialize cipher
        Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

        // Decrypt and verify authentication tag
        byte[] plaintext = cipher.doFinal(ciphertext);

        return new String(plaintext, "UTF-16");
    }

    public String decryptWithAAD(byte[] encryptedData) throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedData);
        byte[] iv = new byte[GCM_IV_LENGTH];
        byteBuffer.get(iv);

        byte[] ciphertext = new byte[byteBuffer.remaining()];
        byteBuffer.get(ciphertext);

        Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

        // Add AAD
        cipher.updateAAD(aad.getBytes("UTF-16"));

        byte[] plaintext = cipher.doFinal(ciphertext);

        return new String(plaintext, "UTF-16");
    }

}
