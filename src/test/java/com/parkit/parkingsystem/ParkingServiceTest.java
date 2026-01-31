package com.parkit.parkingsystem;

        import com.parkit.parkingsystem.config.DataBaseConfig;
        import com.parkit.parkingsystem.constants.ParkingType;
        import com.parkit.parkingsystem.dao.ParkingSpotDAO;
        import com.parkit.parkingsystem.dao.TicketDAO;
        import com.parkit.parkingsystem.model.ParkingSpot;
        import com.parkit.parkingsystem.model.Ticket;
        import com.parkit.parkingsystem.service.FareCalculatorService;
        import com.parkit.parkingsystem.service.ParkingService;
        import com.parkit.parkingsystem.util.InputReaderUtil;
        import org.junit.jupiter.api.BeforeEach;
        import org.junit.jupiter.api.Test;
        import org.junit.jupiter.api.extension.ExtendWith;
        import org.mockito.ArgumentCaptor;
        import org.mockito.Mock;
        import org.mockito.Mockito;
        import org.mockito.junit.jupiter.MockitoExtension;
        import org.mockito.junit.jupiter.MockitoSettings;
        import org.mockito.quality.Strictness;

        import java.sql.SQLException;
        import java.util.Date;

        import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ParkingServiceTest {

    private static ParkingService parkingService;
    @Mock
    private static FareCalculatorService fareCalculatorService;
    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    public void setUpPerTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,true);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO,fareCalculatorService);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    void processExitingVehicleTest(){
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(2);
        parkingService.processExitingVehicle();
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(fareCalculatorService, times(1)).calculateFare(any(Ticket.class), eq(true));
    }

    @Test
    void processIncomingVehicleTest(){
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(2);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(2);

        parkingService.processIncomingVehicle();

        ArgumentCaptor<ParkingSpot> captor = ArgumentCaptor.forClass(ParkingSpot.class);
        verify(parkingSpotDAO).updateParking(captor.capture());
        assertFalse(captor.getValue().isAvailable());

        verify(ticketDAO).saveTicket(any(Ticket.class));
        verify(ticketDAO).getNbTicket("ABCDEF");
    }

    @Test
    void processExitingVehicleUnableUpdateTest(){
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(2);

        parkingService.processExitingVehicle();

        verify(fareCalculatorService, times(1)).calculateFare(any(Ticket.class), eq(true));
        verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
    }

    @Test
    void getNextParkingNumberIfAvailableTest() {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        assertEquals(1, parkingSpot.getId());
        assertTrue(parkingSpot.isAvailable());

        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
    }

    @Test
    void getNextParkingNumberIfAvailableParkingNumberNotFoundTest() {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(0);

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        assertNull(parkingSpot, "Parking spot should be null when no slot is available");
    }

    @Test
    void getNextParkingNumberIfAvailableParkingNumberWrongArgument(){
        when(inputReaderUtil.readSelection()).thenReturn(3);
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        assertNull(parkingSpot, "Error parsing user input for type of vehicle");
    }

    @Test
    void saveTicket_dbException_shouldReturnFalse() throws Exception {

        DataBaseConfig mockConfig = mock(DataBaseConfig.class);
        TicketDAO dao = new TicketDAO();
        dao.dataBaseConfig = mockConfig;

        when(mockConfig.getConnection()).thenThrow(new SQLException());

        Ticket ticket = new Ticket();

        boolean result = dao.saveTicket(ticket);

        assertFalse(result);
    }
}
