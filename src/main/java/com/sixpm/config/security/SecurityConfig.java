package com.sixpm.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtTokenProvider jwtTokenProvider;

	// CORS 설정 상수
	private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
		"http://localhost:3000",
		"https://localhost:3000",
		"http://localhost:5173",
		"https://localhost:5173",
		"http://localhost:8080",     // Swagger UI
		"https://localhost:8080",    // Swagger UI (HTTPS)
		"http://blue-pill.me",
		"https://blue-pill.me",
		"https://www.blue-pill.me",
		"https://api.blue-pill.me"   // API 도메인
	);

	private static final List<String> ALLOWED_METHODS = Arrays.asList(
		"GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
	);

	private static final List<String> EXPOSED_HEADERS = Arrays.asList(
		"Authorization",
		"Set-Cookie",
		"Access-Control-Allow-Origin",
		"Access-Control-Allow-Credentials"
	);

	private static final long CORS_MAX_AGE_SECONDS = 3600L;

	// API 엔드포인트 상수
	private static final String AUTH_API_PATTERN = "/api/v1/auth/**";
	private static final String MEETING_AUTH_PATTERN = "/api/v1/meetings/*/auth/**";
	private static final String ADMIN_API_PATTERN = "/api/admin/**";  // Admin API endpoints

	// Public endpoints (인증 불필요)
	private static final String SWAGGER_UI_PATTERN = "/swagger-ui/**";
	private static final String API_DOCS_PATTERN = "/v3/api-docs/**";
	private static final String ACTUATOR_PATTERN = "/actuator/**";
	private static final String HEALTH_CHECK_PATTERN = "/health";
	private static final String ROOT_PATTERN = "/";
	private static final String FAVICON_PATTERN = "/favicon.ico";
	private static final String CORS_ALL_PATHS = "/**";

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("OPTIONS", "/**").permitAll() // Allow all OPTIONS requests for CORS preflight
				.requestMatchers(FAVICON_PATTERN).permitAll() // Favicon
				.requestMatchers(HEALTH_CHECK_PATTERN).permitAll() // Health check for AWS
				.requestMatchers(ROOT_PATTERN).permitAll() // Root endpoint
				.requestMatchers(ACTUATOR_PATTERN).permitAll() // Actuator endpoints
				.requestMatchers(SWAGGER_UI_PATTERN, API_DOCS_PATTERN).permitAll() // Swagger UI

				// Auth endpoints
				.requestMatchers(AUTH_API_PATTERN).permitAll()
				.requestMatchers(MEETING_AUTH_PATTERN).permitAll() // Anonymous login

				.requestMatchers(ADMIN_API_PATTERN).permitAll()

				.anyRequest().authenticated()
			);

		// JWT 필터 추가
		JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider);
		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(ALLOWED_ORIGINS);  // 정확한 URL 매칭
		configuration.setAllowedMethods(ALLOWED_METHODS);
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(CORS_MAX_AGE_SECONDS);
		configuration.setExposedHeaders(EXPOSED_HEADERS);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration(CORS_ALL_PATHS, configuration);

		return source;
	}
}

