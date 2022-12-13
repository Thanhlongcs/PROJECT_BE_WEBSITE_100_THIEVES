package soixam.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import soixam.dto.reponse.JwtResponse;
import soixam.dto.reponse.ResponseMessage;
import soixam.dto.request.SignInForm;
import soixam.dto.request.SignUpForm;
import soixam.model.Role;
import soixam.model.RoleName;
import soixam.model.User;
import soixam.security.jwt.JwtProvider;
import soixam.security.jwt.JwtTokenFilter;
import soixam.security.userpincal.UserPrinciple;
import soixam.service.impl.RoleServiceIMPL;
import soixam.service.impl.UserServiceIMPL;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Set;

@RestController
@CrossOrigin( origins = "*" )
@RequestMapping( "/auth" )
public class AuthController {
    @Autowired
    UserServiceIMPL userServiceIMPL;
    @Autowired
    RoleServiceIMPL roleServiceIMPL;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtProvider jwtProvider;
    @Autowired
    JwtTokenFilter jwtTokenFilter;

    @PostMapping( "/signup" )
    public ResponseEntity<?> register(@Valid @RequestBody SignUpForm signUpForm) {
        if (userServiceIMPL.existsByUsername(signUpForm.getUsername())) {
            return new ResponseEntity<>(new ResponseMessage("The username is existed"), HttpStatus.OK);
        }
        if (userServiceIMPL.existsByEmail(signUpForm.getEmail())){
            return new ResponseEntity<>(new ResponseMessage("The email is existed"),HttpStatus.OK);
        }
        User user = new User(signUpForm.getName(), signUpForm.getUsername(), signUpForm.getEmail(), passwordEncoder.encode(signUpForm.getPassword()));
        String avatar = "https://firebasestorage.googleapis.com/v0/b/chinhbeo-18d3b.appspot.com/o/avatar.jpg?alt=media&token=56f66b7d-6196-42da-bb8f-73828108db1e";
        user.setAvatar(avatar);
        Set<String> strRole = signUpForm.getRoles();
        Set<Role> roles = new HashSet<>();
        strRole.forEach(role -> {
            switch (role) {
                case "admin":
                    Role adminRole = roleServiceIMPL.findByName(RoleName.ADMIN).orElseThrow(() -> new RuntimeException("Role not found"));
                    roles.add(adminRole);
                    break;
                case "pm":
                    Role pmRole = roleServiceIMPL.findByName(RoleName.PM).orElseThrow(() -> new RuntimeException("Role not found"));
                    roles.add(pmRole);
                    break;
                default:
                    Role userRole = roleServiceIMPL.findByName(RoleName.USER).orElseThrow(() -> new RuntimeException("Role not found"));
                    roles.add(userRole);

            }
        });
        user.setRoles(roles);
        userServiceIMPL.save(user);
        return new ResponseEntity<>(new ResponseMessage("Create_success!!!"), HttpStatus.OK);
    }
    @PostMapping("/signin")
    public ResponseEntity<?>login(@Valid @RequestBody SignInForm signInForm){
        System.out.println("check sigin >>>>" + signInForm.getPassword());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signInForm.getUsername(), signInForm.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtProvider.createToken(authentication);
        UserPrinciple userPrinciple = (UserPrinciple) authentication.getPrincipal();
        return ResponseEntity.ok(new JwtResponse(token, userPrinciple.getName(),userPrinciple.getAvatar(), userPrinciple.getAuthorities()));
    }
}