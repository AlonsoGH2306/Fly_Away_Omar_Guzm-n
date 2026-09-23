package utec.flyaway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utec.flyaway.entity.Flight;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FlightRepository extends JpaRepository<Flight, UUID> {
    Optional<Flight> findByFlightNumber(String flightNumber);
    List<Flight> findByFlightNumberContainingIgnoreCase(String flightNumber);
    List<Flight> findByAirlineNameContainingIgnoreCase(String airlineName);
    List<Flight> findByEstDepartureTimeGreaterThanEqual(Instant from);
    List<Flight> findByEstDepartureTimeLessThanEqual(Instant to);
    List<Flight> findByEstDepartureTimeBetween(Instant from, Instant to);

    // Para búsqueda combinada
    List<Flight> findByFlightNumberContainingIgnoreCaseAndAirlineNameContainingIgnoreCase(
        String flightNumber, String airlineName);
    List<Flight> findByFlightNumberContainingIgnoreCaseAndEstDepartureTimeGreaterThanEqual(
        String flightNumber, Instant from);
    List<Flight> findByFlightNumberContainingIgnoreCaseAndEstDepartureTimeLessThanEqual(
        String flightNumber, Instant to);
    List<Flight> findByAirlineNameContainingIgnoreCaseAndEstDepartureTimeGreaterThanEqual(
        String airlineName, Instant from);
    List<Flight> findByAirlineNameContainingIgnoreCaseAndEstDepartureTimeLessThanEqual(
        String airlineName, Instant to);
    List<Flight> findByFlightNumberContainingIgnoreCaseAndAirlineNameContainingIgnoreCaseAndEstDepartureTimeGreaterThanEqual(
        String flightNumber, String airlineName, Instant from);
    List<Flight> findByFlightNumberContainingIgnoreCaseAndAirlineNameContainingIgnoreCaseAndEstDepartureTimeLessThanEqual(
        String flightNumber, String airlineName, Instant to);
    List<Flight> findByFlightNumberContainingIgnoreCaseAndAirlineNameContainingIgnoreCaseAndEstDepartureTimeBetween(
        String flightNumber, String airlineName, Instant from, Instant to);
    List<Flight> findByFlightNumberContainingIgnoreCaseAndEstDepartureTimeBetween(
        String flightNumber, Instant from, Instant to);
    List<Flight> findByAirlineNameContainingIgnoreCaseAndEstDepartureTimeBetween(
        String airlineName, Instant from, Instant to);
}
