package team.project.upb.api.controller;

import org.passay.RuleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import team.project.upb.api.model.*;
import team.project.upb.api.security.CustomPasswordEncoder;
import team.project.upb.api.security.JwtTokenProvider;
import team.project.upb.api.payload.ApiResponse;
import team.project.upb.api.payload.JwtAuthenticationResponse;
import team.project.upb.api.payload.LoginRequest;
import team.project.upb.api.payload.SignUpRequest;
import team.project.upb.api.repository.RoleRepository;
import team.project.upb.api.repository.UserRepository;
import team.project.upb.api.service.FileMetadataService;
import team.project.upb.api.service.PasswordService;
import team.project.upb.api.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CustomPasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileMetadataService fileMetadataService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) throws Exception {

        if(userRepository.existsByUsername(signUpRequest.getUsername())) {
            ApiResponse apiResponse = new ApiResponse(false, "Username is already taken!");
            return new ResponseEntity(apiResponse, HttpStatus.BAD_REQUEST);
        }

        //TODO - exception handling
        RuleResult ruleResult = passwordService.validatePassword(signUpRequest.getPassword());
        if (!ruleResult.isValid()){
            return new ResponseEntity(new ApiResponse(true, ruleResult.toString()), HttpStatus.BAD_REQUEST);
        }

        // Creating user's account
        User user = new User(signUpRequest.getUsername(), signUpRequest.getPassword());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new Exception("User Role not set."));

        user.setRoles(Collections.singleton(userRole));
        userRepository.save(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/users/{username}")
                .buildAndExpand(user.getUsername()).toUri();

        return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
    }

    @PostMapping(value = "/pass-strength", consumes = "application/json")
    public boolean checkPasswordStrength(@RequestBody PasswordJSON password) {
        String pass = password.getPassword();
        RuleResult result = passwordService.validatePassword(pass);
        System.out.println(result.getDetails());
        return result.isValid();
    }

    @PostMapping(value = "/sendfile")
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

    @GetMapping(value = "/getfiles")
    public List<FileMetadataDTO> getFiles(@RequestParam String username) {

        User user = userService.findByName(username);
        if (user == null) {
            return null;
        }

        return fileMetadataService.findAllByReceiverId(user.getId());
    }

    @PostMapping(value = "/downloadfile")
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

}
