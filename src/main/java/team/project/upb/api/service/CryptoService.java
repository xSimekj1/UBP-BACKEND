package team.project.upb.api.service;

import java.util.Base64;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


@Service
public class CryptoService {

    private final String initVector = "encryptionIntVec";

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

    public byte[] encryptFileData(MultipartFile file, byte[] secretKeyBytes) throws Exception {
        byte[] encFileByteArray = null;
        try {
            byte[] fileByteArr = file.getBytes();

            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(secretKeyBytes, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(cipher.ENCRYPT_MODE, skeySpec, iv);

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

//    public byte[] decryptFileData(MultipartFile file, byte[] secretKeyBytes) throws Exception {
    public byte[] decryptFileData(byte[] fileByteArr, byte[] secretKeyBytes) throws Exception {

        byte[] decryptedFileBytes = null;
        try {
//            byte [] fileByteArr=file.getBytes();

            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(secretKeyBytes, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            decryptedFileBytes = cipher.doFinal(fileByteArr);

        } catch (IOException ex) {
            throw new Exception("Errorencrypting/decryptingfile"+ex.getMessage(),ex);
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return decryptedFileBytes;
    }
    
    public  ResponseEntity<byte[]> downloadFile(byte[] encryptedFile, String fileName) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData(fileName, fileName);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        ResponseEntity<byte[]> response = new ResponseEntity<>(encryptedFile, headers, HttpStatus.OK);
        return response;
    }
}
