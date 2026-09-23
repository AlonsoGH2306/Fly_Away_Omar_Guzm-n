package utec.flyaway.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewFlightRequestDTO {
    private String airlineName;
    private String flightNumber;
    private Instant estDepartureTime;
    private Instant estArrivalTime;
    private Integer availableSeats;
}
