package uk.co.deveroonie.hyvote.util;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
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

            String privatePEM = "-----BEGIN PRIVATE KEY-----\n" +
                    privateKey +
                    "\n-----END PRIVATE KEY-----";

            Files.writeString(getDataDir().resolve("private.key"), privatePEM, StandardCharsets.US_ASCII);

            String publicPEM = "-----BEGIN PUBLIC KEY-----\n" +
                    publicKey +
                    "\n-----END PUBLIC KEY-----";

            Files.writeString(getDataDir().resolve("public.key"), publicPEM, StandardCharsets.US_ASCII);
    }

    public static PrivateKey getPrivateKey() throws Exception {
        String pem = Files.readString(
                getDataDir().resolve("private.key"),
                StandardCharsets.US_ASCII
        );

        String base64 = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", ""); // removes newlines & spaces

        byte[] decoded = Base64.getDecoder().decode(base64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePrivate(spec);
    }
}
