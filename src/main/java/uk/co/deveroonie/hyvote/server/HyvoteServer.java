package uk.co.deveroonie.hyvote.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import uk.co.deveroonie.hyvote.HyvotePlugin;

import java.net.InetSocketAddress;
import java.util.logging.Level;

public class HyvoteServer {
    private int port;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public HyvoteServer() {
        this.port = HyvotePlugin.getSettings().port;
    }

    public void start() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) {
                        channel.pipeline().addLast(new MessageHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture future = bootstrap.bind(this.port);

        future.addListener((f) -> {
            if(f.isSuccess()) {
                int port = ((InetSocketAddress) future.channel().localAddress()).getPort();
                HyvotePlugin.getLog().at(Level.INFO).log("Bound to port " + port);
            } else {
                HyvotePlugin.getLog().at(Level.SEVERE).log("Failed to bind to port " + this.port, f.cause());
            }
        });
    }

    public void stop() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
    }
}