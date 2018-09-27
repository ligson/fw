package core.test.aop;

import core.test.service.impl.UserServiceImpl;
import org.ligson.fw.core.annotation.Component;
import org.ligson.fw.core.aop.AopFilter;

import java.lang.reflect.Method;

@Component
public class MyAopFilter extends AopFilter {
    @Override
    public String pattern() {
        return ".*UserServiceImpl.*";
    }

    @Override
    public void before(Object object, Method method, Object[] args) {
        System.out.println("调用方法:" + method.getName() + " before");
    }

    @Override
    public void after(Object object, Method method, Object[] args) {
        System.out.println("调用方法:" + method.getName() + " after");
    }

    @Override
    public Object around(Object object, Method method, Object[] args) {
        Object result = super.around(object, method, args);
        System.out.println("执行方法:" + method.getName() + " 结果:" + result);
        return result;
    }
}
