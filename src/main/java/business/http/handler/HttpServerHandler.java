package business.http.handler;

import business.http.ParamParse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MixedFileUpload;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.*;


public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {



    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception {

        System.out.println("-------method-------"+fullHttpRequest.method().toString());
        System.out.println("-------uri-------"+fullHttpRequest.uri());

        try{

            /*
            Iterator<Map.Entry<String,String>> header = fullHttpRequest.headers().iteratorAsString();
            while(header.hasNext()){
                Map.Entry temp = header.next();
                System.out.println("key:"+temp.getKey()+",....value:"+temp.getValue());
            }
            */

            if(fullHttpRequest.method().toString().equals(HttpMethod.GET.toString())){
                doGet(ctx,fullHttpRequest);
            }else if(fullHttpRequest.method().toString().equals(HttpMethod.POST.toString())){
                doPost(ctx,fullHttpRequest);
            }
        }catch (Exception e){
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    Unpooled.wrappedBuffer("不好意思出错了".getBytes("utf-8")));
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.write(response);
            ctx.flush();
        }finally{
            if(ctx != null)
                ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        cause.printStackTrace();
    }


    private void doGet(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception{
        System.out.println("-------doGet start-------");

        String uri  = fullHttpRequest.uri();


        if(uri.indexOf("download")>0){//模拟文件下载
            String path = "C:\\Users\\Administrator\\Desktop\\testFile\\746bf39b-f517-4a8d-b773-44cb31b1c100.txt";
            File file = new File(path);
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            HttpResponse  response = new DefaultHttpResponse(HttpVersion.HTTP_1_1
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
            if (HttpUtil.isKeepAlive(fullHttpRequest)) {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            }
            /*
            ctx.write("OK: " + randomAccessFile.length() + '\n');
            if (ctx.pipeline().get(SslHandler.class) == null) {
                // SSL not enabled - can use zero-copy file transfer.
               // ctx.write(new DefaultFileRegion(randomAccessFile.getChannel(), 0, randomAccessFile.length()));
            } else {
                // SSL enabled - cannot use zero-copy file transfer.
                ctx.write(new ChunkedFile(randomAccessFile));
            }
            ctx.writeAndFlush("\n");
             */
            //进行写出
            ctx.write(response);
            //构造发送文件线程，将文件写入到Chunked缓冲区中 写出ChunkedFile
            ChannelFuture sendFileFuture = ctx.write(new ChunkedFile(randomAccessFile, 0, randomAccessFile.length(),8192), ctx.newProgressivePromise());
            sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
                @Override
                public void operationComplete(ChannelProgressiveFuture future)
                        throws Exception {
                    System.out.println("file "+ file.getName()+" transfer complete.");
                }
                @Override
                public void operationProgressed(ChannelProgressiveFuture future,
                                                long progress, long total) throws Exception {
                    System.out.println("----progress----"+progress+"--total--:"+total);
                }
            });
            //如果使用Chunked编码，最后则需要发送一个编码结束的看空消息体，进行标记，表示所有消息体已经成功发送完成。
            ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            //如果当前连接请求非Keep-Alive ，最后一包消息发送完成后 服务器主动关闭连接
            if (!HttpUtil.isKeepAlive(fullHttpRequest)){
                lastContentFuture.addListener(ChannelFutureListener.CLOSE);
            }
        }else{
            Map<String,Object> paraMap = ParamParse.parseGet(fullHttpRequest);
            System.out.println(paraMap);
            ObjectMapper objectMapper = new ObjectMapper();
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer( objectMapper.writeValueAsString(paraMap).getBytes("utf-8")));
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.write(response);
            ctx.flush();
        }
    }



    private void doPost(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception{
        System.out.println("-------doPost start-------");

        String value = fullHttpRequest.headers().getAsString("content-type");

        System.out.println("-------doPost start-------"+InterfaceHttpData.HttpDataType.FileUpload);
        //文件上传类型
        if(value!=null&&value.startsWith(HttpHeaders.Values.MULTIPART_FORM_DATA)){
            StringJoiner stringJoiner = new StringJoiner(",","","");
            try{
                HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(fullHttpRequest);
                if(fullHttpRequest.content().isReadable()){
                    String json=fullHttpRequest.content().toString(CharsetUtil.UTF_8);
                    System.out.println("json:"+json);
                    //paraMap.putAll(JsonUtils.getMapFromJSON(json));
                }
                decoder.offer(fullHttpRequest);//form

                List<InterfaceHttpData> list =  decoder.getBodyHttpDatas();
                //MixedFileUpload fileUpload =  (MixedFileUpload)decoder.getBodyHttpData("file");

                for(InterfaceHttpData interfaceHttpData:list){

                    if(interfaceHttpData.getHttpDataType().equals(InterfaceHttpData.HttpDataType.FileUpload)){
                        MixedFileUpload fileUpload = (MixedFileUpload)interfaceHttpData;
                        int index = fileUpload.getFilename().lastIndexOf(".");
                        String endFix = fileUpload.getFilename().substring(index);
                        String path = "C:\\Users\\Administrator\\Desktop\\testFile\\" +/* File.separator*/ UUID.randomUUID()+endFix;
                        File file = new File(path);
                        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                        randomAccessFile.write(fileUpload.get());

                        stringJoiner.add(fileUpload.getFilename());
                    }
                }
            }catch(Exception e){
                throw e;
            }
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer( stringJoiner.toString().getBytes("utf-8")));
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain;charset=UTF-8");
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            ctx.write(response);
            ctx.flush();
        }else{
            Map<String,Object> paraMap = ParamParse.parsePost(fullHttpRequest);
        }
    }



    public static void main(String[] args) throws Exception{

    }
}
