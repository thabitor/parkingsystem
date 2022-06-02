package com.parkit.parkingsystem.service;
import java.time.Duration;
import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;


import java.util.Date;
import java.util.Objects;

import static java.lang.Math.abs;

public class FareCalculatorService {


    public static void calculateFare(Ticket ticket) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        int monthIn = ticket.getInTime().getMonth();
        int dateIn = ticket.getInTime().getDate();
        double inHour = ticket.getInTime().getHours();
        // double inMinute = ticket.getInTime().getMinutes();

        int monthOut = ticket.getOutTime().getMonth();
        int dateOut = ticket.getOutTime().getDate();
        double outHour = ticket.getOutTime().getHours();
        // double outMinute = ticket.getOutTime().getMinutes();

        double timeIn = ticket.getInTime().getTime();
        double timeOut = ticket.getOutTime().getTime();
        double timeDiff = timeOut - timeIn;
        double timeDiffMinutes = (timeDiff / (60 * 1000)) % 60;
        double timeDiffHrs = (timeDiff / (60 * 60 * 1000)) % 24;


        //TODO: Some tests are failing here. Need to check if this logic is correct

        double duration;

        if ((dateOut - dateIn == 1) || (dateIn - dateOut > 1
                && (monthOut - monthIn == 1 || monthOut - monthIn == -11))) {
            duration = 24.0;
        }else if (timeDiffHrs >= 22.0 && timeDiffHrs <= 23.0) {
            duration = 1.0;
        }else if ((timeDiffHrs >= 1.0 && timeDiffMinutes <= 5.0)) {
            duration = 1.0;
        } else if (timeDiffMinutes <= 45.0 && timeDiffMinutes > 30.0) {
            duration = 0.75;
        } else if (timeDiffMinutes <= 30.0) {
            duration = 0.0;
        } else {
            duration = timeOut - timeIn;
        }



        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown Parking Type");
        }
    }
}