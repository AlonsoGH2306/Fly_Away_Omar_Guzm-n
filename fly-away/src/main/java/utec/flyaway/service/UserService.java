package utec.flyaway.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utec.flyaway.dto.NewIdDTO;
import utec.flyaway.dto.RegisterUserDTO;
import utec.flyaway.entity.User;
import utec.flyaway.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public NewIdDTO registerUser(RegisterUserDTO dto) {
        validateUser(dto);

        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        User saved = userRepository.save(user);
        return new NewIdDTO(saved.getId().toString());
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public User getUserById(java.util.UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private void validateUser(RegisterUserDTO dto) {
        if (dto.getFirstName() == null || dto.getLastName() == null || 
            dto.getEmail() == null || dto.getPassword() == null) {
            throw new IllegalArgumentException("Todos los campos son obligatorios");
        }

        if (!dto.getEmail().matches("^[a-z0-9_\\.]+@[a-z0-9_\\.]+\\.[a-z]{2,3}(\\.[a-z]{2})?$")) {
            throw new IllegalArgumentException("Formato de email inválido");
        }

        if (!dto.getFirstName().matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("El nombre debe contener al menos una letra mayúscula");
        }

        if (!dto.getLastName().matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("El apellido debe contener al menos una letra mayúscula");
        }

        if (dto.getPassword().length() < 8 ||
            !dto.getPassword().matches(".*[A-Za-z].*") ||
            !dto.getPassword().matches(".*[0-9].*")) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres, con al menos una letra y un número");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
    }
}
