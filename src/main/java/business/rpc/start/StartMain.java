package business.rpc.start;

import business.rpc.RpcFramework;
import business.rpc.service.IHelloService;
import business.rpc.service.impl.HelloServiceImpl;

public class StartMain {

    public static void main(String[] args) throws Exception{
        IHelloService helloService = new HelloServiceImpl();
        RpcFramework.export(helloService,8099);
    }
}
