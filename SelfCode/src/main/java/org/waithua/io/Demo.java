package org.waithua.io;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by jch on 17/1/3.
 */
@RestController
public class Demo {

    @RequestMapping("/test")
    public String home() {
        return "Hello World!";
    }

}
