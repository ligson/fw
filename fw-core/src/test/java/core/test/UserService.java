package core.test;

import org.ligson.fw.core.annotation.Autowire;
import org.ligson.fw.core.annotation.Service;

@Service
public class UserService {
    @Autowire
    private CalcService calcService;

    public int add(int a, int b) {
        return calcService.add(a, b);
    }
}
