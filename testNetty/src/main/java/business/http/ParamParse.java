package business.http;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParamParse {


    public static Map<String,Object> parseGet(FullHttpRequest fullHttpRequest) throws Exception{
         Map<String,Object> paraMap = new HashMap<>();
        // 是GET请求
        QueryStringDecoder decoder = new QueryStringDecoder(fullHttpRequest.uri());
        decoder.parameters().entrySet().forEach( entry -> {
            // entry.getValue()是一个List, 只取第一个元素
            paraMap.put(entry.getKey(), entry.getValue().get(0));
        });
        return paraMap;
    }



    public static Map<String,Object> parsePost(FullHttpRequest fullHttpRequest) throws Exception{
        Map<String,Object> paraMap = new HashMap<>();
        //HttpContent content = new DefaultHttpContent(Unpooled.wrappedBuffer(request.content()));
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(fullHttpRequest);
        if(fullHttpRequest.content().isReadable()){
            String json=fullHttpRequest.content().toString(CharsetUtil.UTF_8);
            System.out.println("json:"+json);
            //paraMap.putAll(JsonUtils.getMapFromJSON(json));
        }
        decoder.offer(fullHttpRequest);//form
        List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();
        for (InterfaceHttpData parm : parmList) {
            if(parm.getHttpDataType().equals(InterfaceHttpData.HttpDataType.Attribute)){
                Attribute data = (Attribute) parm;
                paraMap.put(data.getName(), data.getValue());
            }else if(parm.getHttpDataType().equals(InterfaceHttpData.HttpDataType.FileUpload)){
                System.out.println("-------------文件对象------------------");
            }
        }
        return paraMap;


    }
}
