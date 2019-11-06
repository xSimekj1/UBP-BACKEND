package team.project.upb.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import team.project.upb.api.service.KeyService;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(value = "/api")
public class KeyController {

    @Autowired
    KeyService keyService;

    @GetMapping("/generatekeys")
    public Map<String, String> generateKeys() throws Exception {
        return keyService.generateKeys();
    }

    @GetMapping("/getkeys")
    public Map<String, String> getKeys() throws Exception {
        return keyService.getKeys();
    }
}
