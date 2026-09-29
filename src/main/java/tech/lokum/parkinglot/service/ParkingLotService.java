package tech.lokum.parkinglot.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.lokum.parkinglot.dto.CreateParkingLotRequest;
import tech.lokum.parkinglot.dto.ParkingLotResponse;
import tech.lokum.parkinglot.entity.ParkingLot;
import tech.lokum.parkinglot.entity.User;
import tech.lokum.parkinglot.exception.BusinessException;
import tech.lokum.parkinglot.exception.ForbiddenOperationException;
import tech.lokum.parkinglot.exception.ParkingLotAlreadyExistsException;
import tech.lokum.parkinglot.exception.ResourceNotFoundException;
import tech.lokum.parkinglot.repository.ParkingLotRepository;
import tech.lokum.parkinglot.repository.UserRepository;
import org.springframework.http.HttpStatus;

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
    private final UserRepository userRepository;

    public ParkingLotService(ParkingLotRepository parkingLotRepository, UserRepository userRepository) {
        this.parkingLotRepository = parkingLotRepository;
        this.userRepository = userRepository;
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
        User operator = userRepository.findById(request.operatorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Operator " + request.operatorId() + " was not found"
                ));
        if (operator.getRole() != User.Role.OPERATOR) {
            throw new ForbiddenOperationException("Only operators can create parking lots"
            );
        }

        String name = request.name().strip();
        String address = request.address().strip();
        if (parkingLotRepository.existsByNameAndAddressAndStatus(name, address, ParkingLot.Status.ACTIVE)) {
            throw new ParkingLotAlreadyExistsException();
        }

        ParkingLot parkingLot = new ParkingLot();
        parkingLot.setName(name);
        parkingLot.setAddress(address);
        parkingLot.setCapacity(request.capacity());
        operator.addParkingLot(parkingLot);

        ParkingLot savedParkingLot = parkingLotRepository.save(parkingLot);
        return new ParkingLotResponse(
                savedParkingLot.getId(),
                savedParkingLot.getName(),
                savedParkingLot.getAddress(),
                savedParkingLot.getCapacity()
        );
    }
}
