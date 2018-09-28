package core.test.service;

import org.ligson.fw.core.annotation.Autowire;
import org.ligson.fw.core.annotation.Service;

@Service
public class CalcService {
    @Autowire
    private PrintService printService;

    public int add(int a, int b) {
        printService.print("计算结果:" + a + "+" + b + "=" + (a + b));
        return a + b;
    }
}
