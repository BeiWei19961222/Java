package com.beiwei.action;

import com.beiwei.annotation.MyController;
import com.beiwei.annotation.MyRequestMapping;

@MyController
@MyRequestMapping("/game")
public class GameAction {

    @MyRequestMapping("/play")
    public String play(){
        System.out.println("����Ϸ��!");
        return "/game.jsp";
    }
}
