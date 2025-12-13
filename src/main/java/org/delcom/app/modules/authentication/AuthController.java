package org.delcom.app.modules.authentication;

import org.delcom.app.utils.ConstUtil;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.List;

@Controller
@RequestMapping("/auth")
public class AuthController {

  private final AccountService accountService;

  public AuthController(AccountService accountService) {
    this.accountService = accountService;
  }

  @GetMapping("/login")
  public String showLogin(Model model, HttpSession session) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    boolean isLoggedIn = auth != null
        && auth.isAuthenticated()
        && !(auth instanceof AnonymousAuthenticationToken);
    if (isLoggedIn) {
      return "redirect:/";
    }

    model.addAttribute("loginForm", new SignInRequest());
    return ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN;
  }

  @PostMapping("/login/post")
  public String postLogin(@Valid @ModelAttribute("loginForm") SignInRequest signInRequest,
      BindingResult bindingResult,
      HttpSession session,
      Model model) {

    if (bindingResult.hasErrors()) {
      return ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN;
    }

    User existingUser = accountService.getUserByEmail(signInRequest.getEmail());
    if (existingUser == null) {
      bindingResult.rejectValue("email", "error.loginForm", "Pengguna ini belum terdaftar");
      return ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN;
    }

    boolean isPasswordMatch = new BCryptPasswordEncoder()
        .matches(signInRequest.getPassword(), existingUser.getPassword());
    if (!isPasswordMatch) {
      bindingResult.rejectValue("email", "error.loginForm", "Email atau kata sandi salah");
      return ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN;
    }

    List<GrantedAuthority> authorities = List.of(
        new SimpleGrantedAuthority("ROLE_USER"));

    Authentication authentication = new UsernamePasswordAuthenticationToken(
        existingUser,
        null,
        authorities);

    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);

    session.setAttribute(
        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
        securityContext);

    return "redirect:/";
  }

  @GetMapping("/register")
  public String showRegister(Model model, HttpSession session) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    boolean isLoggedIn = auth != null
        && auth.isAuthenticated()
        && !(auth instanceof AnonymousAuthenticationToken);
    if (isLoggedIn) {
      return "redirect:/";
    }

    model.addAttribute("registerForm", new RegisterForm());
    return ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER;
  }

  @PostMapping("/register/post")
  public String postRegister(@Valid @ModelAttribute("registerForm") RegisterForm registerForm,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes,
      HttpSession session,
      Model model) {

    if (bindingResult.hasErrors()) {
      return ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER;
    }

    User existingUser = accountService.getUserByEmail(registerForm.getEmail());
    if (existingUser != null) {
      bindingResult.rejectValue("email", "error.registerForm", "Pengguna dengan email ini sudah terdaftar");
      return ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER;
    }

    String hashPassword = new BCryptPasswordEncoder().encode(registerForm.getPassword());

    User createdUser = accountService.createUser(
        registerForm.getName(),
        registerForm.getEmail(),
        hashPassword);

    if (createdUser == null) {
      bindingResult.rejectValue("email", "error.registerForm", "Gagal membuat pengguna baru");
      return ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER;
    }

    redirectAttributes.addFlashAttribute("success", "Akun berhasil dibuat! Silakan login.");

    return "redirect:/auth/login";
  }

  @GetMapping("/logout")
  public String logout(HttpSession session) {
    session.invalidate();
    return "redirect:/auth/login";
  }
}
