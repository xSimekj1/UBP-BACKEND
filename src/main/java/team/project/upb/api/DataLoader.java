package team.project.upb.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import team.project.upb.api.model.Role;
import team.project.upb.api.model.RoleName;
import team.project.upb.api.repository.RoleRepository;

import javax.persistence.NoResultException;
import java.util.Optional;

@Component
public class DataLoader implements ApplicationRunner {

    private RoleRepository roleRepository;

    @Autowired
    public DataLoader(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public void run(ApplicationArguments args) throws Exception {
        Optional<Role> userRole = roleRepository.findByName(RoleName.ROLE_USER);
        if (!userRole.isPresent())
            roleRepository.save(new Role(RoleName.ROLE_USER));

        Optional<Role> adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN);
        if (!userRole.isPresent())
            roleRepository.save(new Role(RoleName.ROLE_ADMIN));
    }
}