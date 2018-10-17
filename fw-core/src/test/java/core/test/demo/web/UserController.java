package core.test.demo.web;

import core.test.demo.service.UserService;
import core.test.demo.vo.UserVo;
import org.ligson.fw.core.annotation.Autowire;
import org.ligson.fw.core.web.annotation.Controller;
import org.ligson.fw.core.web.annotation.RequestBody;
import org.ligson.fw.core.web.annotation.RequestMapping;
import org.ligson.fw.core.web.enums.HttpMethod;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = "/user")
public class UserController {
    @Autowire
    private UserService userService;

    @RequestMapping(value = "/register", method = HttpMethod.POST)
    public boolean register(String name, String password) {
        return userService.register(name, password);
    }

    @RequestMapping(value = "/list", method = HttpMethod.GET)
    public List<UserVo> list() {
        List<UserVo> userVos = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            UserVo userVo = new UserVo();
            userVo.setId(i + "");
            userVo.setName("test-" + i);
            userVo.setBirth(new Date());
            userVos.add(userVo);
        }
        return userVos;
    }

    @RequestMapping(value = "/save", method = HttpMethod.POST)
    public UserVo save(@RequestBody UserVo userVo) {
        System.out.println(userVo);
        return userVo;
    }

}
