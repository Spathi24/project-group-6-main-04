package ca.mcgill.ecse321.boardgame.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ca.mcgill.ecse321.boardgame.dto.ErrorDto;
import ca.mcgill.ecse321.boardgame.dto.EventRegistrationRequestDto;
import ca.mcgill.ecse321.boardgame.dto.EventRegistrationResponseDto;
import ca.mcgill.ecse321.boardgame.model.*;
import ca.mcgill.ecse321.boardgame.repo.EventRegistrationRepository;
import ca.mcgill.ecse321.boardgame.repo.EventRepository;
import ca.mcgill.ecse321.boardgame.repo.GameRepository;
import ca.mcgill.ecse321.boardgame.repo.UserAccountRepository;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDateTime;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EventRegistrationIntegrationTests {

    @Autowired
    private TestRestTemplate client;

    @Autowired
    private UserAccountRepository userAccountRepo;

    @Autowired
    private EventRepository eventRepo;

    @Autowired
    private GameRepository gameRepo;

    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    private static final String BASE_URL = "/api/eventregistrations";

    private long userAccountId;
    private long eventId;

    @BeforeAll
    public void setup() {
        UserAccount user = new UserAccount("testuser", "password", "testuser@mail.com", AccountType.GAMEOWNER);
        user = userAccountRepo.save(user);
        userAccountId = user.getUserAccountID();

        Game game = new Game("Monopoly", "Classical", "Board Game");
        game = gameRepo.save(game);

        Event event = new Event(Date.valueOf(LocalDateTime.now().plusDays(1).toLocalDate()),
                Time.valueOf(LocalDateTime.now().plusHours(1).toLocalTime()),
                "Test Location", "A fun board game event", 10, game, user, "Test Event");
        event = eventRepo.save(event);
        eventId = event.getEventID();
    }

    @AfterAll
    public void clean() {
        eventRegistrationRepository.deleteAll();
        eventRepo.deleteAll();
        gameRepo.deleteAll();
        userAccountRepo.deleteAll();
    }

    @Test
    @Order(1)
    public void testRegisterEvent() {
        EventRegistrationRequestDto request = new EventRegistrationRequestDto(userAccountId, eventId);
        ResponseEntity<EventRegistrationResponseDto> response = client.postForEntity(BASE_URL, request,
                EventRegistrationResponseDto.class);

        EventRegistrationResponseDto createdRegistration = response.getBody();

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(createdRegistration);
        assertEquals(userAccountId, createdRegistration.getParticipantId());
        assertEquals(eventId, createdRegistration.getEventId());
        assertEquals(ParticipationStatus.PENDING, createdRegistration.getStatus());
    }

    @Test
    @Order(2)
    public void testRegisterEvent_NullParticipantIdOrEventId() {
        EventRegistrationRequestDto request1 = new EventRegistrationRequestDto(null, eventId);
        EventRegistrationRequestDto request2 = new EventRegistrationRequestDto(userAccountId, null);

        ResponseEntity<ErrorDto> response1 = client.postForEntity(BASE_URL, request1, ErrorDto.class);
        ResponseEntity<ErrorDto> response2 = client.postForEntity(BASE_URL, request2, ErrorDto.class);

        assertEquals(HttpStatus.BAD_REQUEST, response1.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode());
    }

    @Test
    @Order(3)
    public void testRegisterEvent_UserNotFound() {
        EventRegistrationRequestDto request = new EventRegistrationRequestDto(9999L, eventId);
        ResponseEntity<ErrorDto> response = client.postForEntity(BASE_URL, request, ErrorDto.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(4)
    public void testRegisterEvent_EventNotFound() {
        EventRegistrationRequestDto request = new EventRegistrationRequestDto(userAccountId, 9999L);
        ResponseEntity<ErrorDto> response = client.postForEntity(BASE_URL, request, ErrorDto.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(5)
    public void testRegisterEvent_EventFull() {
        Game game = new Game("Chess", "Strategy", "Board Game");
        game = gameRepo.save(game);

        UserAccount user = new UserAccount("anotheruser", "password", "anotheruser@mail.com", AccountType.GAMEOWNER);
        user = userAccountRepo.save(user);

        Event fullEvent = new Event(Date.valueOf(LocalDateTime.now().plusDays(1).toLocalDate()),
                Time.valueOf(LocalDateTime.now().plusHours(2).toLocalTime()),
                "Full Event Location", "An event at max capacity", 0, game, user, "Full Event");
        fullEvent = eventRepo.save(fullEvent);

        EventRegistrationRequestDto request = new EventRegistrationRequestDto(userAccountId, fullEvent.getEventID());
        ResponseEntity<ErrorDto> response = client.postForEntity(BASE_URL, request, ErrorDto.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(6)
    public void testRegisterEvent_EventAlreadyStarted() {
        Game game = new Game("Checkers", "Strategy", "Board Game");
        game = gameRepo.save(game);

        UserAccount user = new UserAccount("anotheruser2", "password", "anotheruser2@mail.com", AccountType.GAMEOWNER);
        user = userAccountRepo.save(user);

        Event pastEvent = new Event(Date.valueOf(LocalDateTime.now().minusDays(1).toLocalDate()),
                Time.valueOf(LocalDateTime.now().minusHours(1).toLocalTime()),
                "Past Location", "An event that already occurred", 10, game, user, "Past Event");
        pastEvent = eventRepo.save(pastEvent);

        EventRegistrationRequestDto request = new EventRegistrationRequestDto(userAccountId, pastEvent.getEventID());
        ResponseEntity<ErrorDto> response = client.postForEntity(BASE_URL, request, ErrorDto.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(7)
    public void testRegisterEvent_AlreadyRegistered() {
        EventRegistrationRequestDto request = new EventRegistrationRequestDto(userAccountId, eventId);
        client.postForEntity(BASE_URL, request, EventRegistrationResponseDto.class);

        ResponseEntity<ErrorDto> response = client.postForEntity(BASE_URL, request, ErrorDto.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(8)
    public void testGetEventRegistration() {
        String url = BASE_URL + "/" + userAccountId + "/" + eventId;
        ResponseEntity<EventRegistrationResponseDto> response = client.getForEntity(url,
                EventRegistrationResponseDto.class);
        EventRegistrationResponseDto registration = response.getBody();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(registration);
        assertEquals(userAccountId, registration.getParticipantId());
        assertEquals(eventId, registration.getEventId());
        assertEquals("PENDING", registration.getStatus().toString());
    }

    @Test
    @Order(9)
    public void testGetEventRegistration_UserNotFound() {
        String url = BASE_URL + "/9999/" + eventId;
        ResponseEntity<ErrorDto> response = client.getForEntity(url, ErrorDto.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(10)
    public void testGetEventRegistration_EventNotFound() {
        String url = BASE_URL + "/" + userAccountId + "/9999";
        ResponseEntity<ErrorDto> response = client.getForEntity(url, ErrorDto.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(11)
    public void testGetEventRegistration_RegistrationNotFound() {
        String url = BASE_URL + "/9999/9999";
        ResponseEntity<ErrorDto> response = client.getForEntity(url, ErrorDto.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(12)
    public void testGetAllRegistrationsForUser() {
        String url = BASE_URL + "/user/" + userAccountId;

        ResponseEntity<EventRegistrationResponseDto[]> response = client.getForEntity(url,
                EventRegistrationResponseDto[].class);
        EventRegistrationResponseDto[] registrations = response.getBody();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(registrations);
        assertTrue(registrations.length > 0);
        assertEquals(userAccountId, registrations[0].getParticipantId());
    }

    @Test
    @Order(13)
    public void testGetAllRegistrationsForUser_UserNotFound() {
        String url = BASE_URL + "/user/9999";
        ResponseEntity<ErrorDto> response = client.getForEntity(url, ErrorDto.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(14)
    public void testGetAllRegistrationsForEvent() {
        String url = BASE_URL + "/event/" + eventId;
        ResponseEntity<EventRegistrationResponseDto[]> response = client.getForEntity(url,
                EventRegistrationResponseDto[].class);
        EventRegistrationResponseDto[] registrations = response.getBody();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(registrations);
        assertTrue(registrations.length > 0);
        assertEquals(eventId, registrations[0].getEventId());
    }

    @Test
    @Order(15)
    public void testGetAllRegistrationsForEvent_EventNotFound() {
        String url = BASE_URL + "/event/9999";
        ResponseEntity<ErrorDto> response = client.getForEntity(url, ErrorDto.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(16)
    public void testGetAllRegistrations() {
        ResponseEntity<EventRegistrationResponseDto[]> response = client.getForEntity(BASE_URL,
                EventRegistrationResponseDto[].class);
        EventRegistrationResponseDto[] registrations = response.getBody();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(registrations);
        assertTrue(registrations.length > 0);
    }

    @Test
    @Order(17)
    public void testCancelEventRegistration() {
        String url = BASE_URL + "/" + userAccountId + "/" + eventId;
        client.delete(url);
        ResponseEntity<ErrorDto> response = client.getForEntity(url, ErrorDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(18)
    public void testCancelEventRegistration_UserNotFound() {
        String url = BASE_URL + "/9999/" + eventId;
        ResponseEntity<ErrorDto> response = client.exchange(url, HttpMethod.DELETE, null, ErrorDto.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(19)
    public void testCancelEventRegistration_EventNotFound() {
        String url = BASE_URL + "/" + userAccountId + "/9999";
        ResponseEntity<ErrorDto> response = client.exchange(url, HttpMethod.DELETE, null, ErrorDto.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(20)
    public void testCancelEventRegistration_RegistrationNotFound() {
        String url = BASE_URL + "/9999/9999";
        ResponseEntity<ErrorDto> response = client.exchange(url, HttpMethod.DELETE, null, ErrorDto.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @SuppressWarnings("null")
    @Test
    @Order(21)
    public void testCancelEventRegistration_EventAlreadyStarted() {
        Game game = new Game("Scrabble", "Word", "Board Game");
        game = gameRepo.save(game);

        UserAccount user = new UserAccount("anotheruser3", "password", "anotheruser3@mail.com", AccountType.GAMEOWNER);
        user = userAccountRepo.save(user);

        Event ongoingEvent = new Event(Date.valueOf(LocalDateTime.now().plusDays(1).toLocalDate()),
                Time.valueOf(LocalDateTime.now().plusHours(1).toLocalTime()),
                "Ongoing Location", "An event that is ongoing", 10, game, user, "Ongoing Event");
        ongoingEvent = eventRepo.save(ongoingEvent);

        // Register the user for the event
        EventRegistrationRequestDto request = new EventRegistrationRequestDto(user.getUserAccountID(),
                ongoingEvent.getEventID());
        client.postForEntity(BASE_URL, request, EventRegistrationResponseDto.class);

        // Simulate the event starting
        ongoingEvent.setDate(Date.valueOf(LocalDateTime.now().toLocalDate()));
        ongoingEvent.setTime(Time.valueOf(LocalDateTime.now().minusHours(1).toLocalTime()));
        eventRepo.save(ongoingEvent);

        String url = BASE_URL + "/" + user.getUserAccountID() + "/" + ongoingEvent.getEventID();
        ResponseEntity<ErrorDto> response = client.exchange(url, HttpMethod.DELETE, null, ErrorDto.class);

        assertEquals("Cannot cancel registration for an event that has already started.",
                response.getBody().getErrors().get(0));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
