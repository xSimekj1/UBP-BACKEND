package team.project.upb.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import team.project.upb.api.service.CryptoService;
import team.project.upb.api.service.KeyService;

import java.io.*;
import java.util.Base64;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(value = "/api")
public class CryptoController {

    @Autowired
    CryptoService cryptoService;
    @Autowired
    KeyService keyService;

    @PostMapping(path = "/encrypt")
    public ResponseEntity<byte[]> encryptFile(@RequestParam("file") MultipartFile file, @RequestParam("publicKey") String publicKey) throws Exception {
        if (!keyService.isPublicKeyValid(publicKey))
            throw new Exception("Not valid Public Key");

        byte[] secretKey = cryptoService.generateSecretKey();
        byte[] encryptedSecretKey = cryptoService.encryptSecretKey(secretKey, publicKey);

        byte[] encFileBytes = cryptoService.encryptFileData(file,secretKey);

        //Temporary for downloading files localy for testing
        File convFile = new File(file.getOriginalFilename());
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(encFileBytes);
        fos.close();

        return cryptoService.downloadFile(encFileBytes,file,encryptedSecretKey);
    }

    @PostMapping(path = "/decrypt")
    public ResponseEntity<byte[]> decryptFile(@RequestParam("file") MultipartFile file,
                                              @RequestParam("encryptedSecretKey") String encryptedSecretKeyStr,
                                              @RequestParam("privateKey") String privateKey) throws Exception {

        byte[] encryptedSecretKey = Base64.getDecoder().decode(encryptedSecretKeyStr);
        byte[] secretKey = cryptoService.decryptSecretKey(encryptedSecretKey, privateKey);

        byte[] decFileBytes = cryptoService.decryptFileData(file, secretKey);

        return cryptoService.downloadFile(decFileBytes, file, null);
    }

    @GetMapping(path = "/test")
    public String testCall() {
        System.out.println("called");
        return "works";
    }

}
