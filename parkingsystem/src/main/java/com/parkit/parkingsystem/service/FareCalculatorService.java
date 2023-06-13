package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    /**
     *  method that calculate ticket price when a vehicule exit parking
     *
     *  @param ticket ticket that we have to calculate price
     *  @param discount if regular user, will have a 5% discount on price
     */

    public void calculateFare(Ticket ticket, boolean discount) {
	if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
	    throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
	}

	long inHour = ticket.getInTime().getTime();
	long outHour = ticket.getOutTime().getTime();

	// TODO: Some tests are failing here. Need to check if this logic is correct
    //Problem solved with getTime() method and a double variable (not float, it is bugged)
	double duration = Math.round(((outHour - inHour) / 3600000.0) * 1000.0) / 1000.0;

    //free if vehicle exit before 30 minutes parking
	if (duration < 0.5) {
	    ticket.setPrice(0);
	} else {

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
		throw new IllegalArgumentException("Unkown Parking Type");
	    }

	}

    //5% discount if regular user
	if (discount == true) {
	    ticket.setPrice(ticket.getPrice() * 95 / 100);
	}
    }

    /**
     *  method that calculate price without discount
     *
     *  @param ticket ticket that we have to calculate price
     */

    public void calculateFare(Ticket ticket) {
	calculateFare(ticket, false);
    }
}