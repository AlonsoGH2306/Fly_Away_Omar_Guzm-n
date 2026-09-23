package utec.flyaway.service;

import org.springframework.stereotype.Service;
import utec.flyaway.dto.AuthTokenDTO;
import utec.flyaway.dto.LoginDTO;
import utec.flyaway.entity.User;
import utec.flyaway.repository.UserRepository;
import utec.flyaway.utils.JwtUtil;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public AuthTokenDTO login(LoginDTO dto) {
        if (dto.getEmail() == null || dto.getPassword() == null) {
            throw new IllegalArgumentException("Email and password are mandatory");
        }

        User user = userRepository.findByEmail(dto.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!user.getPassword().equals(dto.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthTokenDTO(token);
    }
}
