package com.ll.gramgram.boundedContext.member.controller;
//boundedContext 디렉토리 사용 이유: 개별? 개발? 모듈(member, likeableperson, insta)
// standard 디렉토리: 공통 모듈(다른 프로젝트에서도 사용 가능)

import com.ll.gramgram.boundedContext.member.entity.Member;
import com.ll.gramgram.boundedContext.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest // 스프링부트 관련 컴포넌트 테스트할 때 붙여야 함, Ioc 컨테이너 작동시킴
@AutoConfigureMockMvc // http 요청, 응답 테스트
@Transactional // 실제로 테스트에서 발생한 DB 작업이 영구적으로 적용되지 않도록, test + 트랜잭션 => 자동롤백
@ActiveProfiles("test") // application-test.yml 을 활성화 시킨다.
public class MemberControllerTests {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("회원가입 폼")
    void t001() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(get("/member/join"))
                .andDo(print()); // 확인용
        // THEN
        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("showJoin"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString("""
                        <input type="text" name="username"
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        <input type="password" name="password"
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        <input type="submit" value="회원가입"
                        """.stripIndent().trim())));
    }

    @Test
    @DisplayName("회원가입")
    void t002() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(post("/member/join")
                        .with(csrf()) // CSRF 키 생성
                        .param("username", "user10")
                        .param("password", "1234")
                )
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().is3xxRedirection());

        Member member = memberService.findByUsername("user10").orElse(null);

        assertThat(member).isNotNull();
    }

    @Test
    @DisplayName("회원가입시에 올바른 데이터를 넘기지 않으면 400")
    void t003() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(post("/member/join")
                        .with(csrf()) // CSRF 키 생성
                        .param("username", "user10")
                )
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().is4xxClientError());

        // WHEN
        resultActions = mvc
                .perform(post("/member/join")
                        .with(csrf()) // CSRF 키 생성
                        .param("password", "1234")
                )
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().is4xxClientError());

        // WHEN
        resultActions = mvc
                .perform(post("/member/join")
                        .with(csrf()) // CSRF 키 생성
                        .param("username", "user10" + "a".repeat(30))
                        .param("password", "1234")
                )
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().is4xxClientError());

        // WHEN
        resultActions = mvc
                .perform(post("/member/join")
                        .with(csrf()) // CSRF 키 생성
                        .param("username", "user10")
                        .param("password", "1234" + "a".repeat(30))
                )
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("로그인 폼")
    void t004() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(get("/member/login"))
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("showLogin"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString("""
                        <input type="text" name="username"
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        <input type="password" name="password"
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        <input type="submit" value="로그인"
                        """.stripIndent().trim())));
    }

    @Test
    // @Rollback(value = false) // DB에 흔적이 남는다.
    @DisplayName("로그인 처리")
    void t005() throws Exception {
        // 로그인이 잘 처리되었는지 확인하는 코드
        // WHEN
        ResultActions resultActions = mvc
                .perform(post("/member/login")
                        .with(csrf()) // CSRF 키 생성
                        .param("username", "user1")
                        .param("password", "1234")
                )
                .andDo(print());

        //세션에 접근해서 값을 가져온 뒤 확인까지
        MvcResult mvcResult = resultActions.andReturn();
        HttpSession httpSession = mvcResult.getRequest().getSession(false);
        SecurityContext securityContext = (SecurityContext)httpSession.getAttribute("SPRING_SECURITY_CONTEXT");
        User user = (User)securityContext.getAuthentication().getPrincipal();

        assertThat(user.getUsername()).isEqualTo("user1");

        // THEN
        resultActions
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/**"));
    }
}
