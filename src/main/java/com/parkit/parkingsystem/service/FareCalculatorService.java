package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount){

        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }
        double reduction = 1;
        int faskPark = 1;
        long inMillis = ticket.getInTime().getTime();
        long outMillis = ticket.getOutTime().getTime();

        //TODO: Some tests are failing here. Need to check if this logic is correct
        double durationInHours = (outMillis - inMillis)/(1000.0*60*60);

        if (durationInHours<0.5){
            faskPark = 0;}
        else{
            faskPark = 1;
        }

        if (discount){
           reduction = 0.95;}
        else{
            reduction = 1;
        }

        double rawPriceFareCar =
                ((durationInHours * Fare.CAR_RATE_PER_HOUR)*reduction)*faskPark;

        double roundedPriceFareCar =
                Math.ceil(rawPriceFareCar * 100.00)/100.00;

        double rawPriceFareBike =
                ((durationInHours * Fare.BIKE_RATE_PER_HOUR)*reduction)*faskPark;

        double roundedPriceFareBike =
                Math.ceil(rawPriceFareBike * 100.00)/100.00;

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice(roundedPriceFareCar);
                break;
            }
            case BIKE: {
                ticket.setPrice(roundedPriceFareBike);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
}