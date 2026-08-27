package org.acme.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyGenerator {
    public static void main(String[] args) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();
            PrivateKey privateKey = pair.getPrivate();
            PublicKey publicKey = pair.getPublic();

            // Directorio destino
            String dir = "src/main/resources/META-INF/resources/";

            // Guardar clave privada
            PKCS8EncodedKeySpec pkcs8Spec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
            String privateKeyPem = "-----BEGIN PRIVATE KEY-----\n" +
                    Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(pkcs8Spec.getEncoded()) +
                    "\n-----END PRIVATE KEY-----";
            try (FileOutputStream fos = new FileOutputStream(dir + "privateKey.pem")) {
                fos.write(privateKeyPem.getBytes());
            }

            // Guardar clave pública
            X509EncodedKeySpec x509Spec = new X509EncodedKeySpec(publicKey.getEncoded());
            String publicKeyPem = "-----BEGIN PUBLIC KEY-----\n" +
                    Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(x509Spec.getEncoded()) +
                    "\n-----END PUBLIC KEY-----";
            try (FileOutputStream fos = new FileOutputStream(dir + "publicKey.pem")) {
                fos.write(publicKeyPem.getBytes());
            }

            System.out.println("Claves generadas correctamente en " + dir);
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
    }
}
