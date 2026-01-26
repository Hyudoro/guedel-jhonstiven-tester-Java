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
        long inMillis = ticket.getInTime().getTime();
        long outMillis = ticket.getOutTime().getTime();
        double durationInHours = (outMillis - inMillis) / 3_600_000.0;

       if (durationInHours<0.5) { // if the parking time < 30 minutes then its bill's free.
           ticket.setPrice(0);
       }
       else
       {
           switch(ticket.getParkingSpot().getParkingType()){
               case CAR: ratePerHour = Fare.CAR_RATE_PER_HOUR; break;
               case BIKE: ratePerHour = Fare.BIKE_RATE_PER_HOUR; break;
               default: throw new IllegalArgumentException("Unknown Parking Type");

           }
           price = durationInHours * ratePerHour;
           if(discount) {
               System.out.println("Heureux de vous revoir ! En tant qu’utilisateur régulier de notre parking, vous allez obtenir une remise de 5%");
               price *= 0.95; // discount
           }
           price = Math.ceil(price*100)/100; // round the number. ex : 0.6494 -> 0.65.
           ticket.setPrice(price);
       }

    }
    public void calculateFare(Ticket ticket){
        calculateFare(ticket,false);
    }
}