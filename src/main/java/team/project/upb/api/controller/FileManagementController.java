package team.project.upb.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import team.project.upb.api.model.*;
import team.project.upb.api.service.CommentService;
import team.project.upb.api.service.FileMetadataService;
import team.project.upb.api.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(value = "/api/file")
public class FileManagementController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileMetadataService fileMetadataService;

    @Autowired
    private CommentService commentService;

    @PostMapping(value = "/send")
    public boolean saveFile(HttpServletRequest request,
                            @RequestParam("file") MultipartFile file,
                            @RequestParam("receiver") String receiverUsername,
                            @RequestParam("sender") String senderUsername) {

        // Check if user with this username exists, if not notice client
        User receiver = userService.findByName(receiverUsername);
        if (receiver == null) {
            return false;
        }

        // Get upper level of current directory
        String filePath = request.getServletContext().getRealPath(".");
        int index = (filePath.lastIndexOf("\\"));
        filePath = filePath.substring(0, index) + "\\" + file.getOriginalFilename();

        try {
            file.transferTo(new File(filePath));
        } catch (IOException e) {
            // TODO: log exception
            System.out.println(e);
        }

        FileMetadata fm = new FileMetadata();
        fm.setFilePath(filePath);
        fm.setFilename(file.getOriginalFilename());
        fm.setSenderUsername(senderUsername);
        fm.setReceiver(receiver);

        fileMetadataService.save(fm);

        return true;
    }

    @GetMapping(value = "/getall")
    public List<FileMetadataDTO> getFilesByUsername(@RequestParam String username) {

        User user = userService.findByName(username);
        if (user == null) {
            return null;
        }

        return fileMetadataService.findAllByReceiverId(user.getId());
    }

    @GetMapping(value = "/getrestriced")
    public List<FileMetadataDTO> getFiles(@RequestParam String username) {

        User user = userService.findByName(username);
        if (user == null) {
            return null;
        }

        return fileMetadataService.getAllWithRestrictDownload(user.getId());
    }

    @PostMapping(value = "/download")
    public ResponseEntity<byte[]> getFile(@RequestBody FileMetadataDTO fileMetadata) {

        FileMetadata fm = fileMetadataService.findById(fileMetadata.getId());

        File f = new File(fm.getFilePath());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData(fm.getFilename(), fm.getFilename());
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        ResponseEntity<byte[]> response = null;
        try {
            response = new ResponseEntity<>(Files.readAllBytes(f.toPath()), headers, HttpStatus.OK);
        } catch(IOException e) {
            // TODO: log exception
            System.out.println(e);
        }

        return response;
    }

    @PostMapping(value = "/update-comments")
    public CommentDTO updateComments(@RequestBody CommentRequest commentRequest) {

        FileMetadata fm = fileMetadataService.findById(commentRequest.getFileMetadataId());
        // handle case if file was deleted

        Comment comment = new Comment();
        comment.setContent(commentRequest.getContent());
        comment.setFileMetadata(fm);
        comment.setCommentedBy(commentRequest.getCommentedBy());
        commentService.save(comment);

        return new CommentDTO();
    }


}
