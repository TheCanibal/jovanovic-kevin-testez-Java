package com.parkit.parkingsystem.integration;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private Date outTime;
    private Date inTime;
    


    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
        outTime = new Date();
        inTime = new Date(System.currentTimeMillis() - 60*60*1000);
        
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle(inTime);

        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability
        assertThat(ticket).isNotNull();
        assertThat(ticket.getParkingSpot().isAvailable()).isFalse();
        assertThat(parkingSpotDAO.updateParking(ticketDAO.getTicket("ABCDEF").getParkingSpot())).isTrue();

    }

    @Test
    public void testParkingLotExit(){
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle(outTime);

        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        //TODO: check that the fare generated and out time are populated correctly in the database

        assertThat(ticket.getOutTime()).isNotNull();
        //L'heure d'entrée et de sortie sont quasi les mêmes car on a pas initialisé de délai donc le prix est de 0
        assertThat(ticket.getPrice()).isEqualTo(1.5);
    }

    @Test
    public void testParkingLotExitRecurringUser(){
        
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        for(int i=0; i < 2; i++) {
            parkingService.processIncomingVehicle(inTime);
            parkingService.processExitingVehicle();
            System.out.println("ICI C'EST LE TEST RECURRING USER!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }

        
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        //TODO: check that the fare generated and out time are populated correctly in the database
        assertThat(ticket.getOutTime()).isNotNull();
        //L'heure d'entrée et de sortie sont quasi les mêmes car on a pas initialisé de délai donc le prix est de 0
        assertThat(ticket.getPrice()).isEqualTo(1.425);
    }

}
