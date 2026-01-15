package uk.co.deveroonie.hyvote.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import uk.co.deveroonie.hyvote.util.Keys;
import uk.co.deveroonie.hyvote.util.ProcessVote;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MessageHandler extends ChannelInboundHandlerAdapter {
    private ByteArrayOutputStream buffer;
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        buffer = new ByteArrayOutputStream();
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        try {
            // Copy bytes from ByteBuf to our buffer
            in.readBytes(buffer, in.readableBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            in.release(); // Important! Release the ByteBuf
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        byte[] fullMessage = buffer.toByteArray();

        if (fullMessage.length >= 4 &&
                fullMessage[0] == 'H' &&
                fullMessage[1] == 'V' &&
                fullMessage[2] == '0' &&
                fullMessage[3] == '1') {
            ByteBuffer buf = ByteBuffer.wrap(fullMessage);
            buf.position(4);
            int keyLength = buf.getInt();
            byte[] encryptedKey = new byte[keyLength];
            buf.get(encryptedKey);

            int payloadLength = buf.getInt();
            byte[] encryptedPayload = new byte[payloadLength];
            buf.get(encryptedPayload);
            // make sure they aren't doing anything outrageous
            if (keyLength < 0 || keyLength > 512) {
                ctx.close();
                return;
            }
            if (payloadLength < 0 || payloadLength > 8192) {
                ctx.close();
                return;
            }
            if (buf.remaining() < keyLength + 4 + payloadLength) {
                ctx.close();
                return;
            }

            // probably okay now!

            // decrypt the AES key
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, Keys.getPrivateKey());

            byte[] aesKeyBytes = cipher.doFinal(encryptedKey);

            // decrypt the REAL bytes
            byte[] iv = Arrays.copyOfRange(encryptedPayload, 0, 16); // First 16 bytes
            byte[] actualPayload = Arrays.copyOfRange(encryptedPayload, 16, encryptedPayload.length);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");
            aesCipher.init(Cipher.DECRYPT_MODE, aesKey, ivSpec);

            byte[] voteMessage = cipher.doFinal(actualPayload);

            // Pass it down for processing
            new ProcessVote(voteMessage);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
