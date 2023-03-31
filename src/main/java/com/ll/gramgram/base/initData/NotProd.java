package com.ll.gramgram.base.initData;

import com.ll.gramgram.boundedContext.member.service.MemberService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev", "test"}) // 개발 환경과 테스트 환경에서만 사용하겠다
public class NotProd {
    @Bean
    CommandLineRunner initData(
            MemberService memberService
    ) {
        return args -> {
            memberService.join("user1", "1234");
            memberService.join("user2", "1234");
            memberService.join("user3", "1234");
            memberService.join("user4", "1234");
        };
    }
}
