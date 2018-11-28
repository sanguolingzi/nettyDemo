package business.demo2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpServer {
    private int port;

    public HttpServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        /*
        if (args.length != 1) {
            System.err.println(
                    "Usage: " + HttpServer.class.getSimpleName() +
                            " <port>");
            return;
        }
        */
        //int port = Integer.parseInt(args[0]);
        new HttpServer(8088).start();

        ConcurrentHashMap<String,ChannelHandlerContext> clientMap = new ConcurrentHashMap<>();


    }

    public void start() throws Exception {
        ServerBootstrap b = new ServerBootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        Map<String,ChannelHandlerContext> clientMap = new ConcurrentHashMap<>();

        try{
            b.group(group,workGroup)
                    .channel(NioServerSocketChannel.class)//ServerSocketChannel以NIO的selector为基础进行实现的 用来接收新的连接 这里告诉channel如何获得新连接
                    //添加过滤
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            System.out.println("initChannel ch:" + ch);
                            ch.pipeline()
                                    .addLast(new IdleStateHandler(5, 10, 0))
                                    //.addLast("decoder", new StringDecoder())   // 1
                                    //.addLast("encoder", new StringEncoder())  // 2
                                    .addLast("decoder",new StringDecoder())
                                    .addLast("encoder",new StringEncoder())
                                    .addLast("aggregator", new HttpObjectAggregator(512 * 1024))    // 3
                                    .addLast("handler", new HttpHandler(clientMap));// 4
                        }
                    })
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
            //cf.channel().close();


            //启动线程  模拟业务操作 从客户端缓存中获取客户端连接对象 并发送消息
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        if(!clientMap.keySet().isEmpty()){
                            int size = clientMap.keySet().size();
                            int index = new SecureRandom().nextInt(size);
                            System.out.println("------index------"+index+"......size:"+size);
                            AtomicInteger atomicInteger = new AtomicInteger(0);

                            Iterator<ChannelHandlerContext> it = clientMap.values().iterator();
                            while(it.hasNext()){
                                ChannelHandlerContext channelHandlerContext=  it.next();
                                if(atomicInteger.get()==index){
                                    if(channelHandlerContext.channel().isActive()){
                                        channelHandlerContext.channel().writeAndFlush("hi this is server message: index:"+index);

                                    }else{
                                        System.out.println("-------client is closed ------------"+channelHandlerContext);
                                        it.remove();
                                    }
                                    break;
                                }else{
                                    System.out.println(atomicInteger.get()+"----"+(atomicInteger.get()==index)+"---------"+index);
                                }
                                atomicInteger.incrementAndGet();
                            }
                        }else{
                            System.out.println("-------not found client------------");
                        }
                        try{
                            Thread.sleep(5000);
                            //System.out.println("-------休眠5s------------");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

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
