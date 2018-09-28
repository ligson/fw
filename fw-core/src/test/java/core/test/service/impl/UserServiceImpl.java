package core.test.service.impl;

import core.test.service.CalcService;
import core.test.service.UserService;
import org.ligson.fw.core.annotation.Autowire;
import org.ligson.fw.core.annotation.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowire
    private CalcService calcService;

    public int add(int a, int b) {
        System.out.println("调用add---------------"+calcService.getClass().getName());
        int add = calcService.add(a, b);
//        int add = a + b;
        System.out.println("calcService:" + add);
        return add;
    }
}
