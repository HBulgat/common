package top.bulgat.common.springboot.middleware.test.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.bulgat.common.base.model.Result;
import top.bulgat.common.base.util.JsonUtils;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @GetMapping("/login")
    public Result<Void> login(@RequestParam("username") String username,
                              @RequestParam("password") String password){
        log.info("[UserController::login] param:{username:{},password:{}}",username,password);
        Result<Void> res = Result.success();
        log.info("[UserController::login] return value: {}", JsonUtils.toJson(res));
        return res;
    }

}
