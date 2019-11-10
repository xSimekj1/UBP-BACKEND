package team.project.upb.api.model;

import javax.persistence.*;

@Entity
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String filePath;
    private String filename;
    private String senderUsername;
    @ManyToOne
    private User receiver;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }
}
