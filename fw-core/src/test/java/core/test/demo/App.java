package core.test.demo;

import core.test.demo.service.UserService;
import org.ligson.fw.core.Application;
import org.ligson.fw.core.web.annotation.EnableWeb;
import org.ligson.fw.core.annotation.FWApp;

@FWApp(basePackages = {"core.test"}, enableCglibProxy = true)
@EnableWeb(port = 8888)
public class App {

    public static void main(String[] args) throws Exception {
        Application application = new Application();
        application.run(args);
        UserService userService = application.getBeanByClass(UserService.class);
        System.out.println(userService.add(10, 19));
    }
}
