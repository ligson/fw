package core.test.service;

import org.ligson.fw.core.annotation.Autowire;
import org.ligson.fw.core.annotation.Service;

@Service
public class CalcService {
    @Autowire
    private PrintService printService;

    public int add(int a, int b) {
        System.out.println(printService+"=====");
        printService.print("计算结果:" + a + "+" + b + "=" + (a + b));
        System.out.println("..........");
        return a + b;
    }

    @Override
    public String toString() {
        return "CalcService{" +
                "printService=" + printService +
                '}';
    }
}
