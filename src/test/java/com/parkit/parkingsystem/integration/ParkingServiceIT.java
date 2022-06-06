
package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.util.Date;

@ExtendWith(MockitoExtension.class)

public class ParkingServiceIT {

    public static final String TEST_CAR_REG_NUMBER = "TSTCAR";
    public static final String TEST_BIKE_REG_NUMBER = "TSTBIKE";
    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO mockTicketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
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

    @Test
    public void calculateFareCarForRecurrentUser() throws Exception {
        Connection con = null;
        try {
            con = dataBaseTestConfig.getConnection();
            long timeIn = System.currentTimeMillis() - (60 * 60 * 1000);
            Date inTime = new Date(timeIn);
            ParkingService inTimeSpy = Mockito.spy(new ParkingService(inputReaderUtil, parkingSpotDAO, mockTicketDAO));
            doReturn(inTime).when(inTimeSpy).makeInTime();
            when(inputReaderUtil.readSelection()).thenReturn(Integer.valueOf("1"));
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(TEST_CAR_REG_NUMBER);
            inTimeSpy.processIncomingVehicle();
            inTimeSpy.processExitingVehicle();
            inTimeSpy.processIncomingVehicle();
            inTimeSpy.processExitingVehicle();
            assertEquals((Fare.CAR_RATE_PER_HOUR) - ((Fare.CAR_RATE_PER_HOUR) * 0.05), mockTicketDAO.getTicket(TEST_CAR_REG_NUMBER).getPrice());
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            dataBaseTestConfig.closeConnection(con);
        }
    }

    @Test
    public void calculateFareBikeForRecurrentUser() throws Exception {
        Connection con = null;
        try {
            con = dataBaseTestConfig.getConnection();
            long timeIn = System.currentTimeMillis() - (60 * 60 * 1000);
            Date inTime = new Date(timeIn);
            ParkingService inTimeSpy = Mockito.spy(new ParkingService(inputReaderUtil, parkingSpotDAO, mockTicketDAO));
            doReturn(inTime).when(inTimeSpy).makeInTime();
            when(inputReaderUtil.readSelection()).thenReturn(Integer.valueOf("2"));
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(TEST_BIKE_REG_NUMBER);
            inTimeSpy.processIncomingVehicle();
            inTimeSpy.processExitingVehicle();
            inTimeSpy.processIncomingVehicle();
            inTimeSpy.processExitingVehicle();
            assertEquals((Fare.BIKE_RATE_PER_HOUR) - ((Fare.BIKE_RATE_PER_HOUR) * 0.05), mockTicketDAO.getTicket(TEST_BIKE_REG_NUMBER).getPrice());
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            dataBaseTestConfig.closeConnection(con);
        }
    }
}
