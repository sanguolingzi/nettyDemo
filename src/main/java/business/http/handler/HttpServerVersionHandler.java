package business.http.handler;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;

import java.io.File;
import java.io.RandomAccessFile;

public class HttpServerVersionHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {


        String path = "C:\\Users\\Administrator\\Desktop\\testFile\\746bf39b-f517-4a8d-b773-44cb31b1c100.txt";
        File file = new File(path);
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1
                ,HttpResponseStatus.OK);

        //设置响应信息
        //HttpUtil.setContentLength(fullHttpRequest,randomAccessFile.length());
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, randomAccessFile.length());
        //设置响应头
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,"application/octet-stream");
        //设置响应头
        response.headers().set(HttpHeaderNames.CONTENT_DISPOSITION, "attachment; filename=abc.txt");

        //response.headers().set(HttpHeaderNames.TRANSFER_ENCODING,"chunked");

        //如果一直保持连接则设置响应头信息为：HttpHeaders.Values.KEEP_ALIVE
        //if (HttpUtil.isKeepAlive(fullHttpRequest)) {
        //    response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        //}
        //进行写出
        ctx.write(response);
        //构造发送文件线程，将文件写入到Chunked缓冲区中 写出ChunkedFile
        ChannelFuture sendFileFuture = ctx.write(new ChunkedFile(randomAccessFile, 0, randomAccessFile.length(),8192), ctx.newProgressivePromise());
        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationComplete(ChannelProgressiveFuture future)
                    throws Exception {
                System.out.println("file "+ file.getName()+" transfer complete.");

                System.out.println("future.isDone():"+future.isDone());
                System.out.println("future.isCancelled():"+future.isCancelled());
                System.out.println("future.isSuccess():"+future.isSuccess());
                System.out.println("future.isCancellable():"+future.isCancellable());
                System.out.println("future.isVoid():"+future.isVoid());
                //randomAccessFile.close();
            }
            @Override
            public void operationProgressed(ChannelProgressiveFuture future,
                                            long progress, long total) throws Exception {
                System.out.println("--------"+progress+"--total--:"+total);

                System.out.println("future.isDone():"+future.isDone());
                System.out.println("future.isCancelled():"+future.isCancelled());
                System.out.println("future.isSuccess():"+future.isSuccess());
                System.out.println("future.isCancellable():"+future.isCancellable());
                System.out.println("future.isVoid():"+future.isVoid());

            }
        });
        //如果使用Chunked编码，最后则需要发送一个编码结束的看空消息体，进行标记，表示所有消息体已经成功发送完成。
        ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        //如果当前连接请求非Keep-Alive ，最后一包消息发送完成后 服务器主动关闭连接
        //if (!HttpUtil.isKeepAlive(fullHttpRequest)){
        //    lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        //}



    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ctx.close();
        cause.printStackTrace();
        //关闭连接
    }
}
