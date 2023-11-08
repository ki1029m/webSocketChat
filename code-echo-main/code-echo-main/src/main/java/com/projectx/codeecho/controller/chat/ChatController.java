package com.projectx.codeecho.controller.chat;

import com.projectx.codeecho.domain.entity.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.PathVariable; //path를 통한 변수받기

@Controller
public class ChatController {
    @GetMapping(value="/chat/{roomName}")
    public ModelAndView chatView(@PathVariable String roomName) {
        ModelAndView mv = new ModelAndView("ide/chat");
        mv.addObject("roomName", roomName); // 방의 이름을 뷰로 전달
        return mv;
    }

    @MessageMapping("/chat/{roomName}")
    @SendTo("/topic/room/{roomName}")
    public Message sendMessage(Message message) {
        return message;
    }
}