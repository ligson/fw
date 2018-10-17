package core.test.demo.service.impl;

import core.test.demo.service.CalcService;
import core.test.demo.service.UserService;
import org.ligson.fw.core.annotation.Autowire;
import org.ligson.fw.core.annotation.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowire
    private CalcService calcService;

    public int add(int a, int b) {
        int add = calcService.add(a, b);
        return add;
    }

    @Override
    public boolean register(String name, String password) {
        System.out.println(name + password);
        return false;
    }
}
