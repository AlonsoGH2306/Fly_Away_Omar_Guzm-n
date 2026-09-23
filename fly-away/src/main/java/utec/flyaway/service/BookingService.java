package utec.flyaway.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utec.flyaway.dto.BookingResponseDTO;
import utec.flyaway.dto.FlightBookRequestDTO;
import utec.flyaway.dto.NewIdDTO;
import utec.flyaway.entity.Booking;
import utec.flyaway.entity.Flight;
import utec.flyaway.entity.User;
import utec.flyaway.repository.BookingRepository;
import utec.flyaway.repository.FlightRepository;
import utec.flyaway.repository.UserRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                         FlightRepository flightRepository,
                         UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.flightRepository = flightRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public NewIdDTO bookFlight(UUID customerId, FlightBookRequestDTO dto) {
        if (dto.getFlightId() == null || dto.getFlightId().isEmpty()) {
            throw new IllegalArgumentException("flightId es obligatorio");
        }

        UUID flightId = UUID.fromString(dto.getFlightId());
        Flight flight = flightRepository.findById(flightId)
            .orElseThrow(() -> new RuntimeException("Vuelo no encontrado: " + dto.getFlightId()));

        User customer = userRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Check if flight is in the past
        Instant now = Instant.now();
        if (flight.getEstDepartureTime().isBefore(now) || flight.getEstArrivalTime().isBefore(now)) {
            throw new IllegalArgumentException("El vuelo " + dto.getFlightId() + " ya está en el pasado o en tránsito");
        }

        // Check available seats
        if (flight.getAvailableSeats() <= 0) {
            throw new IllegalArgumentException("El vuelo " + dto.getFlightId() + " no tiene asientos disponibles (sobreventa)");
        }

        // Check overlapping flights
        List<Booking> customerBookings = bookingRepository.findByCustomerId(customerId);
        for (Booking existingBooking : customerBookings) {
            Flight existingFlight = existingBooking.getFlight();
            if (!existingFlight.getId().equals(flightId)) {
                boolean overlaps = flight.getEstDepartureTime().isBefore(existingFlight.getEstArrivalTime()) &&
                                  flight.getEstArrivalTime().isAfter(existingFlight.getEstDepartureTime());
                if (overlaps) {
                    throw new IllegalArgumentException("Conflicto de horario: ya tienes un vuelo reservado en ese rango");
                }
            }
        }

        // Create booking
        Booking booking = new Booking();
        booking.setBookingDate(Instant.now());
        booking.setFlight(flight);
        booking.setCustomer(customer);
        booking.setCustomerFirstName(customer.getFirstName());
        booking.setCustomerLastName(customer.getLastName());

        Booking saved = bookingRepository.save(booking);

        flight.setAvailableSeats(flight.getAvailableSeats() - 1);
        flightRepository.save(flight);

        generateConfirmationEmail(saved, flight);

        return new NewIdDTO(saved.getId().toString());
    }

    public BookingResponseDTO getBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        Flight flight = booking.getFlight();

        return new BookingResponseDTO(
            booking.getId().toString(),
            booking.getBookingDate().toString(),
            flight.getId().toString(),
            flight.getFlightNumber(),
            booking.getCustomer().getId().toString(),
            booking.getCustomerFirstName(),
            booking.getCustomerLastName(),
            flight.getEstDepartureTime().toString(),
            flight.getEstArrivalTime().toString()
        );
    }

    private void generateConfirmationEmail(Booking booking, Flight flight) {
        String passenger = booking.getCustomerFirstName() + " " + booking.getCustomerLastName();
        String message = "Hola " + passenger + ", ¡tu reserva fue exitosa! " +
            "La reserva es para el vuelo " + flight.getFlightNumber() +
            " con fecha de salida " + flight.getEstDepartureTime() +
            " y fecha de llegada " + flight.getEstArrivalTime() +
            ". La reserva fue registrada el " + booking.getBookingDate() +
            ". ¡Buen viaje! Fly Away Travel";

        String fileName = "flight_booking_email_" + booking.getId() + ".txt";
        try {
            Files.writeString(Path.of(fileName), message, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo generar el email de confirmación para la reserva " + booking.getId(), e);
        }
    }


}
