package business.rpc.start;

import business.rpc.RpcFramework;
import business.rpc.service.IHelloService;

public class ConsumerStart {

    public static void main(String[] args) throws Exception{

        IHelloService service = RpcFramework.refer(IHelloService.class,"localhost",8099);
        service.sayHelloDeail("罗湘");
    }
}
