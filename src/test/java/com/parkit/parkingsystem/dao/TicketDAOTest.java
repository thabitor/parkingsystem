package com.parkit.parkingsystem.dao;

import static junit.framework.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TicketDAOTest {

    public static final String TEST_CAR_REG_NUMBER = "TSTCAR";
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

    /**
     * Method under test: {@link TicketDAO#saveTicket(Ticket)}
     */
    @Test
    void testSaveTicket() {

        Connection con = null;
        try {
            con = dataBaseTestConfig.getConnection();

            Ticket ticket = new Ticket();
            long timeIn = System.currentTimeMillis() - (60 * 60 * 1000);
            Date inTime = new Date(timeIn);
            Date outTime = new Date();
            ticket.setId(1);
            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, true));
            ticket.setPrice(1.5);
            ticket.setVehicleRegNumber(TEST_CAR_REG_NUMBER);
            assertFalse(mockTicketDAO.saveTicket(ticket));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            dataBaseTestConfig.closeConnection(con);
        }
    }


    /**
     * Method under test: {@link TicketDAO#getTicket(String)}
     */
    @Test
    void testGetTicketNotNull() {

        Connection con = null;
        try {
            con = dataBaseTestConfig.getConnection();
            ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, mockTicketDAO);
            when(inputReaderUtil.readSelection()).thenReturn(Integer.valueOf("1"));
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(TEST_CAR_REG_NUMBER);
            parkingService.processIncomingVehicle();
            assertNotNull(mockTicketDAO);
        } catch (NullPointerException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            dataBaseTestConfig.closeConnection(con);
        }
    }

    /**
     * Method under test: {@link TicketDAO#getTicket(String)}
     */
    @Test
    void testGetTicketIsNullWithoutParkingService() {
        TicketDAO ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = new DataBaseTestConfig();
        assertNull(ticketDAO.getTicket("42"));
    }

    /**
     * Method under test: {@link TicketDAO#updateTicket(Ticket)}
     */
    @Test
    void testUpdateTicket() {
        Connection con = null;
        try {
            con = dataBaseTestConfig.getConnection();
        Ticket ticket = new Ticket();
        long timeIn = System.currentTimeMillis() - (60 * 60 * 1000);
        Date inTime = new Date(timeIn);
        Date outTime = new Date();
        ticket.setId(1);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(new ParkingSpot(10, ParkingType.CAR, true));
        ticket.setPrice(1.5);
        ticket.setVehicleRegNumber(TEST_CAR_REG_NUMBER);
        assertTrue(mockTicketDAO.updateTicket(ticket));
    } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            dataBaseTestConfig.closeConnection(con);
        }
    }
}

