package business.demo.file;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class HttpStaticFileServer
{
    private final int port;

    public HttpStaticFileServer(int port)
    {
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            ((ServerBootstrap)b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class))
                    .childHandler(new HttpStaticFileServerInitializer());

            b.bind(this.port).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args)
            throws Exception
    {
        int port;
        if (args.length > 0)
            port = Integer.parseInt(args[0]);
        else {
            port = 8089;
        }
        new HttpStaticFileServer(port).run();
    }
}