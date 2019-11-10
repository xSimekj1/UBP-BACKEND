package team.project.upb.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.project.upb.api.model.User;
import team.project.upb.api.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User findByName(String name) {
        Optional<User> optionalUser = userRepository.findByUsername(name);
        User user = null;

        if(optionalUser.isPresent()) {
            user = optionalUser.get();
        }

        return user;
    }

}
