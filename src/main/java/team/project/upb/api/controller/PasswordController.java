package team.project.upb.api.controller;

import org.passay.RuleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import team.project.upb.api.model.PasswordJSON;
import team.project.upb.api.service.PasswordService;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(value = "/api/auth")
public class PasswordController {

    @Autowired
    private PasswordService passwordService;

    @PostMapping(value = "/pass-strength", consumes = "application/json")
    public boolean checkPasswordStrength(@RequestBody PasswordJSON password) {
        String pass = password.getPassword();
        RuleResult result = passwordService.validatePassword(pass);
        System.out.println(result.getDetails());
        return result.isValid();
    }

}
