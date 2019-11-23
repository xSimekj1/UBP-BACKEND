package team.project.upb.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import team.project.upb.api.model.*;
import team.project.upb.api.repository.UserRepository;
import team.project.upb.api.service.*;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(value = "/api/file")
public class FileManagementController {

    private final int SECRET_KEY_LENGTH = 128;

    @Autowired
    private UserService userService;

    @Autowired
    private KeyService keyService;

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private FileMetadataService fileMetadataService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping(value = "/send")
    public boolean saveFile(HttpServletRequest request,
                            @RequestParam("file") MultipartFile file,
                            @RequestParam("receiver") String receiverUsername,
                            @RequestParam("sender") String senderUsername) throws Exception {

        // Check if user with this username exists, if not notice client
        User receiver = userService.findByName(receiverUsername);
        if (receiver == null) {
            return false;
        }

        String receiverPublicKey = keyService.getUserPublickey(receiverUsername);
        byte[] secretKey = cryptoService.generateSecretKey();
        byte[] encryptedSecretKey = cryptoService.encryptSecretKey(secretKey, receiverPublicKey);
        byte[] encFileBytes = cryptoService.encryptFileData(file, secretKey);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(encryptedSecretKey);
        outputStream.write(encFileBytes);

        byte[] encryptedSecretKeyArr = new byte[SECRET_KEY_LENGTH];
        InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
        is.read(encryptedSecretKeyArr, 0, encryptedSecretKeyArr.length);

        MultipartFile result = new MockMultipartFile(file.getName(),
                file.getOriginalFilename(), file.getContentType(), outputStream.toByteArray());

        // Get upper level of current directory
        String filePath = request.getServletContext().getRealPath(".");
        int index = (filePath.lastIndexOf("\\"));
        filePath = filePath.substring(0, index) + "\\" + file.getOriginalFilename();

       try {
            result.transferTo(new File(filePath));
        } catch (IOException e) {
            // TODO: log exception
            System.out.println(e);
        }

        FileMetadata fm = new FileMetadata();
        fm.setFilePath(filePath);
        fm.setFilename(file.getOriginalFilename());
        fm.setSenderUsername(senderUsername);
        fm.getReceivers().add(receiver);

        fileMetadataService.save(fm);

        return true;
    }

    @PutMapping(value = "/update-receivers")
    public void updateReceivers(@RequestBody Map<String, String> payload) {

        User receiver = userService.findByName(payload.get("receiver"));
        FileMetadata fm = fileMetadataService.findById(new Long(payload.get("fileId")));
        if (fm.getReceivers().add(receiver)) {
            fileMetadataService.save(fm);
        }
    }

    @GetMapping(value = "/getrestriced")
    public List<FileMetadataDTO> getFiles(@RequestParam String username) {

        User user = userService.findByName(username);
        if (user == null) {
            return null;
        }

        return fileMetadataService.getAllWithRestrictDownload(user.getId());
    }

    @DeleteMapping(value = "/deletefile")
    public Map<String, Boolean> deleteEmployee(@RequestBody FileMetadataDTO fileMetadataDto) {

        fileMetadataService.deleteFile(fileMetadataDto.getId());

        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return response;
    }

    @PostMapping(value = "/download")
    public ResponseEntity<byte[]> getFile(@RequestBody FileMetadataDTO fileMetadata) throws Exception {
        String loggedInUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userRepository.findByUsername(loggedInUsername).orElseThrow(() ->
                new UsernameNotFoundException("User not found with username: " + loggedInUsername)
        );

        FileMetadata fm = fileMetadataService.findById(fileMetadata.getId());

        File f = new File(fm.getFilePath());

        MultipartFile file = new MockMultipartFile(f.getName(),
                f.getName(), new MimetypesFileTypeMap().getContentType(f), Files.readAllBytes(f.toPath()));

        byte[] encryptedSecretKeyArr = new byte[SECRET_KEY_LENGTH];
        file.getInputStream().read(encryptedSecretKeyArr, 0, encryptedSecretKeyArr.length);

        byte[] secretKey = cryptoService.decryptSecretKey(encryptedSecretKeyArr, loggedInUser.getPrivateKeyValue());
        byte[] decFileBytes = cryptoService.decryptFileData(Arrays.copyOfRange(file.getBytes(),SECRET_KEY_LENGTH, file.getBytes().length), secretKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData(fm.getFilename(), fm.getFilename());
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        ResponseEntity<byte[]> response = null;
        response = new ResponseEntity<>(decFileBytes, headers, HttpStatus.OK);

        return response;
    }

    @PostMapping(value = "/downloadenc")
    public ResponseEntity<byte[]> getEncFile(@RequestBody FileMetadataDTO fileMetadata) throws Exception{
        FileMetadata fm = fileMetadataService.findById(fileMetadata.getId());

        File f = new File(fm.getFilePath());

        MultipartFile file = new MockMultipartFile(f.getName(),
                f.getName(), new MimetypesFileTypeMap().getContentType(f), Files.readAllBytes(f.toPath()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData(fm.getFilename(), fm.getFilename());
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        ResponseEntity<byte[]> response = null;
        response = new ResponseEntity<>(file.getBytes(), headers, HttpStatus.OK);

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
