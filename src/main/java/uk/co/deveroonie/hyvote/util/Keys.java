package uk.co.deveroonie.hyvote.util;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import static uk.co.deveroonie.hyvote.HyvotePlugin.getDataDir;

public class Keys {
    public static void saveKeys() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);

        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        String privateKey = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(keyPair.getPrivate().getEncoded());
        String publicKey = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(keyPair.getPublic().getEncoded());

        try (FileOutputStream stream = new FileOutputStream(getDataDir().resolve("private.key").toFile())) {
            String pem = "-----BEGIN PRIVATE KEY-----\n" +
                    privateKey +
                    "\n-----END PRIVATE KEY-----";

            stream.write(pem.getBytes());
        }

        try (FileOutputStream stream = new FileOutputStream(getDataDir().resolve("public.key").toFile())) {
            String pem = "-----BEGIN PUBLIC KEY-----\n" +
                    publicKey +
                    "\n-----END PUBLIC KEY-----";

            stream.write(pem.getBytes());
        }
    }

    public static PrivateKey getPrivateKey() throws Exception {
        String base64 = Files.readString(getDataDir().resolve("private.key"));
        byte[] decoded = Base64.getDecoder().decode(base64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePrivate(spec);
    }
}
