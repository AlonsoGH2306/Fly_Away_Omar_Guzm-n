package utec.flyaway.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utec.flyaway.dto.*;
import utec.flyaway.entity.Flight;
import utec.flyaway.repository.FlightRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
        Instant departure = parseDate(dto.getEstDepartureTime());
        Instant arrival = parseDate(dto.getEstArrivalTime());
        validateFlight(dto, departure, arrival);

        Flight flight = new Flight();
        flight.setAirlineName(dto.getAirlineName());
        flight.setFlightNumber(dto.getFlightNumber());
        flight.setEstDepartureTime(departure);
        flight.setEstArrivalTime(arrival);
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
        Instant from = parseDate(estDepartureTimeFrom);
        Instant to = parseDate(estDepartureTimeTo);

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
            .orElseThrow(() -> new RuntimeException("Vuelo no encontrado"));
    }

    private Instant parseDate(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        try {
            return Instant.parse(trimmed);
        } catch (Exception e1) {
            try {
                return OffsetDateTime.parse(trimmed).toInstant();
            } catch (Exception e2) {
try {
                return LocalDate.parse(trimmed).atStartOfDay(ZoneOffset.UTC).toInstant();
            } catch (Exception e3) {
                try {
                    return LocalDateTime.parse(trimmed).atZone(ZoneOffset.UTC).toInstant();
                } catch (Exception e4) {
                    throw new IllegalArgumentException("Fecha de salida inválida: " + value);
                }
            }
            }
        }
    }

    private void validateFlight(NewFlightRequestDTO dto, Instant departure, Instant arrival) {
        if (dto.getAirlineName() == null || dto.getFlightNumber() == null || 
            departure == null || arrival == null || 
            dto.getAvailableSeats() == null) {
            throw new IllegalArgumentException("Todos los campos son obligatorios");
        }

        if (!dto.getFlightNumber().matches("^[A-Z0-9]{1,6}$")) {
            throw new IllegalArgumentException("Formato de número de vuelo inválido (solo A-Z y 0-9, máximo 6 caracteres)");
        }

        if (dto.getAvailableSeats() <= 0) {
            throw new IllegalArgumentException("Los asientos disponibles deben ser mayores a 0");
        }

        if (departure.isAfter(arrival) || departure.equals(arrival)) {
            throw new IllegalArgumentException("La hora de salida debe ser anterior a la hora de llegada");
        }

        if (flightRepository.findByFlightNumber(dto.getFlightNumber()).isPresent()) {
            throw new IllegalArgumentException("El número de vuelo ya existe");
        }
    }
}
