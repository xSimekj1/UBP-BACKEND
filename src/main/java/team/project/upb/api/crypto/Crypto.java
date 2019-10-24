package team.project.upb.api.crypto;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Crypto {

    @Id
    private Long id;
    private String dummyString;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDummyString() {
        return dummyString;
    }

    public void setDummyString(String dummyString) {
        this.dummyString = dummyString;
    }
}
