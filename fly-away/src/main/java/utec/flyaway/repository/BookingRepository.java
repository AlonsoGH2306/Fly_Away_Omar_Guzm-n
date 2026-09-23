package utec.flyaway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utec.flyaway.entity.Booking;
import utec.flyaway.entity.User;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByCustomer(User customer);
    List<Booking> findByCustomerId(UUID customerId);
}
