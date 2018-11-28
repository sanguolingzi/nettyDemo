package business.demo2;

import business.test.kryoProtocal.KryoUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class HttpHandler extends ChannelInboundHandlerAdapter {

    private Map<String,ChannelHandlerContext> clientMap;

    public HttpHandler(Map<String,ChannelHandlerContext> clientMap) {
        super();
        this.clientMap = clientMap;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        System.out.println("---- channelRegistered---"+ctx.channel().id());
        if(!clientMap.containsKey(ctx.channel().id().toString())){
            clientMap.put(ctx.channel().id().toString(),ctx);
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

        System.out.println("----"+msg.getClass().getSimpleName());
        System.out.println("server 收到消息！:"+KryoUtil.parseSerializable((byte[])msg));
        ctx.channel().writeAndFlush("你好客户端 这是服务端发送的消息："+KryoUtil.doSerializable(LocalDateTime.now()));
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
        if(null != cause){
            cause.printStackTrace();
        }
        if(null != ctx){
            clientMap.remove(ctx.channel().id().toString());
            ctx.close();
        }
    }
}
