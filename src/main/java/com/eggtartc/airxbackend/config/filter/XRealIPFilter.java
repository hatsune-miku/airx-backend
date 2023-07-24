package com.eggtartc.airxbackend.config.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.logging.Logger;

@Component
public class XRealIPFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String xRealIp = httpServletRequest.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(httpServletRequest) {
                @Override
                public String getRemoteAddr() {
                    return xRealIp;
                }
            };
            chain.doFilter(wrappedRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}
