package team.project.upb.api.service;

import org.passay.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    public RuleResult validatePassword(String inputPassword){
        List<Rule> rules = new ArrayList<>();
        //Rule 1: Password length should be in between
        //8 and 16 characters
        rules.add(new LengthRule(8, 16));
        //Rule 2: No whitespace allowed
        rules.add(new WhitespaceRule());
        //Rule 3.a: At least one Upper-case character
        rules.add(new CharacterRule(EnglishCharacterData.UpperCase, 1));
        //Rule 3.b: At least one Lower-case character
        rules.add(new CharacterRule(EnglishCharacterData.LowerCase, 1));
        //Rule 3.c: At least one digit
        rules.add(new CharacterRule(EnglishCharacterData.Digit, 1));

        PasswordValidator validator = new PasswordValidator(rules);
        PasswordData password = new PasswordData(inputPassword);
        RuleResult result = validator.validate(password);

//        if(result.isValid()){
//            System.out.println("Password validated.");
//
//        } else {
//            System.out.println("Invalid Password: " + validator.getMessages(result));
//        }
        return result;
    }
}