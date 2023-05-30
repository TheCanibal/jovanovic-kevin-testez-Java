package com.parkit.parkingsystem;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Date;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @AfterEach
    private void nextTest() {
        System.out.println("\nTEST SUIVANT\n");
    }

    @Test
    public void processExitingVehicleTest(){
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(2);
	
        parkingService.processExitingVehicle();
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        
        
        verify(ticketDAO).getNbTicket("ABCDEF");
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        assertThat(ticket.getParkingSpot().isAvailable()).isEqualTo(true);
        assertEquals(Math.round(ticket.getPrice()*1000.0)/1000.0, 1.425);

    }

    @Test
    public void testProcessIncomingVehicle(){
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(2);
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
	
        parkingService.processIncomingVehicle();
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        
        
        verify(ticketDAO).getNbTicket("ABCDEF");
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(parkingSpotDAO).getNextAvailableSlot(ParkingType.CAR);
        verify(inputReaderUtil).readSelection();
        assertThat(ticket.getParkingSpot().isAvailable()).isEqualTo(false);

    }

    @Test
    public void processExitingVehicleTestUnableUpdate(){
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
	
        parkingService.processExitingVehicle();
        
        verify(ticketDAO).updateTicket(any(Ticket.class));

    }

    @Test
    public void testGetNextParkingNumberIfAvailable(){
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(inputReaderUtil.readSelection()).thenReturn(1);
	
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        
        verify(parkingSpotDAO).getNextAvailableSlot(ParkingType.CAR);
        verify(inputReaderUtil).readSelection();
        assertThat(parkingSpot.getId()).isEqualTo(1);
        assertThat(parkingSpot.isAvailable()).isEqualTo(true);
    

    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound(){
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);
        when(inputReaderUtil.readSelection()).thenReturn(1);
	
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        
        verify(parkingSpotDAO).getNextAvailableSlot(any(ParkingType.class));
        verify(inputReaderUtil).readSelection();

        assertThat(parkingSpot).isEqualTo(null);

    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument(){
        //when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
        when(inputReaderUtil.readSelection()).thenReturn(3);
	
        //assertThrows(IllegalArgumentException.class, () -> parkingService.getNextParkingNumberIfAvailable());
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        
        //verify(parkingSpotDAO).getNextAvailableSlot(any(ParkingType.class));
        verify(inputReaderUtil).readSelection();

        assertThat(parkingSpot).isEqualTo(null);

    }



}
