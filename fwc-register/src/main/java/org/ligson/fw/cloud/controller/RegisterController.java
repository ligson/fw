package org.ligson.fw.cloud.controller;

import org.ligson.fw.cloud.common.vo.ServerNode;
import org.ligson.fw.cloud.service.RegisterService;
import org.ligson.fw.core.annotation.Autowire;
import org.ligson.fw.core.web.annotation.Controller;
import org.ligson.fw.core.web.annotation.RequestBody;
import org.ligson.fw.core.web.annotation.RequestMapping;
import org.ligson.fw.core.web.enums.HttpMethod;

@Controller
public class RegisterController {
    @Autowire
    private RegisterService registerService;

    @RequestMapping(value = "/reister", method = HttpMethod.POST)
    public boolean register(@RequestBody ServerNode serverNode) {
        boolean register = registerService.register(serverNode);
        return register;
    }
}
