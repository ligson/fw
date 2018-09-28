package core.test.aop;

import core.test.service.impl.UserServiceImpl;
import org.ligson.fw.core.annotation.Component;
import org.ligson.fw.core.aop.AopFilter;
import org.ligson.fw.core.aop.MethodInvoke;

import java.lang.reflect.Method;
import java.util.Arrays;

@Component
public class MyAopFilter extends AopFilter {
    @Override
    public String pattern() {
        return ".*UserServiceImpl.*";
    }

    @Override
    public void before(Object object, Method method, Object[] args) {
        System.out.println("调用方法:" + method.getDeclaringClass().getName() + "." + method.getName() + " before");
    }

    @Override
    public void after(Object object, Method method, Object[] args) {
        System.out.println("调用方法:" + method.getDeclaringClass().getName() + "." + method.getName() + " after");
    }

    @Override
    public Object around(Object object, Method method, Object[] args, MethodInvoke methodInvoke) throws Throwable {
        Object result = super.around(object, method, args, methodInvoke);
        System.out.println("执行方法:" + method.getDeclaringClass().getName() + "." + method.getName() + " 参数:" + Arrays.toString(args) + " 结果：" + result);
        return result;
    }
}
