package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)

class ParkingSpotDAOTest {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO mockParkingSpotDAO;
    private static TicketDAO mockTicketDAO;
    private static DataBasePrepareService dataBasePrepareService;


    @BeforeAll
    private static void setUp() {
        mockParkingSpotDAO = new ParkingSpotDAO();
        mockParkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        mockTicketDAO = new TicketDAO();
        mockTicketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();

    }

    @BeforeEach
    private void setUpPerTest() {
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown() {

    }

    /**
     * Method under test: {@link ParkingSpotDAO#getNextAvailableSlot(ParkingType)}
     */
    @Test
    void testParkingSpotsAreNotLessThanZero() {
        assertFalse((new ParkingSpotDAO()).getNextAvailableSlot(ParkingType.CAR) < 0);
        assertFalse((new ParkingSpotDAO()).getNextAvailableSlot(ParkingType.BIKE) < 0);
    }

    /**
     *
     * Method under test: {@link ParkingSpotDAO#getNextAvailableSlot(ParkingType)}
     */
    @Test
    void GivenAParkingSpotCheckIfMethodRetrievesCorrectId() {
        Connection con = null;
        try {
            con = dataBaseTestConfig.getConnection();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
            int parkingSpotNumber = mockParkingSpotDAO.getNextAvailableSlot(parkingSpot.getParkingType());
            assertEquals(parkingSpotNumber, parkingSpot.getId());
        } catch (NullPointerException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            dataBaseTestConfig.closeConnection(con);
        }
    }

    /**
     * Method under test: {@link ParkingSpotDAO#updateParking(ParkingSpot)}
     */
    @Test
    void testUpdateParkingSpotWithAvailability() {
        assertFalse(mockParkingSpotDAO.updateParking(new ParkingSpot(10, ParkingType.CAR, true)));
    }
}

