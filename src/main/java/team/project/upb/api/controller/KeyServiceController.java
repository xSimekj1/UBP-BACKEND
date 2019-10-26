package team.project.upb.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import team.project.upb.api.service.KeyService;

import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping(value = "/api")
public class KeyServiceController {

    @Autowired
    KeyService keyService;

    @GetMapping("/generatekeys")
    public Map<String, String> encryptFile() throws Exception {
        return keyService.generateKeys();
    }
}
