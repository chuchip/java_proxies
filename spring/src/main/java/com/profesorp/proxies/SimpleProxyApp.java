package com.profesorp.proxies;
import com.profesorp.dumb.DumbImpl;
import com.profesorp.dumb.DumbInterface;
import lombok.extern.slf4j.Slf4j;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Slf4j
public class SimpleProxyApp {
    public static void main(String[] args) {
        DumbInterface dumbClass= (DumbInterface) Proxy.newProxyInstance(
                DumbInterface.class.getClassLoader(),
                new Class[] {DumbInterface.class},
                new DumbProxy(new DumbImpl()));
        dumbClass.sayHello();
        log.info("Adding number {} to number {}. Result {}",1,2,  dumbClass.sum2Numbers(1,2));
        log.info("Adding number {} to number {}. Result {}",0,2,  dumbClass.sum2Numbers(0,2));
      }

}
@Slf4j
class DumbProxy implements InvocationHandler
{
    Object target;
    public DumbProxy(Object target)
    {
        this.target=target;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object obj=null;
        switch (method.getName()) {
            case "sayHello":
                log.info("'sayHello' function was invoked");
                method.invoke(target);
                break;
            case "sum2Numbers":
                log.info("'sum2Numbers' function was invoked with numbers {}, {} ",args[0],args[1] );
                if ((Integer) args[0]==0)
                    args[0]=10; // Change the number 0 to 10.
                obj=method.invoke(target,args);
                break;
            default:
                log.info("I don't know what function is this: "+method.getName());
        }
        return obj;
    }
}
