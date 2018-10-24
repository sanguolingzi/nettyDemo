package business.websocket.server;

import business.websocket.channelinitalizer.WebSocketChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class WebSocketServer {

    private int port = 8088;

    public static void  main(String[] args) throws Exception{
        new WebSocketServer().start();
    }

    public void start() throws Exception {
        ServerBootstrap b = new ServerBootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        try{
            b.group(group,workGroup)
                    .channel(NioServerSocketChannel.class)//ServerSocketChannel以NIO的selector为基础进行实现的 用来接收新的连接 这里告诉channel如何获得新连接
                    //添加过滤
                    .childHandler(new WebSocketChannelInitializer())
                    /**
                     * 这里可以设置指定通道channel的配置参数 请参考ChannelOption的详细的ChannelConfig实现的接口文档
                     */
                    .option(ChannelOption.SO_BACKLOG, 128) // determining the number of connections queued
                    /**
                     *  option()是提供给ServerSocketChannel
                     *  childOption()是提供给父管道ServerChannel接收到的连接
                     */
                    .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE);

            ChannelFuture cf = b.bind(port).sync();
            if(cf.isSuccess()){
                System.out.println("netty start success");
            }

            /**
             * 这里一直等待直到连接被关闭
             */
            cf.channel().closeFuture().sync();
        }catch (Exception e){
            System.out.println("netty start error");
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
