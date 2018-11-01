package business.test;

import business.test.kryoProtocal.KryoUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.time.LocalDateTime;

public class HttpClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //ctx.writeAndFlush("this is client");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("client channelRead..");

        System.out.println("msg:"+ KryoUtil.parseSerializable((byte[])msg));

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client channelReadComplete..");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt ;

            if (idleStateEvent.state() == IdleState.READER_IDLE){
                System.err.println("已经5秒没有收到信息！"+LocalDateTime.now());
                //向客户端发送消息
                //ctx.writeAndFlush("这是客户端发送的心跳包："+LocalDateTime.now()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE) ;
            }else if (idleStateEvent.state() == IdleState.WRITER_IDLE){
                System.err.println("已经10秒没有写信息！"+LocalDateTime.now());
            }
        }

        super.userEventTriggered(ctx, evt);
    }
}
