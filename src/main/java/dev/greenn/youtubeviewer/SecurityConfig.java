package dev.greenn.youtubeviewer;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import java.io.IOException;

@Configuration
public class SecurityConfig {


    public SecurityConfig() {

    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(registry ->{
            registry.requestMatchers("/", "/subsciptions").permitAll();
            registry.anyRequest().authenticated();
        })
                .oauth2Login(oauth2Login -> new AuthenticationSuccessHandler() {
                    @Override
                    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                        response.sendRedirect("/profile");
                    }
                })
               // .formLogin(Customizer.withDefaults())
                .build();
    }


}