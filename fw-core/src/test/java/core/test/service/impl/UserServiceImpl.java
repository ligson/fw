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
        return calcService.add(a, b);
    }
}
