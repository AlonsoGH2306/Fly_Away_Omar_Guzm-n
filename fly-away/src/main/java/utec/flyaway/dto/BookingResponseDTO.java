package utec.flyaway.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDTO {
    private String id;
    private String bookingDate;
    private String flightId;
    private String flightNumber;
    private String customerId;
    private String customerFirstName;
    private String customerLastName;
    private String estDepartureTime;
    private String estArrivalTime;
}
