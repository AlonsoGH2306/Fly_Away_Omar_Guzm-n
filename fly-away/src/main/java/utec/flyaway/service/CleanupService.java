package utec.flyaway.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utec.flyaway.repository.BookingRepository;
import utec.flyaway.repository.FlightRepository;
import utec.flyaway.repository.UserRepository;

@Service
public class CleanupService {

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final UserRepository userRepository;

    public CleanupService(BookingRepository bookingRepository,
                         FlightRepository flightRepository,
                         UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.flightRepository = flightRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void cleanup() {
        bookingRepository.deleteAll();
        flightRepository.deleteAll();
        userRepository.deleteAll();
    }
}
