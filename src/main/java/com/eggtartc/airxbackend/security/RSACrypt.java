package com.eggtartc.airxbackend.security;

import org.springframework.stereotype.Component;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;
import javax.crypto.Cipher;

@Component
class RSACrypt {
    public String encrypt(String plaintext, RSAPublicKey key) {
        return Arrays.stream(chunkedString(plaintext, 32))
            .map(it -> encryptPart(it, key))
            .collect(Collectors.joining("-"));
    }

    public String decrypt(String ciphertext, RSAPrivateKey key) {
        return Arrays.stream(ciphertext.split("-"))
            .map(it -> decryptPart(it, key))
            .collect(Collectors.joining(""));
    }

    private String[] chunkedString(String s, int chunkSize) {
        String[] chunks = new String[(s.length() + chunkSize - 1) / chunkSize];
        for (int i = 0; i < chunks.length; i++) {
            chunks[i] = s.substring(i * chunkSize, Math.min(s.length(), (i + 1) * chunkSize));
        }
        return chunks;
    }

    public String encryptPart(String plaintext, RSAPublicKey key) {
        return Base64.getEncoder().encodeToString(
            rsaEncrypt(plaintext.getBytes(), key)
        );
    }

    public String decryptPart(String ciphertext, RSAPrivateKey key) {
        return new String(
            rsaDecrypt(Base64.getDecoder().decode(ciphertext), key)
        );
    }

    private byte[] rsaEncrypt(byte[] data, RSAPublicKey key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] rsaDecrypt(byte[] data, RSAPrivateKey key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
