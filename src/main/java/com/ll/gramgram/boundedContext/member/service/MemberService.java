package com.ll.gramgram.boundedContext.member.service;

import com.ll.gramgram.boundedContext.member.entity.Member;
import com.ll.gramgram.boundedContext.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 아래 메서드들이 전부 readonly 라는 것을 명시, 나중을 위해
//@Transactional(readOnly = true) DB가 하나 이상일 때 유용
// -> RW: 마스터 DB 서버(읽기&쓰기), R: 슬레이드 DB 서버, R: 슬레이드 DB 서버
//회원가입: 1, 아이디로 회원찾기: 1, 2, 3 -> 아이디로 회원찾기는 1을 제외하는 편이 나음
public class MemberService {
    private final PasswordEncoder passwordEncoder;

    private final MemberRepository memberRepository;

    public Optional<Member> findByUsername(String username) {
        return memberRepository.findByUsername(username);
    }

    @Transactional
    public Member join(String username, String password) {
        Member member = Member
                .builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .build();

        return memberRepository.save(member);
    }
}
