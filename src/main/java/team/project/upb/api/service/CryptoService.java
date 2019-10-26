package team.project.upb.api.service;

import java.util.Base64;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


@Service
public class CryptoService {

    public byte[] generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(128); // The AES key size in number of bits
        SecretKey secKey = generator.generateKey();

        return secKey.getEncoded();
    }

    public byte[] encryptSecretKey(byte[] secretKey, String publicKeyStr) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, NoSuchProviderException, InvalidKeySpecException, BadPaddingException, IllegalBlockSizeException {

        byte[] publicBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);

        //encrypt secret key with public key
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.PUBLIC_KEY, pubKey);
        byte[] encryptedKey = cipher.doFinal(secretKey/*Seceret Key From Step 1*/);

        return encryptedKey;
    }

    public byte[] encryptFileData(MultipartFile file, byte[] encryptedSecretKey) throws Exception {
        byte[] encFileByteArray = null;
        try {
            byte [] fileByteArr=file.getBytes();
            //generate secret key from byte data
            SecretKey encryptedKey = new SecretKeySpec(encryptedSecretKey, 0, encryptedSecretKey.length, "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(cipher.ENCRYPT_MODE, encryptedKey);

            encFileByteArray = cipher.doFinal(fileByteArr);

        } catch (IOException ex) {
            throw new Exception("Errorencrypting/decryptingfile"+ex.getMessage(),ex);
        } catch (BadPaddingException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return encFileByteArray;
    }

    public byte[] decryptSecretKey(byte[] encryptedSecretKey, String privateKeyStr) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        byte[] privateBytes = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec keySpec1 = new PKCS8EncodedKeySpec(privateBytes);
        KeyFactory keyFactoryPr = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactoryPr.generatePrivate(keySpec1);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.PRIVATE_KEY, privateKey);
        byte[] decryptedKeyBytes = cipher.doFinal(encryptedSecretKey);

//        SecretKey decryptedKey= new SecretKeySpec(decryptedKeyBytes , 0, decryptedKeyBytes .length, "AES");
        return decryptedKeyBytes;
    }

    public byte[] decryptFileData(MultipartFile file, byte[] secretKeyBytes) throws Exception {
        byte[] decryptedFileBytes = null;
        try {
            byte [] fileByteArr=file.getBytes();
            SecretKey secretKey = new SecretKeySpec(secretKeyBytes, 0, secretKeyBytes.length, "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            decryptedFileBytes = cipher.doFinal(fileByteArr);

        } catch (IOException ex) {
            throw new Exception("Errorencrypting/decryptingfile"+ex.getMessage(),ex);
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return decryptedFileBytes;
    }
    
    public  ResponseEntity<byte[]> downloadFile(byte[] encryptedFile, MultipartFile file, byte[] encryptedKey) throws IOException {
        //TODO - do not send key in httpheaders?

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData(file.getName(), file.getName());
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        if (encryptedKey != null)
            headers.add("encryptedKey", Base64.getEncoder().encodeToString(encryptedKey));
        ResponseEntity<byte[]> response = new ResponseEntity<>(encryptedFile, headers, HttpStatus.OK);
        return response;
    }
}
