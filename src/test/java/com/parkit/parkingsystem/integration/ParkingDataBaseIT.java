package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
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
public class ParkingDataBaseIT {
    public static final String TEST_VEHICLE_REG_NUMBER = "TST CAR";
    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO mockTicketDAO;
    private static FareCalculatorService fareCalculatorService;
    private static DataBasePrepareService dataBasePrepareService;
    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        mockTicketDAO = new TicketDAO();
        mockTicketDAO.dataBaseConfig = dataBaseTestConfig;
        fareCalculatorService = new FareCalculatorService();
        dataBasePrepareService = new DataBasePrepareService();

    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(TEST_VEHICLE_REG_NUMBER);
        dataBasePrepareService.clearDataBaseEntries();

    }

    @AfterAll
    private static void tearDown() {

    }

    /**
     * checks that a ticket is actually saved in DB and Parking table is updated with availability
     * @throws Exception
     */
    @Test
    public void testParkingACar() throws Exception {
        Connection con = null;
        try {
            con = dataBaseTestConfig.getConnection();
            ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, mockTicketDAO);
            parkingService.processIncomingVehicle();
            Ticket ticket = mockTicketDAO.getTicket(TEST_VEHICLE_REG_NUMBER);
            assertFalse(ticket.getParkingSpot().isAvailable());

        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            dataBaseTestConfig.closeConnection(con);
        }
    }


    /**
     * checks that the fare generated and out time are populated correctly in the database
     */
    @Test
    public void testParkingLotExit() throws Exception {
        Connection con = null;
        try {
            con = dataBaseTestConfig.getConnection();
            long timeIn = System.currentTimeMillis() - (60 * 60 * 1000);
            Date inTime = new Date(timeIn);
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            ParkingService pSSpy = Mockito.spy(new ParkingService(inputReaderUtil, parkingSpotDAO, mockTicketDAO));
            doReturn(inTime).when(pSSpy).makeInTime();
            doReturn(outTime).when(pSSpy).makeOutTime();
            pSSpy.processIncomingVehicle();
            pSSpy.processExitingVehicle();
            assertTrue(mockTicketDAO.updateTicket(mockTicketDAO.getTicket(TEST_VEHICLE_REG_NUMBER)));
            assertTrue(parkingSpotDAO.updateParking(parkingSpot));
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            dataBaseTestConfig.closeConnection(con);
        }
    }

    /**
     * Method tests ParkingService class for checking recurrent customers and returning correct boolean
     * @throws Exception
     */
    @Test
    public void testRecurrentCustomer() throws Exception {
        Connection con = null;
       try {
           con = dataBaseTestConfig.getConnection();
           when(inputReaderUtil.readSelection()).thenReturn(Integer.valueOf("1"));
           when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
           long timeIn = System.currentTimeMillis() - (60 * 60 * 1000);
           Date inTime = new Date(timeIn);
           ParkingService pSSpy = Mockito.spy(new ParkingService(inputReaderUtil, parkingSpotDAO, mockTicketDAO));
           doReturn(inTime).when(pSSpy).makeInTime();
           pSSpy.processIncomingVehicle();
           pSSpy.processExitingVehicle();
           pSSpy.processIncomingVehicle();
           assertTrue(pSSpy.recurrent);
    } catch (NullPointerException e) {
        e.printStackTrace();
    } finally {
        dataBaseTestConfig.closeConnection(con);
    }

    }

}
