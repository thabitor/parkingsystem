package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

public class ParkingService {

    private static final Logger logger = LogManager.getLogger("ParkingService");

    private FareCalculatorService fareCalculatorService = new FareCalculatorService();
    private TicketDAO ticketDAO;
    private InputReaderUtil inputReaderUtil;
    private ParkingSpotDAO parkingSpotDAO;
    public boolean recurrent;
    public Date inTime;
    public Date outTime;

    public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO){
        this.inputReaderUtil = inputReaderUtil;
        this.parkingSpotDAO = parkingSpotDAO;
        this.ticketDAO = ticketDAO;
    }


    public void processIncomingVehicle() {
        recurrent = false;
        try{
            ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
            if(parkingSpot !=null && parkingSpot.getId() > 0){
                String vehicleRegNumber = getVehichleRegNumber();
                parkingSpot.setAvailable(false);
                parkingSpotDAO.updateParking(parkingSpot);//allot this parking space and mark its availability as false
                this.inTime = makeInTime();
                Ticket ticket = new Ticket();
                //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
                //ticket.setId(ticketID);
                ticket.setParkingSpot(parkingSpot);
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(0);
                ticket.setInTime(inTime);
                ticket.setOutTime(null);
                checkVehicleHistory(vehicleRegNumber);
               if (recurrent) {
                   System.out.println("Welcome back! As a recurring user of our parking lot, you'll benefit from a 5% discount.");
               }
                ticketDAO.saveTicket(ticket);
                System.out.println("Generated Ticket and saved in DB");
                System.out.println("Please park your vehicle in spot number:"+parkingSpot.getId());
                System.out.println("Recorded in-time for vehicle number:"+vehicleRegNumber+" is:"+inTime);
            }
        }catch(Exception e){
            logger.error("Unable to process incoming vehicle",e);
        }

    }

    public Date makeInTime () {
        return new Date();
    }

    public Date makeOutTime () {
        return new Date();
    }

    private String getVehichleRegNumber() throws Exception {
        System.out.println("Please type the vehicle registration number and press enter key");
        return inputReaderUtil.readVehicleRegistrationNumber();
    }

    /**
     * This method checks if given vehicle registration number exists previously in the database, if true the customer is recurrent and provided 5% discount.
     * @param vehicleRegNumber as parameter
     * @return returns the value of 'recurrent' boolean true if registration number exists in the database and false if not
     */
    public boolean checkVehicleHistory (String vehicleRegNumber) {
        Connection con = null;
        recurrent = false;
        ticketDAO.getTicket(vehicleRegNumber);
        try {
            con = ticketDAO.dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.CHECK_HISTORY);
            ps.setString(1, vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                recurrent = true;
                PreparedStatement ps2 = con.prepareStatement(DBConstants.UPDATE_RECURRENT);
                ps2.setBoolean(1, true);
                ps2.setTimestamp(2, new Timestamp(inTime.getTime()));
                ps2.execute();
            }
        } catch (Exception ex) {
            logger.error("Error checking vehicle history", ex);
        } finally {
            ticketDAO.dataBaseConfig.closeConnection(con);
        } return recurrent;
    }

    public ParkingSpot getNextParkingNumberIfAvailable(){
        int parkingNumber;
        ParkingSpot parkingSpot = null;
        try{
            ParkingType parkingType = getVehichleType();
            parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
            if(parkingNumber > 0) {
                parkingSpot = new ParkingSpot(parkingNumber,parkingType, true);
            } else {
                throw new Exception("Error fetching parking number from DB. Parking slots might be full");
            }
        } catch(IllegalArgumentException ie) {
            logger.error("Error parsing user input for type of vehicle", ie);
        } catch(Exception e) {
            logger.error("Error fetching next available parking slot", e);
        }
        return parkingSpot;
    }

    private ParkingType getVehichleType(){
        System.out.println("Please select vehicle type from menu");
        System.out.println("1 CAR");
        System.out.println("2 BIKE");
        int input = inputReaderUtil.readSelection();
        switch(input){
            case 1: {
                return ParkingType.CAR;
            }
            case 2: {
                return ParkingType.BIKE;
            }
            default: {
                System.out.println("Incorrect input provided");
                throw new IllegalArgumentException("Entered input is invalid");
            }
        }
    }

    public void processExitingVehicle() {
        Ticket ticket;
        try {
            String vehicleRegNumber = getVehichleRegNumber();
            ticket = ticketDAO.getTicket(vehicleRegNumber);
            outTime = makeOutTime();
            ticket.setOutTime(outTime);
            FareCalculatorService.calculateFare(ticket);
            double getFare = ticket.getPrice();
            BigDecimal fareGetDecimal = new BigDecimal(getFare);
            fareGetDecimal = fareGetDecimal.setScale(2, BigDecimal.ROUND_DOWN);
            if (recurrent) {
                ticket.setPrice(getFare - (getFare * 0.05));
                BigDecimal fareGetDecimalDiscounted = new BigDecimal(getFare);
                fareGetDecimal = fareGetDecimalDiscounted.setScale(2, BigDecimal.ROUND_DOWN);
            }
            checkIfTicketHasBeenUpdated(ticket, outTime, fareGetDecimal);
        } catch(Exception e) {
                logger.error("Unable to process exiting vehicle", e);
            }
        }

    /**
     * Checks if parking ticket has been updated, then updates parking spot if true and tells user fare to pay
     * @param ticket
     * @param outTime
     * @param fareGetDecimal based on condition of recurrent customer in ProcessExitingVehicle() method
     */
    private void checkIfTicketHasBeenUpdated(Ticket ticket, Date outTime, BigDecimal fareGetDecimal) {
        try {
            if (ticketDAO.updateTicket(ticket)) {
                ParkingSpot parkingSpot = ticket.getParkingSpot();
                parkingSpot.setAvailable(true);
                parkingSpotDAO.updateParking(parkingSpot);
                    System.out.println("Please pay the parking fare:" + fareGetDecimal);
                    System.out.println("Recorded out-time for vehicle number:" + ticket.getVehicleRegNumber() + " is:" + outTime);
            } else {
                System.out.println("Unable to update ticket information. Error occurred");
            }
        } catch (Exception e) {
            logger.error("Unable to process exiting vehicle", e);
        }
    }
}


