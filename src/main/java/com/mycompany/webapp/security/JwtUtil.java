package com.mycompany.webapp.security;

import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtUtil {
	//비밀키(노출이 되면 안됨)
	private static final String secretKey = "12345";
	
	//JWT 생성
	public static String createToken(String mid, String authority) {
		log.info("실행");
		String result = null;
		try {
			String token = Jwts.builder()
						//헤더 설정
						.setHeaderParam("alg", "HS256") //alg는 알고리즘
						.setHeaderParam("typ", "JWT")
						
						//토근의 유효기간
						.setExpiration(new Date(new Date().getTime() + 1000*60*60*24)) // 1은 1000분의 1초이기 때문에 1년.
						
						//페이로드 설정
						.claim("mid", mid)
						.claim("authority", authority)
						//서명 설정
						.signWith(SignatureAlgorithm.HS256, secretKey.getBytes("UTF-8"))
						//토큰 생성
						.compact();
			result = token;
		} catch(Exception e) {
		}
		return result;
	}
	
	//JWT 유효성 검사
	//parseClaimsJws : "서명이된" 토큰을 해석한다는 뜻
	public static Claims validateToken(String token) {
		log.info("실행");
		Claims result = null;
		try {
				Claims claims = Jwts.parser()
								.setSigningKey(secretKey.getBytes("UTF-8"))
								.parseClaimsJws(token)
								.getBody();
				result = claims;
		}	catch(Exception e) {
		}
		return result;
	}
	
	//JWT에서 정보 얻기
	public static String getMid(Claims claims) {
		log.info("실행");
		return claims.get("mid", String.class);
	}
	
	public static String getAuthority(Claims claims) {
		log.info("실행");
		return claims.get("authority", String.class);
	}
	
	/*
	//확인한 후 주석처리하기. 왜냐하면 main이 2번 실행됨. SpringbootApplictaion.java
	public static void main(String[] args) throws Exception {
		//토근 생성
		String mid = "user";
		String mrole = "ROLE_USER";
		String jwt = createToken(mid, mrole);
		log.info(jwt);
		
		//토큰 유효성 검사
		Claims claims = validateToken(jwt);
		if(claims != null) {
			log.info("유효한 토큰");
			log.info("mid: " + getMid(claims));
			log.info("authority:" + getAuthority(claims));
		} else {
			log.info("유효하지 않은 토큰");
		}
	}
	*/
}
