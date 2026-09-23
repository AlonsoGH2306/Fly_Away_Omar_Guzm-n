package utec.flyaway.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightSearchResponseDTO {
    private List<FlightDTO> content;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlightDTO {
        private String id;
        private String airlineName;
        private String flightNumber;
        private String estDepartureTime;
        private String estArrivalTime;
        private Integer availableSeats;
    }
}
