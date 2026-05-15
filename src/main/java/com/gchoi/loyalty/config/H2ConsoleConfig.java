package com.gchoi.loyalty.config;

import org.h2.server.web.JakartaWebServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the H2 browser console servlet for local development.
 */
@Configuration
public class H2ConsoleConfig {
    /**
     * Registers the H2 console at {@code /h2-console/*}.
     *
     * @return servlet registration for the H2 console
     */
    @Bean
    public ServletRegistrationBean<JakartaWebServlet> h2ConsoleServlet() {
        ServletRegistrationBean<JakartaWebServlet> registration =
                new ServletRegistrationBean<>(new JakartaWebServlet(), "/h2-console/*");
        registration.setName("H2Console");
        registration.addInitParameter("webAllowOthers", "false");
        registration.addInitParameter("trace", "false");
        return registration;
    }
}
