package com.gl.ceir.supportmodule.client;

import com.gl.ceir.supportmodule.Constants.ClientTypeEnum;
import com.gl.ceir.supportmodule.dto.ClientInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@Component
public class ClientNameFilter extends OncePerRequestFilter {
    @Autowired
    private ClientInfo clientInfo;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        // Check if the request URI contains "swagger" or "v3/api-docs"
        if (requestURI.contains("swagger") || requestURI.contains("v3/api-docs")) {
            // If it's a Swagger request or API documentation request, bypass the filter
            filterChain.doFilter(request, response);
        } else {
            try {
                ClientTypeEnum clientType = ClientTypeEnum.valueOf(request.getHeader("X-Client-Type"));
                String clientId = request.getHeader("X-Client-Id");

                if (Objects.nonNull(clientId)) {
                    ClientInfo.setClientInfo(clientType, clientId);

                    filterChain.doFilter(request, response);

                    ClientInfo.clearClientInfo();
                } else {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                }
            } catch (IllegalArgumentException e) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
            }
        }
    }
}



