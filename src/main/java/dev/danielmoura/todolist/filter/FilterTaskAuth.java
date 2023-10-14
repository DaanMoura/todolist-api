package dev.danielmoura.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.BCrypt.Result;
import dev.danielmoura.todolist.user.IUserRepository;
import dev.danielmoura.todolist.user.UserModel;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository userRepository; 

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String serveletPath = request.getServletPath();

        if (!serveletPath.startsWith("/tasks/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Basic")) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
            return;
        }

        String authEncoded = authorization.substring("Basic".length()).trim();
        byte[] authDecoded = Base64.getDecoder().decode(authEncoded);
        String authString = new String(authDecoded);

        String[] credentials = authString.split(":");
        String username = credentials[0];
        String password = credentials[1];

        UserModel user = this.userRepository.findByUsername(username);
        if (user == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "User not found");
            return;
        }

        Result passwordVerification = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());

        if (!passwordVerification.verified) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Wrong password");
            return;
        }
        
        request.setAttribute("idUser", user.getId());
        filterChain.doFilter(request, response);
    }

}
