package tech.lokum.parkinglot.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.lokum.parkinglot.dto.CreateParkingLotRequest;
import tech.lokum.parkinglot.dto.ParkingLotResponse;
import tech.lokum.parkinglot.entity.ParkingLot;
import tech.lokum.parkinglot.exception.ParkingLotAlreadyExistsException;
import tech.lokum.parkinglot.repository.ParkingLotRepository;

/**
 * Business operations for parking lots.
 *
 * @author Parking Lot API team
 * @version 1.0
 * @since 1.0
 */
@Service
public class ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;

    public ParkingLotService(ParkingLotRepository parkingLotRepository) {
        this.parkingLotRepository = parkingLotRepository;
    }

    /**
     * Persists a new parking lot from validated request details.
     *
     * @param request validated parking lot creation details
     * @return the saved parking lot as an API response
     * @since 1.0
     */
    @Transactional
    public ParkingLotResponse createParkingLot(CreateParkingLotRequest request) {
        String name = request.name().strip();
        String location = request.location().strip();
        if (parkingLotRepository.existsByNameAndAddressAndStatus(
                name,
                location,
                ParkingLot.Status.ACTIVE
        )) {
            throw new ParkingLotAlreadyExistsException();
        }

        ParkingLot parkingLot = new ParkingLot();
        parkingLot.setName(name);
        parkingLot.setLocation(location);
        parkingLot.setCapacity(request.capacity());

        ParkingLot savedParkingLot = parkingLotRepository.save(parkingLot);
        return new ParkingLotResponse(
                savedParkingLot.getId(),
                savedParkingLot.getName(),
                savedParkingLot.getLocation(),
                savedParkingLot.getCapacity()
        );
    }
}
