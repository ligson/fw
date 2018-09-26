package core.test;

import org.ligson.fw.core.annotation.Service;

@Service
public class PrintService {
    public void print(Object object) {
        System.out.println(object);
    }
}
