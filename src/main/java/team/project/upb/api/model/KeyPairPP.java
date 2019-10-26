package team.project.upb.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class KeyPairPP {
    @Id
    private Long userId;
    @Column(columnDefinition="TEXT")
    private String publicKeyValue;
    @Column(columnDefinition="TEXT")
    private String privateKeyValue;

    public KeyPairPP() {
    }

    public KeyPairPP(Long userId, String publicKeyValue, String privateKeyValue) {
        this.userId = userId;
        this.publicKeyValue = publicKeyValue;
        this.privateKeyValue = privateKeyValue;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPublicKeyValue() {
        return publicKeyValue;
    }

    public void setPublicKeyValue(String publicKeyValue) {
        this.publicKeyValue = publicKeyValue;
    }

    public String getPrivateKeyValue() {
        return privateKeyValue;
    }

    public void setPrivateKeyValue(String privateKeyValue) {
        this.privateKeyValue = privateKeyValue;
    }
}
