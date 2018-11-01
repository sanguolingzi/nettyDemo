package business.test;

import business.test.kryoProtocal.KryoUtil;
import io.netty.bootstrap.Bootstrap;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.security.Security;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.Scanner;

public class HttpClient {
    private final String host;
     private final int port;

             public HttpClient() {
                 this(0);
             }

             public HttpClient(int port) {
                 this("localhost", port);
             }

             public HttpClient(String host, int port) {
                 this.host = host;
                 this.port = port;
             }

             public void start() throws Exception {
                 EventLoopGroup group = new NioEventLoopGroup();
                 try {
                         Bootstrap b = new Bootstrap();
                         b.group(group) // 注册线程池
                                 .channel(NioSocketChannel.class) // 使用NioSocketChannel来作为连接用的channel类
                                 .remoteAddress(new InetSocketAddress(this.host, this.port)) // 绑定连接端口和host信息
                                 .handler(new ChannelInitializer<SocketChannel>() { // 绑定连接初始化器
                                 @Override
                                protected void initChannel(SocketChannel ch) throws Exception {
                                    System.out.println("connected...");

                                     ChannelPipeline pipeline = ch.pipeline();
                                     //pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                                     //pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                                     pipeline.addLast(new IdleStateHandler(5, 10, 0));
                                     //pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
                                     //pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));



                                     pipeline.addLast("decoder",new ByteArrayDecoder());
                                     pipeline.addLast("encoder",new ByteArrayEncoder());

                                     pipeline.addLast("handler", new HttpClientHandler());

                                                     }
                             });
                         System.out.println("created..");

                         ChannelFuture cf = b.connect().sync(); // 异步连接服务器
                         //System.out.println("connected..."); // 连接完成



                         new Thread(new Runnable() {
                             @Override
                             public void run() {
                                while(true){

                                    try{
                                        if(cf.channel().isActive()){
                                            //cf.channel().writeAndFlush(Thread.currentThread().getName()+"客户端发送内容:"+ LocalDateTime.now());
                                            cf.channel().writeAndFlush(KryoUtil.doSerializable(Thread.currentThread().getName()+"客户端发送内容:"+ LocalDateTime.now()));
                                            //cf.channel().writeAndFlush(new byte[1024]);
                                        }else{
                                            System.out.println("......break...........");
                                            break;
                                        }
                                        System.out.println(Thread.currentThread().getName()+"客户端发送线程休眠 : start");
                                        Thread.currentThread().sleep(7000);
                                        //break;
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                             }
                         }).start();

                         /*
                         System.out.println("输入信息...");
                         java.util.Scanner scanner = new java.util.Scanner(System.in);

                         String str = scanner.next();

                         while(!"关闭连接".equals(str)){

                           // 关闭完成
                             cf.channel().writeAndFlush(str);
                             System.out.println("消息发送完毕.."); // 关闭完成
                             str = scanner.next();
                         }
                         cf.channel().writeAndFlush(str);
                         */

                         cf.channel().closeFuture().sync(); // 异步等待关闭连接channel
                         System.out.println("closed.."); // 关闭完成
                     } finally {
                         group.shutdownGracefully().sync(); // 释放线程池资源
                         //System.out.print("准备重连!");
                         //start();
                         //System.out.print("重连完毕!");
                     }
             }

             public static void main(String[] args) throws Exception {
                 new HttpClient("localhost", 8081).start(); // 连接127.0.0.1/65535，并启动
             }
}
