package com.testforge.product.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.testforge.product.repository.RevokedTokenRepository;
import com.testforge.product.service.JwtService;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {
	private final JwtService jwtService;
	private final RevokedTokenRepository revokedTokens;

	public JwtAuthFilter(JwtService jwtService, RevokedTokenRepository revokedTokens) {
		this.jwtService = jwtService;
		this.revokedTokens = revokedTokens;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			chain.doFilter(request, response);
			return;
		}
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith("Bearer ")) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Bearer token required");
			return;
		}
		try {
			Claims claims = jwtService.parse(header.substring(7));
			if (revokedTokens.existsByJti(claims.getId())) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has been logged out");
				return;
			}
			String role = claims.get("role", String.class);
			SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
					claims.getSubject(), null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
		} catch (Exception ex) {
			SecurityContextHolder.clearContext();
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
			return;
		}
		chain.doFilter(request, response);
	}
	
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
	    String path = request.getServletPath();

	    return path.startsWith("/swagger-ui/")
	            || path.equals("/swagger-ui.html")
	            || path.equals("/v3/api-docs")
	            || path.startsWith("/v3/api-docs/");
	}
}
