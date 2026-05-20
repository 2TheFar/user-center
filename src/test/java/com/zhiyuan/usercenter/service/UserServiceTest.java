package com.zhiyuan.usercenter.service;

import java.util.Date;

import com.zhiyuan.usercenter.model.domain.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务测试
 *
 * @author zhiyuan
 */
@SpringBootTest
public class UserServiceTest {
    @Resource
    private UserService userService;

    @Test
    public void testAddUser() {
        User user = new User();
        user.setUsername("otaku");
        user.setUserAccount("qq");
        user.setUserPassword("123");
        user.setAvatarUrl("https://i1.hdslb.com/bfs/new_dyn/a7b0410d551a4d67ac66d57c2095a1ff1517060220.png");
        user.setPhone("13812345678");
        user.setEmail("123@qq.com");
        user.setGender(0);
        boolean result = userService.save(user);
        System.out.println(user.getId() + "" + result);//嚯嚯嚯，还有这种拼接方式的哦，夸张哦
        assertTrue(result);//写断言，这里还简化了
    }

    @Test
    void userRegister() {
        String userAccount = "";
        String userPassword = "testPassword1";
        String confirmPassword = "testPassword1";
        // 不为空
        long result = userService.userRegister(userAccount, userPassword, confirmPassword);
        Assertions.assertEquals(-1, result);
        // 测账号长度
        userAccount = "test";
        result = userService.userRegister(userAccount, userPassword, confirmPassword);
        Assertions.assertEquals(-2, result);
        // 测密码长度
        userAccount = "testAccount1";
        userPassword = "test";
        confirmPassword = "test";
        result = userService.userRegister(userAccount, userPassword, confirmPassword);
        Assertions.assertEquals(-3, result);
        // 测特殊字符
        userAccount += "@";
        userPassword = "testPassword1";
        confirmPassword = "testPassword1";
        result = userService.userRegister(userAccount, userPassword, confirmPassword);
        Assertions.assertEquals(-4, result);
        // 测确认密码
        userAccount = "testAccount1";
        confirmPassword = "textPassword1";
        result = userService.userRegister(userAccount, userPassword, confirmPassword);
        Assertions.assertEquals(-5, result);
        // 测重复
        userAccount = "qq1234";
        confirmPassword = "testPassword1";
        result = userService.userRegister(userAccount, userPassword, confirmPassword);
        Assertions.assertEquals(-6, result);
        // 测插入
        userAccount = "testAccount1";
        result = userService.userRegister(userAccount, userPassword, confirmPassword);
        // 断言result>0
        Assertions.assertTrue(result > 0);
    }
}