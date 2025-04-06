package ca.mcgill.ecse321.boardgame.service;

import ca.mcgill.ecse321.boardgame.dto.EventRegistrationRequestDto;
import ca.mcgill.ecse321.boardgame.dto.EventRegistrationResponseDto;
import ca.mcgill.ecse321.boardgame.exception.BoardGameException;
import ca.mcgill.ecse321.boardgame.model.Event;
import ca.mcgill.ecse321.boardgame.model.EventRegistration;
import ca.mcgill.ecse321.boardgame.model.ParticipationStatus;
import ca.mcgill.ecse321.boardgame.model.UserAccount;
import ca.mcgill.ecse321.boardgame.repo.EventRegistrationRepository;
import ca.mcgill.ecse321.boardgame.repo.EventRepository;
import ca.mcgill.ecse321.boardgame.repo.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class EventRegistrationServiceTests {

    @Mock
    private EventRegistrationRepository eventRegistrationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private EventRegistrationService eventRegistrationService;

    private UserAccount user;
    private Event event;

    @BeforeEach
    public void setUp() {
        user = mock(UserAccount.class);
        event = mock(Event.class);

        lenient().when(user.getUserAccountID()).thenReturn(1L);
        lenient().when(user.getName()).thenReturn("Bob");

        lenient().when(event.getEventID()).thenReturn(1L);
        lenient().when(event.getEventName()).thenReturn("Test Event");
        lenient().when(event.getMaxParticipant()).thenReturn(10);
        lenient().when(event.getDate()).thenReturn(new java.sql.Date(System.currentTimeMillis() + 86400000L));
        lenient().when(event.getTime()).thenReturn(new java.sql.Time(System.currentTimeMillis() + 86400000L));
    }

    @Test
    public void testRegisterUserSuccessfully() {
        when(userAccountRepository.findUserAccountByUserAccountID(1L)).thenReturn(user);
        when(eventRepository.findEventByEventID(1L)).thenReturn(event);
        when(eventRegistrationRepository.findAllByEventRegistrationKeyEvent(event)).thenReturn(List.of());

        EventRegistrationRequestDto request = new EventRegistrationRequestDto(1L, 1L);
        EventRegistrationResponseDto response = eventRegistrationService.register(request);

        assertNotNull(response);
        assertEquals(1L, response.getParticipantId());
        assertEquals(1L, response.getEventId());
        assertEquals("Bob", user.getName());
        assertEquals("Test Event", event.getEventName());
        assertEquals(ParticipationStatus.PENDING, response.getStatus());
        verify(eventRegistrationRepository, times(1)).save(any(EventRegistration.class));
    }

    @Test
    public void testRegisterUser_NullParticipantIdOrEventId() {
        EventRegistrationRequestDto request = new EventRegistrationRequestDto(null, null);
        BoardGameException exception = assertThrows(BoardGameException.class,
                () -> eventRegistrationService.register(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Participant ID and Event ID are required.", exception.getMessage());
    }

    @Test
    public void testRegisterUser_UserNotFound() {
        when(userAccountRepository.findUserAccountByUserAccountID(anyLong())).thenReturn(null);

        EventRegistrationRequestDto request = new EventRegistrationRequestDto(1L, 1L);
        BoardGameException exception = assertThrows(BoardGameException.class,
                () -> eventRegistrationService.register(request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("User not found.", exception.getMessage());
    }

    @Test
    public void testRegisterUser_EventNotFound() {
        when(userAccountRepository.findUserAccountByUserAccountID(anyLong())).thenReturn(user);
        when(eventRepository.findEventByEventID(anyLong())).thenReturn(null);

        EventRegistrationRequestDto request = new EventRegistrationRequestDto(1L, 1L);
        BoardGameException exception = assertThrows(BoardGameException.class,
                () -> eventRegistrationService.register(request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Event not found.", exception.getMessage());
    }

    @Test
    public void testRegisterUser_EventFull() {
        when(userAccountRepository.findUserAccountByUserAccountID(anyLong())).thenReturn(user);
        when(eventRepository.findEventByEventID(anyLong())).thenReturn(event);
        EventRegistration registration = mock(EventRegistration.class);
        when(eventRegistrationRepository.findAllByEventRegistrationKeyEvent(event))
                .thenReturn(List.of(registration, registration, registration, registration, registration,
                        registration, registration, registration, registration, registration));

        EventRegistrationRequestDto request = new EventRegistrationRequestDto(1L, 1L);
        BoardGameException exception = assertThrows(BoardGameException.class,
                () -> eventRegistrationService.register(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Event is already full.", exception.getMessage());
    }

    @Test
    public void testRegisterUser_EventAlreadyStarted() {
        when(event.getDate()).thenReturn(new java.sql.Date(System.currentTimeMillis() - 86400000L)); // 1 day ago
        when(event.getTime()).thenReturn(new java.sql.Time(System.currentTimeMillis() - 86400000L)); // 1 day ago

        when(userAccountRepository.findUserAccountByUserAccountID(anyLong())).thenReturn(user);
        when(eventRepository.findEventByEventID(anyLong())).thenReturn(event);

        EventRegistrationRequestDto request = new EventRegistrationRequestDto(1L, 1L);
        BoardGameException exception = assertThrows(BoardGameException.class,
                () -> eventRegistrationService.register(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Cannot register for an event that has already started.", exception.getMessage());
    }

    @Test
    public void testRegisterUser_AlreadyRegistered() {
        when(userAccountRepository.findUserAccountByUserAccountID(anyLong())).thenReturn(user);
        when(eventRepository.findEventByEventID(anyLong())).thenReturn(event);
        EventRegistration registration = new EventRegistration(
                new EventRegistration.EventRegistrationKey(user, event),
                ParticipationStatus.PENDING);
        when(eventRegistrationRepository.findByEventRegistrationKey(any())).thenReturn(registration);

        EventRegistrationRequestDto request = new EventRegistrationRequestDto(1L, 1L);
        BoardGameException exception = assertThrows(BoardGameException.class,
                () -> eventRegistrationService.register(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("User is already registered for this event.", exception.getMessage());
    }

    @Test
    public void testGetRegistrationSuccessfully() {
        EventRegistration registration = new EventRegistration(new EventRegistration.EventRegistrationKey(user, event), ParticipationStatus.PENDING);

        when(userAccountRepository.findUserAccountByUserAccountID(anyLong())).thenReturn(user);
        when(eventRepository.findEventByEventID(anyLong())).thenReturn(event);
        when(eventRegistrationRepository.findByEventRegistrationKey(any())).thenReturn(registration);

        EventRegistrationResponseDto response = eventRegistrationService.getRegistration(1L, 1L);

        assertNotNull(response);
        assertEquals(1L, response.getParticipantId());
        assertEquals(1L, response.getEventId());
        assertEquals(ParticipationStatus.PENDING, response.getStatus());
        assertEquals("Bob", user.getName());
        assertEquals("Test Event", event.getEventName());
    }

    @Test
    public void testGetRegistration_UserNotFound() {
        when(userAccountRepository.findUserAccountByUserAccountID(anyLong())).thenReturn(null);

        BoardGameException exception = assertThrows(BoardGameException.class,
                () -> eventRegistrationService.getRegistration(1L, 1L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("User not found.", exception.getMessage());
    }

    @Test
    public void testGetRegistration_EventNotFound() {
        when(userAccountRepository.findUserAccountByUserAccountID(anyLong())).thenReturn(user);
        when(eventRepository.findEventByEventID(anyLong())).thenReturn(null);

        BoardGameException exception = assertThrows(BoardGameException.class,
                () -> eventRegistrationService.getRegistration(1L, 1L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Event not found.", exception.getMessage());
    }

    @Test
    public void testGetRegistration_RegistrationNotFound() {
        when(userAccountRepository.findUserAccountByUserAccountID(anyLong())).thenReturn(user);
        when(eventRepository.findEventByEventID(anyLong())).thenReturn(event);
        when(eventRegistrationRepository.findByEventRegistrationKey(any())).thenReturn(null);

        BoardGameException exception = assertThrows(BoardGameException.class,
                () -> eventRegistrationService.getRegistration(1L, 1L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Registration not found.", exception.getMessage());
    }

    @Test
    public void testGetAllRegistrationsByUserSuccessfully() {
        EventRegistration registration = new EventRegistration(
                new EventRegistration.EventRegistrationKey(user, event),
                ParticipationStatus.PENDING);

        when(userAccountRepository.findUserAccountByUserAccountID(anyLong())).thenReturn(user);
        when(eventRegistrationRepository.findAllByEventRegistrationKeyRegistrant(user))
                .thenReturn(List.of(registration));

        List<EventRegistrationResponseDto> responseList = eventRegistrationService.getAllRegistrationsByUser(1L);
        assertNotNull(responseList);
        assertFalse(responseList.isEmpty());
        assertEquals(1, responseList.size());
        EventRegistrationResponseDto response = responseList.get(0);
        assertEquals(1L, response.getParticipantId());
        assertEquals(1L, response.getEventId());
        assertEquals("Bob", user.getName());
        assertEquals("Test Event", event.getEventName());
        assertEquals(ParticipationStatus.PENDING, response.getStatus());
    }

    @Test
    public void testGetAllRegistrationsByUser_UserNotFound() {
        when(userAccountRepository.findUserAccountByUserAccountID(anyLong())).thenReturn(null);

        BoardGameException exception = assertThrows(BoardGameException.class,
                () -> eventRegistrationService.getAllRegistrationsByUser(1L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("User not found.", exception.getMessage());
    }

    @Test
    public void testGetAllRegistrationsByEventSuccessfully() {
        EventRegistration registration = new EventRegistration(
                new EventRegistration.EventRegistrationKey(user, event),
                ParticipationStatus.PENDING);

        when(eventRepository.findEventByEventID(anyLong())).thenReturn(event);
        when(eventRegistrationRepository.findAllByEventRegistrationKeyEvent(event)).thenReturn(List.of(registration));

        List<EventRegistrationResponseDto> responseList = eventRegistrationService.getAllRegistrationsByEvent(1L);
        assertNotNull(responseList);
        assertFalse(responseList.isEmpty());
        assertEquals(1, responseList.size());
        EventRegistrationResponseDto response = responseList.get(0);
        assertEquals(1L, response.getParticipantId());
        assertEquals(1L, response.getEventId());
        assertEquals("Bob", user.getName());
        assertEquals("Test Event", event.getEventName());
        assertEquals(ParticipationStatus.PENDING, response.getStatus());
    }

    @Test
    public void testGetAllRegistrationsByEvent_EventNotFound() {
        when(eventRepository.findEventByEventID(anyLong())).thenReturn(null);

        BoardGameException exception = assertThrows(BoardGameException.class,
                () -> eventRegistrationService.getAllRegistrationsByEvent(1L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Event not found.", exception.getMessage());
    }

    @Test
    public void testGetAllRegistrationsSuccessfully() {
        EventRegistration registration = new EventRegistration(new EventRegistration.EventRegistrationKey(user, event),
                ParticipationStatus.PENDING);

        when(eventRegistrationRepository.findAll()).thenReturn(List.of(registration));

        List<EventRegistrationResponseDto> responseList = eventRegistrationService.getAllRegistrations();

        assertNotNull(responseList);
        assertEquals(1, responseList.size());
        EventRegistrationResponseDto response = responseList.get(0);
        assertEquals(1L, response.getParticipantId());
        assertEquals(1L, response.getEventId());
        assertEquals("Bob", user.getName());
        assertEquals("Test Event", event.getEventName());
        assertEquals(ParticipationStatus.PENDING, response.getStatus());
    }

    @Test
    public void testCancelRegistrationSuccessfully() {
        EventRegistration registration = new EventRegistration(new EventRegistration.EventRegistrationKey(user, event),
                ParticipationStatus.PENDING);
        when(userAccountRepository.findUserAccountByUserAccountID(anyLong())).thenReturn(user);
        when(eventRepository.findEventByEventID(anyLong())).thenReturn(event);
        when(eventRegistrationRepository.findByEventRegistrationKey(any())).thenReturn(registration);

        assertDoesNotThrow(() -> eventRegistrationService.cancelRegistration(1L, 1L));
        verify(eventRegistrationRepository, times(1)).delete(registration);
    }

    @Test
    public void testCancelRegistration_UserNotFound() {
        when(userAccountRepository.findUserAccountByUserAccountID(anyLong())).thenReturn(null);

        BoardGameException exception = assertThrows(BoardGameException.class,
                () -> eventRegistrationService.cancelRegistration(1L, 1L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("User not found.", exception.getMessage());
    }

    @Test
    public void testCancelRegistration_EventNotFound() {
        when(userAccountRepository.findUserAccountByUserAccountID(anyLong())).thenReturn(user);
        when(eventRepository.findEventByEventID(anyLong())).thenReturn(null);

        BoardGameException exception = assertThrows(BoardGameException.class,
                () -> eventRegistrationService.cancelRegistration(1L, 1L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Event not found.", exception.getMessage());
    }

    @Test
    public void testCancelRegistration_RegistrationNotFound() {
        when(userAccountRepository.findUserAccountByUserAccountID(anyLong())).thenReturn(user);
        when(eventRepository.findEventByEventID(anyLong())).thenReturn(event);
        when(eventRegistrationRepository.findByEventRegistrationKey(any())).thenReturn(null);

        BoardGameException exception = assertThrows(BoardGameException.class,
                () -> eventRegistrationService.cancelRegistration(1L, 1L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Registration not found.", exception.getMessage());
    }

    @Test
    public void testCancelRegistration_EventAlreadyStarted() {
        Event event = mock(Event.class);
        UserAccount user = mock(UserAccount.class);
        EventRegistration registration = new EventRegistration(new EventRegistration.EventRegistrationKey(user, event),
                ParticipationStatus.PENDING);

        when(userAccountRepository.findUserAccountByUserAccountID(anyLong())).thenReturn(user);
        when(eventRepository.findEventByEventID(anyLong())).thenReturn(event);
        when(eventRegistrationRepository.findByEventRegistrationKey(any())).thenReturn(registration);

        when(event.getDate()).thenReturn(new java.sql.Date(System.currentTimeMillis() - 86400000L)); // Event is in the
                                                                                                     // past
        when(event.getTime()).thenReturn(new java.sql.Time(System.currentTimeMillis() - 86400000L));

        BoardGameException exception = assertThrows(BoardGameException.class,
                () -> eventRegistrationService.cancelRegistration(1L, 1L));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Cannot cancel registration for an event that has already started.", exception.getMessage());
    }
}
