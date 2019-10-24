package team.project.upb.api;

import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@CrossOrigin
@RequestMapping(value = "/api")
public class CryptoController {

    @PostMapping(path = "/encrypt")
    public String encryptFile(@RequestBody File file) {
        return "encrypt endpoint works!";
    }

    @GetMapping(path = "/file", produces = "text/plain")
    public String getEncryptedFile() {
        return "encrypted file endpoint";
    }
}
