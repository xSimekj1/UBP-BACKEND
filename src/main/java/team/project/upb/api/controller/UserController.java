package team.project.upb.api.controller;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import team.project.upb.api.security.CurrentUser;
import team.project.upb.api.security.UserPrincipal;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/users")
public class UserController {

    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER')")
    public String getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        String userSummary =  currentUser.getUsername();
        return userSummary;
    }

    @GetMapping("/hello")
    public String getmessage() {
        return "Hello User";
    }

}