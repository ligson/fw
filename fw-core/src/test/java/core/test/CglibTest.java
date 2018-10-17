package core.test;

import core.test.demo.service.CalcService;
import core.test.demo.service.PrintService;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

class MyMethodInterceptor implements MethodInterceptor {
    public String name;

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        System.out.println("start");
        /*if (name.equals(method.getName())) {
            return proxy.invokeSuper(obj, args);
        } else {
            return null;
        }*/
        return proxy.invokeSuper(obj, args);
    }
}

public class CglibTest {
    public static void main(String[] args) throws Exception {
        String name = "add";
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(CalcService.class);
        MyMethodInterceptor my = new MyMethodInterceptor();
        my.name = name;
        enhancer.setCallback(my);
        enhancer.setInterfaces(CalcService.class.getInterfaces());
        CalcService calcService = (CalcService) enhancer.create();
        Field field = CalcService.class.getDeclaredField("printService");
        field.setAccessible(true);


        Enhancer enhancer2 = new Enhancer();
        enhancer2.setSuperclass(PrintService.class);
        MyMethodInterceptor my2 = new MyMethodInterceptor();
        my2.name = name;
        enhancer2.setCallback(my2);
        enhancer2.setInterfaces(PrintService.class.getInterfaces());
        PrintService p = (PrintService) enhancer2.create();
        p.print("-0-0-");
        System.out.println(p);
        field.set(calcService, p);

        int result = calcService.add(10, 11);
        System.out.println(result);

    }
}
