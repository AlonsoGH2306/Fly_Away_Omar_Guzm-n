package utec.flyaway.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utec.flyaway.dto.*;
import utec.flyaway.entity.Flight;
import utec.flyaway.repository.FlightRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FlightService {

    private final FlightRepository flightRepository;

    public FlightService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Transactional
    public NewIdDTO createFlight(NewFlightRequestDTO dto) {
        validateFlight(dto);

        Flight flight = new Flight();
        flight.setAirlineName(dto.getAirlineName());
        flight.setFlightNumber(dto.getFlightNumber());
        flight.setEstDepartureTime(dto.getEstDepartureTime());
        flight.setEstArrivalTime(dto.getEstArrivalTime());
        flight.setAvailableSeats(dto.getAvailableSeats());

        Flight saved = flightRepository.save(flight);
        return new NewIdDTO(saved.getId().toString());
    }

    @Transactional
    public NewFlightManyResponseDTO createManyFlights(NewFlightManyRequestDTO dto) {
        List<String> ids = new ArrayList<>();

        for (NewFlightRequestDTO flightDTO : dto.getInputs()) {
            NewIdDTO result = createFlight(flightDTO);
            ids.add(result.getId());
        }

        return new NewFlightManyResponseDTO(ids);
    }

    public FlightSearchResponseDTO searchFlights(String flightNumber, String airlineName, 
                                                  String estDepartureTimeFrom, String estDepartureTimeTo) {
        Instant from = estDepartureTimeFrom != null ? Instant.parse(estDepartureTimeFrom) : null;
        Instant to = estDepartureTimeTo != null ? Instant.parse(estDepartureTimeTo) : null;

        List<Flight> flights;

        if (flightNumber != null && airlineName != null && from != null && to != null) {
            flights = flightRepository.findByFlightNumberContainingIgnoreCaseAndAirlineNameContainingIgnoreCaseAndEstDepartureTimeBetween(
                flightNumber, airlineName, from, to);
        } else if (flightNumber != null && airlineName != null && from != null) {
            flights = flightRepository.findByFlightNumberContainingIgnoreCaseAndAirlineNameContainingIgnoreCaseAndEstDepartureTimeGreaterThanEqual(
                flightNumber, airlineName, from);
        } else if (flightNumber != null && airlineName != null && to != null) {
            flights = flightRepository.findByFlightNumberContainingIgnoreCaseAndAirlineNameContainingIgnoreCaseAndEstDepartureTimeLessThanEqual(
                flightNumber, airlineName, to);
        } else if (flightNumber != null && from != null && to != null) {
            flights = flightRepository.findByFlightNumberContainingIgnoreCaseAndEstDepartureTimeBetween(
                flightNumber, from, to);
        } else if (airlineName != null && from != null && to != null) {
            flights = flightRepository.findByAirlineNameContainingIgnoreCaseAndEstDepartureTimeBetween(
                airlineName, from, to);
        } else if (flightNumber != null && airlineName != null) {
            flights = flightRepository.findByFlightNumberContainingIgnoreCaseAndAirlineNameContainingIgnoreCase(
                flightNumber, airlineName);
        } else if (flightNumber != null && from != null) {
            flights = flightRepository.findByFlightNumberContainingIgnoreCaseAndEstDepartureTimeGreaterThanEqual(
                flightNumber, from);
        } else if (flightNumber != null && to != null) {
            flights = flightRepository.findByFlightNumberContainingIgnoreCaseAndEstDepartureTimeLessThanEqual(
                flightNumber, to);
        } else if (airlineName != null && from != null) {
            flights = flightRepository.findByAirlineNameContainingIgnoreCaseAndEstDepartureTimeGreaterThanEqual(
                airlineName, from);
        } else if (airlineName != null && to != null) {
            flights = flightRepository.findByAirlineNameContainingIgnoreCaseAndEstDepartureTimeLessThanEqual(
                airlineName, to);
        } else if (from != null && to != null) {
            flights = flightRepository.findByEstDepartureTimeBetween(from, to);
        } else if (flightNumber != null) {
            flights = flightRepository.findByFlightNumberContainingIgnoreCase(flightNumber);
        } else if (airlineName != null) {
            flights = flightRepository.findByAirlineNameContainingIgnoreCase(airlineName);
        } else if (from != null) {
            flights = flightRepository.findByEstDepartureTimeGreaterThanEqual(from);
        } else if (to != null) {
            flights = flightRepository.findByEstDepartureTimeLessThanEqual(to);
        } else {
            flights = flightRepository.findAll();
        }

        List<FlightSearchResponseDTO.FlightDTO> items = flights.stream()
            .map(f -> new FlightSearchResponseDTO.FlightDTO(
                f.getId().toString(),
                f.getAirlineName(),
                f.getFlightNumber(),
                f.getEstDepartureTime().toString(),
                f.getEstArrivalTime().toString(),
                f.getAvailableSeats()
            ))
            .collect(Collectors.toList());

        return new FlightSearchResponseDTO(items);
    }

    public Flight getFlightById(UUID id) {
        return flightRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Flight not found"));
    }

    private void validateFlight(NewFlightRequestDTO dto) {
        if (dto.getAirlineName() == null || dto.getFlightNumber() == null || 
            dto.getEstDepartureTime() == null || dto.getEstArrivalTime() == null || 
            dto.getAvailableSeats() == null) {
            throw new IllegalArgumentException("All fields are mandatory");
        }

        if (!dto.getFlightNumber().matches("^[A-Z]{2,3}[0-9]{3}$")) {
            throw new IllegalArgumentException("Invalid flight number format");
        }

        if (dto.getAvailableSeats() <= 0) {
            throw new IllegalArgumentException("Available seats must be greater than 0");
        }

        if (dto.getEstDepartureTime().isAfter(dto.getEstArrivalTime()) || 
            dto.getEstDepartureTime().equals(dto.getEstArrivalTime())) {
            throw new IllegalArgumentException("Departure time must be before arrival time");
        }

        if (flightRepository.findByFlightNumber(dto.getFlightNumber()).isPresent()) {
            throw new IllegalArgumentException("Flight number already exists");
        }
    }
}
