package utec.flyaway.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewFlightRequestDTO {
    @JsonAlias("airline")
    private String airlineName;
    private String flightNumber;
    @JsonAlias("departureTime")
    private String estDepartureTime;
    @JsonAlias("arrivalTime")
    private String estArrivalTime;
    private Integer availableSeats;
}
