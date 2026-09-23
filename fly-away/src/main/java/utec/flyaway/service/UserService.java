package utec.flyaway.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utec.flyaway.dto.NewIdDTO;
import utec.flyaway.dto.RegisterUserDTO;
import utec.flyaway.entity.User;
import utec.flyaway.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public NewIdDTO registerUser(RegisterUserDTO dto) {
        validateUser(dto);

        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());

        User saved = userRepository.save(user);
        return new NewIdDTO(saved.getId().toString());
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getUserById(java.util.UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void validateUser(RegisterUserDTO dto) {
        if (dto.getFirstName() == null || dto.getLastName() == null || 
            dto.getEmail() == null || dto.getPassword() == null) {
            throw new IllegalArgumentException("All fields are mandatory");
        }

        if (!dto.getEmail().matches("^[a-z0-9_\\.]+@[a-z0-9_\\.]+\\.[a-z]{2,3}(\\.[a-z]{2})?$")) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (!dto.getFirstName().matches("^[A-z].*")) {
            throw new IllegalArgumentException("First name must start with a letter");
        }

        if (!dto.getLastName().matches("^[A-z].*")) {
            throw new IllegalArgumentException("Last name must start with a letter");
        }

        if (dto.getPassword().length() < 8 || 
            !dto.getPassword().matches(".*[A-Z].*") || 
            !dto.getPassword().matches(".*[0-9].*")) {
            throw new IllegalArgumentException("Password must be at least 8 characters with at least one uppercase letter and one number");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
    }
}
