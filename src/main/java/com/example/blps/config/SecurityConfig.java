package com.example.blps.config;

import com.example.blps.config.filter.JWTFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@ToString
@EqualsAndHashCode
@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {
    private final UserDetailsService userDetailsService;
    private final JWTFilter jwtFilter;
    private final MvcRequestMatcher.Builder mvc;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(AbstractHttpConfigurer::disable).csrf(AbstractHttpConfigurer::disable);

        http.exceptionHandling(eh ->
                eh.authenticationEntryPoint((rq, rs, ex) ->
                        rs.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getLocalizedMessage())
                )
        );

        http.csrf(csrf ->
                csrf.ignoringRequestMatchers("/**")
        );
        http.headers(headersConfigurer ->
                headersConfigurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
        );

        CorsConfiguration corsConfig = new CorsConfiguration();

        corsConfig.setAllowCredentials(true);
        corsConfig.addAllowedOrigin("*");
        corsConfig.addAllowedHeader("*");
        corsConfig.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        CorsFilter filter = new CorsFilter(source);

        http.addFilter(filter);
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        http.authorizeHttpRequests((ar) -> {
            ar.requestMatchers(mvc.pattern("/auth/login")).permitAll();
            ar.requestMatchers(mvc.pattern("/auth/registration")).permitAll();
            // Если будет web socket для отправки ответов, ведь у нас в BMPN нарисовано письмами
            // Открыть все чтобы можно было подключиться, на стадии подписки скидывает через AuthChanellInterceptorAdapter.
            // ar.requestMatchers(mvc.pattern("/socket/**")).permitAll();  TODO ?
            ar.anyRequest().authenticated();
        }).httpBasic(withDefaults());

        return http.build();
    }

// Тоже web socket
//    @Bean
//    public AuthorizationManager<Message<?>> messageAuthorizationManager() {
//        MessageMatcherDelegatingAuthorizationManager.Builder messages = new MessageMatcherDelegatingAuthorizationManager.Builder();
//        messages.simpTypeMatchers(
//                SimpMessageType.CONNECT,
//                SimpMessageType.HEARTBEAT,
//                SimpMessageType.UNSUBSCRIBE,
//                SimpMessageType.DISCONNECT
//        ).permitAll().anyMessage().authenticated();
//        return messages.build();
//    }

    @Bean
    public AuthenticationManager authenticationManager(final AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}