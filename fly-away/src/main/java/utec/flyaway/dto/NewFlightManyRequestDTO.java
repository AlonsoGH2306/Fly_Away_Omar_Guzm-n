package utec.flyaway.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewFlightManyRequestDTO {
    private List<NewFlightRequestDTO> inputs;
}
