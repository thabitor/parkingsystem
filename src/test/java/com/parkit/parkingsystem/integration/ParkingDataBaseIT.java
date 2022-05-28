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

    //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
    //TODO: check that a ticket is actually saved in DB and Parking table is updated with availability

    @Test
    public void testParkingLotExit() throws Exception {
        //TODO: check that the fare generated and out time are populated correctly in the database
        Connection con = null;
        try {
            con = dataBaseTestConfig.getConnection();
            Date outTime = new Date();
            double fare;
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            ParkingService exitSpy = Mockito.spy(new ParkingService(inputReaderUtil, parkingSpotDAO, mockTicketDAO));
            doReturn(outTime).when(exitSpy).makeOutTime();
            exitSpy.processIncomingVehicle();
            exitSpy.processExitingVehicle();
            assertTrue(mockTicketDAO.updateTicket(mockTicketDAO.getTicket(TEST_VEHICLE_REG_NUMBER)));
            assertFalse(parkingSpotDAO.updateParking(parkingSpot));


        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            dataBaseTestConfig.closeConnection(con);
        }
    }

    @Test
    public void testRecurrentCustomer() throws Exception {
        Connection con = null;
       try {
           con = dataBaseTestConfig.getConnection();
           when(inputReaderUtil.readSelection()).thenReturn(Integer.valueOf("1"));
           when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
           ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, mockTicketDAO);
           parkingService.processIncomingVehicle();
           parkingService.processExitingVehicle();
           parkingService.processIncomingVehicle();
           assertTrue(parkingService.recurrent);
    } catch (NullPointerException e) {
        e.printStackTrace();
    } finally {
        dataBaseTestConfig.closeConnection(con);
    }

    }

}
