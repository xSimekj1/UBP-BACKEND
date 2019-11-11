package team.project.upb.api.service;

import org.passay.*;
import org.passay.dictionary.ArrayWordList;
import org.passay.dictionary.WordListDictionary;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
public class PasswordService {

    private List<Rule> defineRules() {

        List<Rule> rules = new ArrayList<>();
        rules.add(new LengthRule(8, 20));
//        rules.add(new WhitespaceRule());
        rules.add(new CharacterRule(EnglishCharacterData.UpperCase, 1));
        rules.add(new CharacterRule(EnglishCharacterData.LowerCase, 1));
        rules.add(new CharacterRule(EnglishCharacterData.Digit, 1));

        List<String> dictionaryList = new LinkedList<String>();

        try {
            File file = new File("dictionary.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;
            while((line = br.readLine()) != null) {
                if (line.length() >= 3) { // maybe 4 is more viable
                    dictionaryList.add(line);
                }
            }
        } catch (IOException e) {
            // TODO: log exception
            System.out.println(e.getMessage());
        }

        String[] dictionaryArray = dictionaryList.toArray(new String[0]);
        WordListDictionary wordListDictionary = new WordListDictionary(new ArrayWordList(dictionaryArray));
        DictionarySubstringRule dictionaryRule = new DictionarySubstringRule(wordListDictionary);

        rules.add(dictionaryRule);

        return rules;
    }

    public RuleResult validatePassword(String password){
        List<Rule> rules = this.defineRules();
        PasswordValidator validator = new PasswordValidator(rules);
        RuleResult result = validator.validate(new PasswordData(password));

        return result;
    }
}