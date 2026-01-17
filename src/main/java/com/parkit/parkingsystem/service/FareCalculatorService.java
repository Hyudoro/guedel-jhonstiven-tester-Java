package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount){

        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime());
        }

        double price;
        double ratePerHour;
        final double FREE_PARKING_HOURS= 0.5;
        final double DISCOUNT_RATE = 0.95;

        long inMillis = ticket.getInTime().getTime();
        long outMillis = ticket.getOutTime().getTime();
        final double MS_TO_HOURS = 1.0/(1000 * 60 * 60);
        double durationInHours = (outMillis - inMillis)* MS_TO_HOURS;

        if (durationInHours<FREE_PARKING_HOURS) { // if the parking time < 30 minutes then its bill's free.
            durationInHours = 0;
        }

        switch(ticket.getParkingSpot().getParkingType()){
            case CAR: ratePerHour = Fare.CAR_RATE_PER_HOUR; break;
            case BIKE: ratePerHour = Fare.BIKE_RATE_PER_HOUR; break;
            default: throw new IllegalArgumentException("Unknown Parking Type");
        }

        price = durationInHours * ratePerHour;
        if(discount) price *= DISCOUNT_RATE;
        price = Math.ceil(price*100)/100;
        ticket.setPrice(price);

    }
}