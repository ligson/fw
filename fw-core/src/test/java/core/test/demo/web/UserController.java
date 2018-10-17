package core.test.demo.web;

import core.test.demo.service.UserService;
import org.ligson.fw.core.annotation.Autowire;
import org.ligson.fw.core.web.annotation.Controller;
import org.ligson.fw.core.web.annotation.RequestMapping;
import org.ligson.fw.core.web.enums.HttpMethod;

@Controller
@RequestMapping(value = "/user")
public class UserController {
    @Autowire
    private UserService userService;

    @RequestMapping(value = "/register", method = HttpMethod.POST)
    public boolean register(String name, String password) {
        return userService.register(name, password);
    }
}
