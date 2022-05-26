package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class TicketDAO {

    private static final Logger logger = LogManager.getLogger("TicketDAO");

    public DataBaseConfig dataBaseConfig = new DataBaseConfig();


    public boolean saveTicket(Ticket ticket) {
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.SAVE_TICKET);
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            ps.setInt(1,ticket.getId());
            ps.setInt(2, ticket.getParkingSpot().getId());
            ps.setString(3, ticket.getVehicleRegNumber());
            ps.setDouble(4, ticket.getPrice());
            ps.setTimestamp(5, new Timestamp(ticket.getInTime().getTime()));
            ps.setTimestamp(6, (ticket.getOutTime() == null) ? null : (new Timestamp(ticket.getOutTime().getTime())));
         //   ps.setBoolean(6, checkVehicleHistory(ticket.getVehicleRegNumber()));
            return ps.execute();
        } catch (Exception ex) {
            logger.error("Error fetching next available slot", ex);
        } finally {
            dataBaseConfig.closeConnection(con);
        }
        return false;
    }

    public Ticket getTicket(String vehicleRegNumber) {
        Connection con = null;
        Ticket ticket = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.GET_TICKET);
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            ps.setString(1, vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            // TODO: handle multiple tickets
            if (rs.next()) {
                ticket = new Ticket();
                ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)), false);
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(rs.getInt(2));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setInTime(rs.getTimestamp(4));
                ticket.setOutTime(rs.getTimestamp(5));
                ticket.setPrice(rs.getDouble(3));
            }
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
        } catch (Exception ex) {
            logger.error("Error fetching next available slot", ex);
        } finally {
            dataBaseConfig.closeConnection(con);
        }
        return ticket;
    }

//    public boolean checkVehicleHistory(String vehicleRegNumber) {
//        Connection con = null;
//        recurrent = false;
//        // vehicleRegNumber = "";
//        getTicket(vehicleRegNumber);
//        try {
//            con = dataBaseConfig.getConnection();
//            PreparedStatement ps = con.prepareStatement(DBConstants.CHECK_HISTORY);
//            ps.setString(1, vehicleRegNumber);
//            ResultSet rs = ps.executeQuery();
//            if (rs.next()) {
//                recurrent = true;
////                PreparedStatement ps2 = con.prepareStatement(DBConstants.UPDATE_RECURRENT);
////                ps2.setBoolean(1, true);
////                ps2.setInt(2,getTicket(vehicleRegNumber).getId());
////                ps2.execute();
//            }
//        } catch (Exception ex) {
//            logger.error("Error checking vehicle history", ex);
//        } finally {
//            dataBaseConfig.closeConnection(con);
//        } return recurrent;
//    }


    public boolean updateTicket(Ticket ticket) {
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
            ps.setDouble(1, ticket.getPrice());
            ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
            ps.setInt(3,ticket.getId());
            ps.execute();
            return true;
        }catch (Exception ex){
            logger.error("Error saving ticket info",ex);
        }finally {
            dataBaseConfig.closeConnection(con);
        }
        return false;
    }
}
