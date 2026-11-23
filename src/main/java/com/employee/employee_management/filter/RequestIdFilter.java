package com.employee.employee_management.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // Get UUID from client
        String userUUID = request.getHeader("X-UUID");

        // Validate UUID
        try {
            UUID parsedUUID = UUID.fromString(userUUID);

            if (!parsedUUID.toString().equalsIgnoreCase(userUUID)) {
                throw new IllegalArgumentException("Invalid UUID format");
            }

        } catch (Exception ex) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");

            response.getWriter().write(
                    """
                            {
                                "status": 400,
                                "message": "Invalid X-UUID. Please provide a valid UUID."
                            }
                            """
            );
            return;
        }

        // Generate API Request ID
        String requestId = UUID.randomUUID().toString();

        // Add response headers
        response.setHeader("X-UUID", userUUID);
        response.setHeader("X-Request-Id", requestId);

        // Store IDs in request attributes
        request.setAttribute("userUUID", userUUID);
        request.setAttribute("requestId", requestId);

        // Add API Request ID to MDC
        MDC.put("requestId", requestId);
        MDC.put("userUUID", userUUID);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("requestId");
            MDC.remove("userUUID");
        }
    }
}