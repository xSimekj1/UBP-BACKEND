package team.project.upb.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import team.project.upb.api.model.User;
import org.springframework.security.core.context.SecurityContextHolder;
import team.project.upb.api.repository.UserRepository;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;

@Service
public class KeyService {

    @Autowired
    UserRepository userRepository;

    public Map<String, String> getKeys() throws Exception {
        String loggedInUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepository.findByUsername(loggedInUsername).orElseThrow(() ->
                new UsernameNotFoundException("User not found with username: " + loggedInUsername)
        );

        HashMap<String, String> keyMap = new HashMap<>();
        keyMap.put("publicK", loggedInUser.getPublicKeyValue());
        keyMap.put("privateK", loggedInUser.getPrivateKeyValue());

        return keyMap;
    }

    public Map<String, String> generateKeys() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        kpg.initialize(1024, random);
        KeyPair kp = kpg.genKeyPair();
        Key publicKey = kp.getPublic();
        Key privateKey = kp.getPrivate();

        String publicString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        String privateString = Base64.getEncoder().encodeToString(privateKey.getEncoded());

        String loggedInUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepository.findByUsername(loggedInUsername).orElseThrow(() ->
                new UsernameNotFoundException("User not found with username: " + loggedInUsername)
        );

        loggedInUser.setPublicKeyValue(publicString);
        loggedInUser.setPrivateKeyValue(privateString);
        userRepository.save(loggedInUser);

        HashMap<String, String> keyMap = new HashMap<>();
        keyMap.put("publicK", publicString);
        keyMap.put("privateK", privateString);

        return keyMap;
    }

    public boolean isPublicKeyValid(String publicKey){
        String loggedInUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepository.findByUsername(loggedInUsername).orElseThrow(() ->
                new UsernameNotFoundException("User not found with username: " + loggedInUsername)
        );
        return loggedInUser.getPublicKeyValue().equals(publicKey);
    }

    public String getUserPublickey(String username) {
        User loggedInUser = userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("User not found with username: " + username)
        );

        return loggedInUser.getPublicKeyValue();
    }

    public PublicKey getPublickey(String key) {
        try{
            byte[] byteKey = Base64.getDecoder().decode(key.getBytes());
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);

            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(X509publicKey);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public PrivateKey getPrivatekey(String key) {
        try{

            byte[] byteKey = Base64.getDecoder().decode(key.getBytes());
            PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(byteKey);

            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(keySpecPKCS8);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }
}
