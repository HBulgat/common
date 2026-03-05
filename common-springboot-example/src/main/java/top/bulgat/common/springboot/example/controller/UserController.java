package top.bulgat.common.springboot.example.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import top.bulgat.common.base.model.Result;
import top.bulgat.common.base.util.JsonUtils;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Data
    @AllArgsConstructor
    public static class Login{
        private String username;
        private String password;
        private boolean success;
    }

    @Data
    @AllArgsConstructor
    public static class Logout{
        private String username;
        private boolean success;
    }
    @GetMapping("/login")
    public Result<Login> login(@RequestParam("username") String username,
                                @RequestParam("password") String password){
        log.info("[UserController.login] username={}, password={}",username,password);
        return Result.success(new Login(username,username,true));
    }

    @Data
    public static class LogoutReq{
        private String username;
    }
    @PostMapping("/logout")
    public Result<Logout> logout(@RequestBody LogoutReq req){
        log.info("[UserController.logout] req={}", JsonUtils.toJson(req));
        return Result.success(new Logout(req.username,true));
    }


}
