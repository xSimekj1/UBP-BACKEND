package team.project.upb.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import team.project.upb.api.service.CryptoService;
import team.project.upb.api.service.KeyService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Arrays;
import java.util.Base64;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(value = "/api")
public class CryptoController {

    @Autowired
    CryptoService cryptoService;

    @Autowired
    KeyService keyService;

    private final boolean IS_TEST = false;
    private final int SECRET_KEY_LENGTH = 128;
    private final int SIGNATURE_LENGTH = 128;

    @PostMapping(path = "/encrypt")
    public ResponseEntity<byte[]> encryptFile(@RequestParam("file") MultipartFile file,
                                              @RequestParam("publicKey") String publicKey) throws Exception {

        if (!keyService.isPublicKeyValid(publicKey)) {
            throw new Exception("Not valid Public Key");
        }

        byte[] secretKey = cryptoService.generateSecretKey();
        byte[] encryptedSecretKey = cryptoService.encryptSecretKey(secretKey, publicKey);
        byte[] encFileBytes = cryptoService.encryptFileData(file, secretKey);

        // Verify integrity
//        Signature sig = Signature.getInstance("SHA1WithRSA");
//        PublicKey pK = keyService.getPublickey(publicKey);
//        sig.initVerify(pK);
//        sig.update(encFileBytes);
//
//        if (sig.verify(encFileBytes)) {
//            System.out.println("verifikovane");
//        }
//        else {
//            System.out.println("narusena integrita");
//        }

        // Write secret key at the start of the encrypted file
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(encryptedSecretKey);
        outputStream.write(encFileBytes);

        byte[] encryptedSecretKeyArr = new byte[SECRET_KEY_LENGTH];
        InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
        is.read(encryptedSecretKeyArr, 0, encryptedSecretKeyArr.length);

        return cryptoService.downloadFile(outputStream.toByteArray(), "encrypted" + file.getOriginalFilename());

    }

    @PostMapping(path = "/decrypt")
    public ResponseEntity<byte[]> decryptFile(@RequestParam("file") MultipartFile file,
                                              @RequestParam("privateKey") String privateKey) throws Exception {

        byte[] encryptedSecretKeyArr = new byte[SECRET_KEY_LENGTH];
        file.getInputStream().read(encryptedSecretKeyArr, 0, encryptedSecretKeyArr.length);

        byte[] secretKey = cryptoService.decryptSecretKey(encryptedSecretKeyArr, privateKey);
        byte[] decFileBytes = cryptoService.decryptFileData(Arrays.copyOfRange(file.getBytes(),SECRET_KEY_LENGTH, file.getBytes().length), secretKey);

        // integrity
//        Signature sig = Signature.getInstance("SHA1WithRSA");
//        sig.initSign(keyService.getPrivatekey(privateKey));
//        sig.update(file.getBytes());
//        byte[] signatureBytes = sig.sign();
//        System.out.println(signatureBytes.length);

        return cryptoService.downloadFile(decFileBytes, "decrypted" + file.getOriginalFilename());
//        return cryptoService.downloadFile(signatureBytes, "decrypted" + file.getOriginalFilename());

    }

    @GetMapping(path = "/offlineapp")
    public ResponseEntity getApp() throws IOException {
//        File f = new File("DecryptingApp.jar");
        File catalinaBase = new File( System.getProperty( "catalina.base" ) ).getAbsoluteFile();
        File f = new File( catalinaBase, "webapps/DecryptingApp.jar" );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData(f.getName(), f.getName());
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(Files.readAllBytes(f.toPath()), headers, HttpStatus.OK);
        return response;
    }

    @GetMapping(path = "/test")
    public String testCall() {
        System.out.println("called");
        return "works";
    }

}
