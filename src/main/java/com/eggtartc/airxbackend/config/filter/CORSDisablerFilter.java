package com.eggtartc.airxbackend.config.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CORSDisablerFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (response instanceof HttpServletResponse servletResponse) {
            servletResponse.setHeader("Access-Control-Allow-Origin", "*");
            servletResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            servletResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
            servletResponse.setHeader("Access-Control-Max-Age", "3600");
            servletResponse.setHeader("Access-Control-Allow-Credentials", "true");

            if (request instanceof HttpServletRequest servletRequest
                && servletRequest.getMethod().equalsIgnoreCase("OPTIONS")) {
                servletResponse.setStatus(HttpServletResponse.SC_OK);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
