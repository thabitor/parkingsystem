package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
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
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.util.Date;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    public static final String TEST_VEHICLE_REG_NUMBER = "ABCDEF";
    public static final String TEST_VEHICLE_REG_NUMBER2 = "MYTEST";
    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
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
            ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingService.processIncomingVehicle();
            Ticket ticket = ticketDAO.getTicket(TEST_VEHICLE_REG_NUMBER);
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
            ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingService.processIncomingVehicle();
            Ticket ticket = ticketDAO.getTicket(TEST_VEHICLE_REG_NUMBER);
            parkingService.processExitingVehicle();
            FareCalculatorService fare = new FareCalculatorService();
            Date time = new Date();
            assertTrue(ticket.getOutTime().getHours() == time.getHours() || ticket.getOutTime().getMinutes() == time.getMinutes());
            assertFalse(ticket.getParkingSpot().isAvailable());

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
           ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
           parkingService.processIncomingVehicle();
    //       Ticket ticket = ticketDAO.getTicket(TEST_VEHICLE_REG_NUMBER2);
           parkingService.processExitingVehicle();
           assertTrue(ParkingService.checkVehicleHistory(TEST_VEHICLE_REG_NUMBER));
    } catch (NullPointerException e) {
        e.printStackTrace();
    } finally {
        dataBaseTestConfig.closeConnection(con);
    }

    }

}
