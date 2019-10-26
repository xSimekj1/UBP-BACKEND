package team.project.upb.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.project.upb.api.model.KeyPairPP;
import team.project.upb.api.repository.KeyPairRepository;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;

@Service
public class KeyService {

    @Autowired
    KeyPairRepository keyPairRepository;

    public Map<String ,String> generateKeys() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        kpg.initialize(1048, random);
        KeyPair kp = kpg.genKeyPair();
        Key publicKey = kp.getPublic();
        Key privateKey = kp.getPrivate();

        String publicString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        String privateString = Base64.getEncoder().encodeToString(privateKey.getEncoded());

        //Only one user with userId = 1
        KeyPairPP keyPairPPEntity = new KeyPairPP(1L,publicString,privateString);
        keyPairRepository.save(keyPairPPEntity);

        HashMap<String, String> keyMap = new HashMap<>();
        keyMap.put("publicK", publicString);
        keyMap.put("privateK", privateString);

        return keyMap;
    }

    public boolean isPublicKeyValid(String publicKey){
        return keyPairRepository.getOne(1L).getPublicKeyValue().equals(publicKey) ? true : false;
    }
}
