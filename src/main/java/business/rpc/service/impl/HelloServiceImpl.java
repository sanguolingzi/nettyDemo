package business.rpc.service.impl;

import business.rpc.service.IHelloService;

public class HelloServiceImpl implements IHelloService
{

    @Override
    public void sayHello(String name) {
        System.out.println("hello "+name);
    }

    @Override
    public void sayHelloDeail(String name) {
        System.out.println("hello sayHelloDeail "+name);
    }
}
