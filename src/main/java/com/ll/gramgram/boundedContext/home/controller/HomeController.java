package com.ll.gramgram.boundedContext.home.controller;

import com.ll.gramgram.boundedContext.member.entity.Member;
import com.ll.gramgram.boundedContext.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.Enumeration;

@Controller
@RequiredArgsConstructor
public class HomeController {
    @GetMapping("/")
    public String showMain() {
        return "usr/home/main";
    }

    @GetMapping("/debugSession") // debug -> 개발자의 편의를 위한 확인용
    @ResponseBody
    public String showDebugSession(HttpSession session) { // 로그인을 세션을 통해 유지
        StringBuilder sb = new StringBuilder("Session content:\n");

        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            Object attributeValue = session.getAttribute(attributeName);
            sb.append(String.format("%s: %s\n", attributeName, attributeValue));
        }

        return sb.toString();
    }
}