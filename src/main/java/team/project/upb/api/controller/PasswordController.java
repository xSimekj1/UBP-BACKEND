package team.project.upb.api.controller;

import org.passay.RuleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import team.project.upb.api.model.PasswordJSON;
import team.project.upb.api.model.PasswordMetadata;
import team.project.upb.api.service.PasswordService;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(value = "/api/auth")
public class PasswordController {

    @Autowired
    private PasswordService passwordService;

    @PostMapping(value = "/pass-strength", consumes = "application/json")
    public PasswordMetadata checkPasswordStrength(@RequestBody PasswordJSON password) {
        String pass = password.getPassword();
        RuleResult result = passwordService.validatePassword(pass);

        String details = result.getDetails().toString();
        details = details.substring(1, details.length() - 1);
        boolean isValid = result.isValid();

        if (!isValid) {
            details = details.replace("ILLEGAL_WORD:", "Heslo obsahuje napadnuteľnú sekvenciu znakov: ")
                             .replace("matchingWord=", "")
                             .replace("TOO_LONG", "Príliš dlhé")
                             .replaceAll("Length=", " ");
        }

        PasswordMetadata passwordMetadata = new PasswordMetadata();
        passwordMetadata.setValid(isValid);
        passwordMetadata.setDetails(details);

        return passwordMetadata;
    }

}
