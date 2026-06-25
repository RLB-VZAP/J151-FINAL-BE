package com.vzap.trytons.filter;

import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.model.User;
import com.vzap.trytons.util.RoleUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter(urlPatterns = "/api/admin/*")
public class RoleFilter {

    private static final String CURRENT_USER_ATTR = "currentUser";

    public void doFilter (ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException{

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        User currentUser = (User) httpRequest.getAttribute(CURRENT_USER_ATTR);

        if (!RoleUtil.isAdmin(currentUser)) {
            AuthorisationException error = new AuthorisationException ("Admin access required");
            sendErrorResponse(httpResponse, error); //Method from Jarryd's section
            return;                                 //might have to rename
        }
        chain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, AuthorisationException error)
            throws IOException {
        response.setStatus(error.getStatusCode());

        //This is broken for now, until Jarryd uploads his part - calm down :)
        ErrorResponse body = ErrorResponse.builder()
                .success(false)
                .message(error.getMessage())
                .status(error.getStatusCode())
                .build();

        response.setContentType("application/json");
        String json = JsonUtil.toJson(body); //Same here, but this might need to change
        response.getWriter().write(json);
    }
}
