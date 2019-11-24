package team.project.upb.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import team.project.upb.api.service.CryptoService;
import team.project.upb.api.service.KeyService;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;

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
    private final int CHECKSUM_LENGTH = 64;

    @PostMapping(path = "/encrypt")
    public ResponseEntity<byte[]> encryptFile(@RequestParam("file") MultipartFile file,
                                              @RequestParam("publicKey") String publicKey) throws Exception {

        if (!keyService.isPublicKeyValid(publicKey)) {
            throw new Exception("Not valid Public Key");
        }

        String checksum = cryptoService.calculateChecksum(file.getName(),file.getBytes());

        byte[] secretKey = cryptoService.generateSecretKey();
        byte[] encryptedSecretKey = cryptoService.encryptSecretKey(secretKey, publicKey);
        byte[] encFileBytes = cryptoService.encryptFileData(file, secretKey);

        // Write secret key at the start of the encrypted file
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(checksum.getBytes());
        outputStream.write(encryptedSecretKey);
        outputStream.write(encFileBytes);

        return cryptoService.downloadFile(outputStream.toByteArray(), "encrypted" + file.getOriginalFilename());
    }

    @PostMapping(path = "/decrypt")
    public ResponseEntity<byte[]> decryptFile(@RequestParam("file") MultipartFile file,
                                              @RequestParam("privateKey") String privateKey) throws Exception {

        byte[] checksumBytes = Arrays.copyOfRange(file.getBytes(), 0, CHECKSUM_LENGTH);
        byte[] encryptedSecretKeyArr = Arrays.copyOfRange(file.getBytes(), CHECKSUM_LENGTH, CHECKSUM_LENGTH + SECRET_KEY_LENGTH);

        byte[] secretKey = cryptoService.decryptSecretKey(encryptedSecretKeyArr, privateKey);
        byte[] decFileBytes = cryptoService.decryptFileData(Arrays.copyOfRange(file.getBytes(),
                CHECKSUM_LENGTH + SECRET_KEY_LENGTH, file.getBytes().length), secretKey);

        String checksum = cryptoService.calculateChecksum(file.getName(),decFileBytes);
        String checksumFromFile = new String (checksumBytes);
        if (!checksum.equals(checksumFromFile)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Checksum not equal", new Exception());
        }

        return cryptoService.downloadFile(decFileBytes, "decrypted" + file.getOriginalFilename());
    }

    @GetMapping(path = "/offlineapp")
    public ResponseEntity getApp() throws IOException {
//        File f = new File("DecryptingApp.jar");
        //Server verzia
        File catalinaBase = new File( System.getProperty( "catalina.base" ) ).getAbsoluteFile();
        File f = new File( catalinaBase, "webapps/DecryptingApp.jar" );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData(f.getName(), f.getName());
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(Files.readAllBytes(f.toPath()), headers, HttpStatus.OK);
        return response;
    }

}
