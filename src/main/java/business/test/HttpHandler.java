package business.test;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class HttpHandler extends ChannelInboundHandlerAdapter {

    private Map<String,Integer> heartBeat = new HashMap<>();

    public HttpHandler() {
        super();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        System.out.println("---- channelRegistered---"+ctx.channel().id());
        if(!heartBeat.containsKey(ctx.channel().id().toString())){
            heartBeat.put(ctx.channel().id().toString(),0);
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        System.out.println("---- channelUnregistered---");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.out.println("---- channelInactive---");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        System.out.println("---- userEventTriggered---"+ctx.channel().id());
        if (evt instanceof IdleStateEvent){
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt ;

            if (idleStateEvent.state() == IdleState.WRITER_IDLE){
                System.err.println("已经 10 秒没有发送信息！"+LocalDateTime.now());
                //向服务端发送消息
                //ctx.writeAndFlush("这是服务端发送心跳包:"+ LocalDateTime.now()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE) ;
            }else if(idleStateEvent.state() == IdleState.READER_IDLE){
                System.err.println("已经 5 秒没有读取信息！"+LocalDateTime.now());
                Integer count = heartBeat.get(ctx.channel().id().toString());
                if(count > 2){
                    heartBeat.remove(ctx.channel().id().toString());
                    System.err.println("关闭连接！"+ctx.channel().id().toString());
                    ctx.channel().close();
                }
                heartBeat.put(ctx.channel().id().toString(),++count);
            }
        }

        super.userEventTriggered(ctx, evt);



    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
        System.out.println("---- channelWritabilityChanged---");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        System.out.println("---- handlerAdded---"+ctx.channel().id());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        System.out.println("---- handlerRemoved---");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("server  channelActive");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("server 收到消息！:"+msg.toString());
        ctx.channel().writeAndFlush("你好客户端 这是服务端发送的消息："+LocalDateTime.now());

        //System.out.println("输入回复...");
        //java.util.Scanner scanner = new java.util.Scanner(System.in);
        //ctx.channel().writeAndFlush(scanner.next());

        //if(msg.equals("关闭连接")){
        //    //结束此次连接
        //    ctx.close();
        //}
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("server channelReadComplete！");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("exceptionCaught");
        if(null != cause) cause.printStackTrace();
        if(null != ctx) ctx.close();
    }
}
