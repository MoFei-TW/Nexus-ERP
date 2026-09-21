package com.mofei.erp.modules.auth.controller;

import com.mofei.erp.common.R;
import com.mofei.erp.modules.user.domain.Employee;
import com.mofei.erp.modules.user.domain.EmployeeVO;
import com.mofei.erp.modules.user.mapper.EmployeeMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final EmployeeMapper employeeMapper;

    public record LoginRequest(String username, String password) {}

    public AuthController(AuthenticationManager authenticationManager, EmployeeMapper employeeMapper) {
        this.authenticationManager = authenticationManager;
        this.employeeMapper = employeeMapper;
    }

    @PostMapping("/login")
    public R<EmployeeVO> login(@RequestBody LoginRequest request,
                               HttpServletRequest httpRequest,
                               HttpServletResponse httpResponse) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        // 設置 Cookie 三大本柱: HttpOnly=true, Secure=true, SameSite=Strict
        ResponseCookie cookie = ResponseCookie.from("MOFEI_SESSION", session.getId())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofHours(24))
                .build();
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // 查詢員工詳細資訊
        Employee employee = employeeMapper.selectById(auth.getName());
        return R.ok("登入成功", EmployeeVO.fromEntity(employee));
    }

    @PostMapping("/logout")
    public R<Void> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        ResponseCookie cookie = ResponseCookie.from("MOFEI_SESSION", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return R.ok("登出成功", null);
    }

    @GetMapping("/me")
    public R<EmployeeVO> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return R.fail(401, "未登入");
        }
        Employee employee = employeeMapper.selectById(authentication.getName());
        return R.ok(EmployeeVO.fromEntity(employee));
    }
}
