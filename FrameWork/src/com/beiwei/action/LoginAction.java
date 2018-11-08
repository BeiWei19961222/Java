package com.beiwei.action;

import com.beiwei.annotation.MyController;
import com.beiwei.annotation.MyRequestMapping;

@MyController
@MyRequestMapping("/user")
public class LoginAction {

    @MyRequestMapping("/login")
    public String login() {
        System.out.println("��¼����!");
        return "/login.jsp";
    }
}
