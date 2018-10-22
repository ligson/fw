package org.ligson.fw.cloud;

import org.ligson.fw.core.annotation.FWApp;
import org.ligson.fw.core.web.annotation.EnableWeb;

@FWApp(enableCglibProxy = true)
@EnableWeb(port = 8888)
public class RegisterServer {
    public static void main(String[] args) {

    }
}
