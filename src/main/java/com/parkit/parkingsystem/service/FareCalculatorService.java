package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import static java.lang.Math.abs;

public class FareCalculatorService {



    public void calculateFare(Ticket ticket) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        double inHour = ticket.getInTime().getHours();
        double outHour = ticket.getOutTime().getHours();
        double inMinute = ticket.getInTime().getMinutes();
        double outMinute = ticket.getOutTime().getMinutes();
        int dateIn = ticket.getInTime().getDate();
        int dateOut = ticket.getOutTime().getDate();
        int monthIn = ticket.getInTime().getMonth();
        int monthOut = ticket.getOutTime().getMonth();


        //TODO: Some tests are failing here. Need to check if this logic is correct

        double duration;

        if ((dateOut - dateIn == 1) || (dateIn - dateOut > 1 && (monthOut - monthIn == 1 || monthOut - monthIn == -11))) {
            duration = 24.0;
        } else if ((abs(outHour - inHour) <= 1.0 || abs(outHour - inHour) == 23.0) && abs(abs(outMinute - inMinute) - 60) > 30.0) {
            duration = abs((abs(inMinute - outMinute) - 60.0) / 60.0);
        } else if ((outMinute - inMinute) <= 30.0) {
            duration = 0.0;
        } else duration = outHour - inHour;


        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default: throw new IllegalArgumentException("Unknown Parking Type");
        }
    }
}