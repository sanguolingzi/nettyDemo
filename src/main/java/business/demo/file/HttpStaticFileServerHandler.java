package business.demo.file;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.MimetypesFileTypeMap;

public class HttpStaticFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest>
{
    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final int HTTP_CACHE_SECONDS = 60;
    private final boolean useSendFile;
    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

    private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

    public HttpStaticFileServerHandler(boolean useSendFile)
    {
        this.useSendFile = useSendFile;
    }

    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request)
            throws Exception
    {
           /*
        if (!request.getDecoderResult().isSuccess()) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        if (request.getMethod() != HttpMethod.GET) {
            sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }

        String uri = request.getUri();
        String path = sanitizeUri(uri);
        if (path == null) {
            sendError(ctx, HttpResponseStatus.FORBIDDEN);
            return;
        }

        File file = new File(path);
        if ((file.isHidden()) || (!file.exists())) {
            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }

        if (file.isDirectory()) {
            if (uri.endsWith("/"))
                sendListing(ctx, file);
            else {
                sendRedirect(ctx, new StringBuilder().append(uri).append('/').toString());
            }
            return;
        }

        if (!file.isFile()) {
            sendError(ctx, HttpResponseStatus.FORBIDDEN);
            return;
        }


        String ifModifiedSince = request.headers().get("If-Modified-Since");
        if ((ifModifiedSince != null) && (!ifModifiedSince.isEmpty())) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
            Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);

            long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000L;
            long fileLastModifiedSeconds = file.lastModified() / 1000L;
            if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                sendNotModified(ctx);
                return;
            }
        }
        */
        RandomAccessFile raf;
        String path = "C:\\Users\\Administrator\\Desktop\\testFile\\746bf39b-f517-4a8d-b773-44cb31b1c100.txt";
        File file = new File(path);
        try {
            raf = new RandomAccessFile(file, "rw");
        } catch (FileNotFoundException fnfe) {
            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }
        long fileLength = raf.length();

        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        HttpHeaders.setContentLength(response, fileLength);
        setContentTypeHeader(response, file);
        //setDateAndCacheHeaders(response, file);
        if (HttpHeaders.isKeepAlive(request)) {
            response.headers().set("Connection", "keep-alive");
        }

        ctx.write(response);
        ChannelFuture sendFileFuture;
        if (this.useSendFile) {
            sendFileFuture = ctx.write(new DefaultFileRegion(raf.getChannel(), 0L, fileLength), ctx.newProgressivePromise());
        }
        else {
            sendFileFuture = ctx.write(new ChunkedFile(raf, 0L, fileLength, 8192), ctx.newProgressivePromise());
        }

        sendFileFuture.addListener(new ChannelProgressiveFutureListener()
        {
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                if (total < 0L)
                    System.err.println("Transfer progress: " + progress);
                else
                    System.err.println("Transfer progress: " + progress + " / " + total);
            }

            public void operationComplete(ChannelProgressiveFuture future)
                    throws Exception
            {
                System.err.println("Transfer complete.");
            }
        });
        ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

        if (!HttpHeaders.isKeepAlive(request))
        {
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        cause.printStackTrace();
        if (ctx.channel().isActive())
            sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    private static String sanitizeUri(String uri)
    {
        try
        {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            try {
                uri = URLDecoder.decode(uri, "ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                throw new Error();
            }
        }

        if (!uri.startsWith("/")) {
            return null;
        }

        uri = uri.replace('/', File.separatorChar);

        if ((uri.contains(new StringBuilder().append(File.separator).append('.').toString())) || (uri.contains(new StringBuilder().append('.').append(File.separator).toString())) || (uri.startsWith(".")) || (uri.endsWith(".")) || (INSECURE_URI.matcher(uri).matches()))
        {
            return null;
        }

        return new StringBuilder().append(System.getProperty("user.dir")).append(File.separator).append(uri).toString();
    }

    private static void sendListing(ChannelHandlerContext ctx, File dir)
    {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set("Content-Type", "text/html; charset=UTF-8");

        StringBuilder buf = new StringBuilder();
        String dirPath = dir.getPath();

        buf.append("<!DOCTYPE html>\r\n");
        buf.append("<html><head><title>");
        buf.append("Listing of: ");
        buf.append(dirPath);
        buf.append("</title></head><body>\r\n");

        buf.append("<h3>Listing of: ");
        buf.append(dirPath);
        buf.append("</h3>\r\n");

        buf.append("<ul>");
        buf.append("<li><a href=\"../\">..</a></li>\r\n");

        for (File f : dir.listFiles())
            if ((!f.isHidden()) && (f.canRead()))
            {
                String name = f.getName();
                if (ALLOWED_FILE_NAME.matcher(name).matches())
                {
                    buf.append("<li><a href=\"");
                    buf.append(name);
                    buf.append("\">");
                    buf.append(name);
                    buf.append("</a></li>\r\n");
                }
            }
        buf.append("</ul></body></html>\r\n");
        ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
        response.content().writeBytes(buffer);
        buffer.release();

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
        response.headers().set("Location", newUri);

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(new StringBuilder().append("Failure: ").append(status.toString()).append("\r\n").toString(), CharsetUtil.UTF_8));

        response.headers().set("Content-Type", "text/plain; charset=UTF-8");

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendNotModified(ChannelHandlerContext ctx)
    {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_MODIFIED);
        setDateHeader(response);

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void setDateHeader(FullHttpResponse response)
    {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));

        Calendar time = new GregorianCalendar();
        response.headers().set("Date", dateFormatter.format(time.getTime()));
    }

    private static void setDateAndCacheHeaders(HttpResponse response, File fileToCache)
    {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));

        Calendar time = new GregorianCalendar();
        response.headers().set("Date", dateFormatter.format(time.getTime()));

        time.add(13, 60);
        response.headers().set("Expires", dateFormatter.format(time.getTime()));
        response.headers().set("Cache-Control", "private, max-age=60");
        response.headers().set("Last-Modified", dateFormatter.format(new Date(fileToCache.lastModified())));
        response.headers().set(HttpHeaderNames.CONTENT_DISPOSITION, "attachment; filename=abc.txt");
    }

    private static void setContentTypeHeader(HttpResponse response, File file)
    {
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        //response.headers().set("Content-Type", mimeTypesMap.getContentType(file.getPath()));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,"application/octet-stream");
    }
}
