package com.mycompany.webapp.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.webapp.dto.Member;
import com.mycompany.webapp.security.JwtUtil;
import com.mycompany.webapp.service.MemberService;
import com.mycompany.webapp.service.MemberService.JoinResult;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/member")
@Slf4j
public class MemberController {
	@Resource
	private MemberService memberService;
	
	@Resource
	private PasswordEncoder passwordEncoder;
	
	@Resource
	private AuthenticationManager authenticationManger;
	
	//queryString 이나 multipart로 받을 때 Member member 가능
	@RequestMapping("/join1")
	public Map<String, String> join1(Member member) {
		log.info("실행");
		member.setMenabled(true);
		member.setMpassword(passwordEncoder.encode(member.getMpassword()));
		//JoinResult는 memberService에 enum타입으로 되어있음.
		JoinResult jr = memberService.join(member);
		Map<String, String> map = new HashMap<>();
		if(jr == JoinResult.SUCCESS) {
			map.put("result", "success");
		} else if(jr == JoinResult.DUPLICATED) {
			map.put("result", "duplicated");
		} else {
			map.put("result", "fail");
		}
		return map;
	}
	
	//@RequestBody Member member하면 쿼리스트링이 아닌 json을 넣는 것.
	@RequestMapping("/join2")
	public Map<String, String> join2(@RequestBody Member member) {
		log.info("실행");
		return join1(member);
	}
	
	@RequestMapping("/login1")
	public Map<String, String> login1(String mid, String mpassword) {
			log.info("실행");
			if(mid == null || mpassword == null) {
				throw new BadCredentialsException("아이디 또는 패스워드가 제공되지 않았음");
		}
		//Spring Security사용자 인증
		UsernamePasswordAuthenticationToken token = 
				new UsernamePasswordAuthenticationToken(mid, mpassword);
		Authentication authentication = authenticationManger.authenticate(token);
		// authenticate 메소드는 id, password 중 하나라도 문제가 있으면 BadCredentailsException 예외 발생
		// id, password가 맞다면 정상적으로 Authentication authentication 생성.
		
		//시큐리티 환경 정보를 갖고있을 수 있는 홀더(컨테이너)를 호출하여 authentication(인증정보)를 저장
		SecurityContext securityContext = SecurityContextHolder.getContext();
		securityContext.setAuthentication(authentication);
		
		//응답 내용
		String authority = authentication.getAuthorities().iterator().next().toString(); // grantedAuthority라는 객체의 문자열을 가져옴. ROLE_USER가 저장됨.
		log.info(authority);
		Map<String, String> map = new HashMap<>();
		map.put("result", "success");
		map.put("mid", mid);
		map.put("jwt", JwtUtil.createToken(mid, authority));
		return map;
	}
	
	@RequestMapping("/login2")
	public Map<String, String> login2(@RequestBody Member member) {
		log.info("실행");
		return login1(member.getMid(), member.getMpassword());
	}
}
