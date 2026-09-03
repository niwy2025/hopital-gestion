package com.hopital.accounting.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

/** Same concise request diagnostics as the other hospital services. */
public class HttpAccessLogFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger("http.access");
    @Override protected boolean shouldNotFilter(HttpServletRequest request) { return request.getRequestURI().startsWith("/actuator"); }
    @Override protected boolean shouldNotFilterErrorDispatch() { return true; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long startedAt = System.nanoTime();
        try { chain.doFilter(request, response); }
        finally {
            log.info("HTTP {} {} -> {} ({} ms)", request.getMethod(), request.getRequestURI(), response.getStatus(),
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt));
        }
    }
}
