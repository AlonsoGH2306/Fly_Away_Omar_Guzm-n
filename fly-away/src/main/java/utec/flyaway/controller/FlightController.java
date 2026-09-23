package utec.flyaway.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utec.flyaway.dto.*;
import utec.flyaway.service.BookingService;
import utec.flyaway.service.FlightService;
import utec.flyaway.utils.JwtUtil;

import java.util.UUID;

@RestController
@RequestMapping("/flights")
public class FlightController {

    private final FlightService flightService;
    private final BookingService bookingService;
    private final JwtUtil jwtUtil;

    public FlightController(FlightService flightService, BookingService bookingService, JwtUtil jwtUtil) {
        this.flightService = flightService;
        this.bookingService = bookingService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/create")
    public ResponseEntity<NewIdDTO> create(@RequestBody NewFlightRequestDTO dto) {
        try {
            NewIdDTO response = flightService.createFlight(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/create-many")
    public ResponseEntity<NewFlightManyResponseDTO> createMany(@RequestBody NewFlightManyRequestDTO dto) {
        try {
            NewFlightManyResponseDTO response = flightService.createManyFlights(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<FlightSearchResponseDTO> search(
            @RequestParam(required = false) String flightNumber,
            @RequestParam(required = false) String airlineName,
            @RequestParam(required = false) String airline,
            @RequestParam(required = false) String estDepartureTimeFrom,
            @RequestParam(required = false) String estDepartureTimeTo,
            @RequestParam(required = false) String departureFrom,
            @RequestParam(required = false) String departureTo,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            HttpServletRequest request) {

        if (!isAuthorized(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String fromDate = firstNonNull(estDepartureTimeFrom, departureFrom, from);
        String toDate = firstNonNull(estDepartureTimeTo, departureTo, to);
        String airlineValue = firstNonNull(airlineName, airline);

        FlightSearchResponseDTO response = flightService.searchFlights(
            flightNumber, airlineValue, fromDate, toDate);
        return ResponseEntity.ok(response);
    }

    private String firstNonNull(String... values) {
        for (String value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    @PostMapping("/book")
    public ResponseEntity<BookingResponseDTO> book(@RequestBody FlightBookRequestDTO dto, HttpServletRequest request) {
        if (!isAuthorized(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            UUID customerId = getUserIdFromToken(request);
            BookingResponseDTO response = bookingService.bookFlight(customerId, dto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("no encontrad")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrad")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/book/{id}")
    public ResponseEntity<BookingResponseDTO> getBooking(@PathVariable String id, HttpServletRequest request) {
        if (!isAuthorized(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            BookingResponseDTO response = bookingService.getBooking(UUID.fromString(id));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    private boolean isAuthorized(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        String token = authHeader.substring(7);
        try {
            jwtUtil.validateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private UUID getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = authHeader.substring(7);
        return jwtUtil.getUserIdFromToken(token);
    }
}
