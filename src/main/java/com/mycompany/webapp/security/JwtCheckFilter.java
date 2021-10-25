package com.mycompany.webapp.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtCheckFilter extends OncePerRequestFilter {
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		log.info("실행");
		//JWT 얻기
		String jwt = null;
		if(request.getHeader("Authorization") != null && 
			request.getHeader("Authorization").startsWith("Bearer")) {
			jwt = request.getHeader("Authorization").substring(7);
		} else if(request.getParameter("jwt") != null) {
			//<img src="url?jwt=xxx"/>
			jwt = request.getParameter("jwt");
		}
		log.info("jwt " + jwt);
		//JWT 유효성 검사
		if(jwt != null) {
			Claims claims = JwtUtil.validateToken(jwt);
			if(claims != null) { // 유효한 토큰이면 옳은 아이디 패스워드가 입력된 것.
				log.info("유효한 토큰");
				// JWT에서 Payload 얻기
				String mid = JwtUtil.getMid(claims);
				String authority = JwtUtil.getAuthority(claims);
				log.info("mid: " + mid);
				log.info("authority: " + authority);
				// Security 인증 확인, Security 인증 처리
				// jwt인증이 되었기 때문에 password는 필요없음
				UsernamePasswordAuthenticationToken token = 
						new UsernamePasswordAuthenticationToken(mid, null, AuthorityUtils.createAuthorityList(authority));
				
				//시큐리티 환경 정보를 갖고있을 수 있는 홀더(컨테이너)를 호출하여 authentication(인증정보)를 저장
				SecurityContext securityContext = SecurityContextHolder.getContext();
				securityContext.setAuthentication(token);
			} else {
				log.info("유효하지 않은 토큰");
			}
		}
		// 다음 필터를 실행
		filterChain.doFilter(request, response);
	}
}
