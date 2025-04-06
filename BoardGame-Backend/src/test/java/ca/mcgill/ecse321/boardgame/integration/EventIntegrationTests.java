package ca.mcgill.ecse321.boardgame.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import ca.mcgill.ecse321.boardgame.exception.ResourceNotFoundException;
import ca.mcgill.ecse321.boardgame.service.EventService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ca.mcgill.ecse321.boardgame.dto.EventCreationDto;
import ca.mcgill.ecse321.boardgame.dto.EventResponseDto;
import ca.mcgill.ecse321.boardgame.dto.ErrorDto;
import ca.mcgill.ecse321.boardgame.model.*;
import ca.mcgill.ecse321.boardgame.repo.*;

import java.sql.Date;
import java.sql.Time;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EventIntegrationTests {

    @Autowired
    private TestRestTemplate client;

    @Autowired
    private EventService eventService;

    @Autowired
    private UserAccountRepository userAccountRepo;

    @Autowired
    private EventRepository eventRepo;

    @Autowired
    private GameRepository gameRepo;

    @Autowired
    private BorrowRequestRepository borrowRequestRepo;

    @Autowired
    private GameCopyRepository gameCopyRepo;

    private static final String BASE_URL = "/api/events";
    private long userAccountId;
    private String gameTitle;
    private long ownerId;

    @BeforeAll
    public void setup() {
        // Create a user account for testing
        UserAccount userAccount = new UserAccount("testuser", "password", "testuser@mail.com", AccountType.PLAYER);
        userAccount = userAccountRepo.save(userAccount);
        userAccountId = userAccount.getUserAccountID();

        // Create a game for testing
        Game game = new Game("Catan", "A fun strategy game", "Strategy");
        game = gameRepo.save(game);
        gameTitle = game.getTitle();

        // Create a game copy for the owner
        UserAccount owner = new UserAccount("owner", "password", "owner@mail.com", AccountType.GAMEOWNER);
        owner = userAccountRepo.save(owner);
        GameCopy gameCopy = new GameCopy(new GameCopy.GameCopyKey(owner, game), "In good shape");
        gameCopy = gameCopyRepo.save(gameCopy);

        // Create a borrow request for the user
        BorrowRequest borrowRequest = new BorrowRequest(RequestStatus.ACCEPTED, Date.valueOf("2023-01-01"),
                Date.valueOf("2023-12-31"), Date.valueOf("2023-11-01"), Date.valueOf("2023-11-30"),
                userAccount, gameCopy);
        borrowRequestRepo.save(borrowRequest);
        ownerId = owner.getUserAccountID();
    }

    @AfterAll
    public void clean() {
        borrowRequestRepo.deleteAll();
        gameCopyRepo.deleteAll();
        eventRepo.deleteAll();
        gameRepo.deleteAll();
        userAccountRepo.deleteAll();
    }

    @Test
    @Order(1)
    public void testCreateEventWithBorrowedGame() {
        // Arrange
        EventCreationDto dto = new EventCreationDto("Test Event", Date.valueOf("2023-11-15"), Time.valueOf("10:00:00"),
                "Test Location", "A fun event", 10, gameTitle);

        // Act
        ResponseEntity<EventResponseDto> response = client.postForEntity(BASE_URL + "/" + userAccountId, dto,
                EventResponseDto.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        EventResponseDto createdEvent = response.getBody();
        assertNotNull(createdEvent);
        assertEquals("Test Event", createdEvent.getEventName());
        assertEquals("Test Location", createdEvent.getLocation());
        assertEquals("A fun event", createdEvent.getDescription());
        assertEquals(10, createdEvent.getMaxParticipants());
        assertEquals("Catan", createdEvent.getGameTitle());
        assertEquals(userAccountId, createdEvent.getCreatorId());
    }

    @Test
    @Order(2)
    public void testCreateEventWithBorrowedGameOutsideTimeFrame() {
        // Arrange
        EventCreationDto dto = new EventCreationDto("Test Event Outside Time Frame", Date.valueOf("2023-12-01"),
                Time.valueOf("10:00:00"),
                "Test Location", "A fun event", 10, gameTitle);

        // Act
        ResponseEntity<ErrorDto> response = client.postForEntity(BASE_URL + "/" + userAccountId, dto,
                ErrorDto.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorDto error = response.getBody();
        assertNotNull(error);
        assertEquals("The game: Catan is not available at the specified date/time.", error.getErrors().get(0));
    }

    @Test
    @Order(3)
    public void testCreateEventWithOwnedGameCopy() {
        EventCreationDto dto = new EventCreationDto("Test Event", Date.valueOf("2023-12-01"),
                Time.valueOf("10:00:00"),
                "Test Location", "A fun event", 10, gameTitle);

        ResponseEntity<EventResponseDto> response = client.postForEntity(BASE_URL + "/" + ownerId, dto,
                EventResponseDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        EventResponseDto createdEvent = response.getBody();
        assertNotNull(createdEvent);
        assertEquals("Test Event", createdEvent.getEventName());
        assertEquals("Test Location", createdEvent.getLocation());
        assertEquals("A fun event", createdEvent.getDescription());
        assertEquals(10, createdEvent.getMaxParticipants());
        assertEquals("Catan", createdEvent.getGameTitle());
        assertEquals(ownerId, createdEvent.getCreatorId());
    }

    @Test
    @Order(4)
    public void testGetEventByIdValid() {

        Event event = new Event(Date.valueOf("2023-11-15"), Time.valueOf("10:00:00"),
                "London", "Bob's event", 10, gameRepo.findGameByTitle(gameTitle),
                userAccountRepo.findUserAccountByUserAccountID(ownerId), "Hokey Pokey");

        Event savedEvent = eventRepo.save(event);
        EventResponseDto dto = new EventResponseDto(savedEvent);

        ResponseEntity<EventResponseDto> response = client.getForEntity(BASE_URL + "/" + savedEvent.getEventID(),
                EventResponseDto.class);

        assertNotNull(response);
        EventResponseDto receivedEvent = response.getBody();
        assertNotNull(receivedEvent);
        assertEquals(dto.getEventName(), receivedEvent.getEventName());
    }

    @Test
    @Order(5)
    public void testGetEventByIdInvalid() {

        ResponseEntity<ErrorDto> response = client.getForEntity(BASE_URL + "/" + 112324,
                ErrorDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorDto receivedEvent = response.getBody();
        assertNotNull(receivedEvent);
        assertEquals("Event not found", receivedEvent.getErrors().get(0));
    }


    @Test
    @Order(6)
    public void testGetAllEventsSuccess() {

        Event event1 = new Event(Date.valueOf("2023-11-15"), Time.valueOf("10:00:00"),
                "London", "Bob's event", 10, gameRepo.findGameByTitle(gameTitle),
                userAccountRepo.findUserAccountByUserAccountID(ownerId), "Hokey Pokey");
        eventRepo.save(event1);

        Event event2 = new Event(Date.valueOf("2023-12-05"), Time.valueOf("14:00:00"),
                "Paris", "Alice's event", 15, gameRepo.findGameByTitle(gameTitle),
                userAccountRepo.findUserAccountByUserAccountID(userAccountId), "Dance Battle");
        eventRepo.save(event2);

        // Act
        ResponseEntity<EventResponseDto[]> response = client.getForEntity(BASE_URL, EventResponseDto[].class);

        // Assert
        assertNotNull(response);
        EventResponseDto[] events = response.getBody();
        assertNotNull(events);
        assertEquals(5, events.length);

        // Check details of the first event
        assertEquals("Bob's event", events[3].getDescription());
        assertEquals("London", events[3].getLocation());
        assertEquals("Hokey Pokey", events[3].getEventName());
        assertEquals(10, events[3].getMaxParticipants());
        assertEquals(gameTitle, events[3].getGameTitle());
        assertEquals(ownerId, events[3].getCreatorId());

        // Check details of the second event
        assertEquals("Alice's event", events[4].getDescription());
        assertEquals("Paris", events[4].getLocation());
        assertEquals("Dance Battle", events[4].getEventName());
        assertEquals(15, events[4].getMaxParticipants());
        assertEquals(gameTitle, events[4].getGameTitle());
        assertEquals(userAccountId, events[4].getCreatorId());
    }

    @Test
    @Order(7)
    public void testUpdateEventDescriptionValid() {

        // Arrange - Create a test event
        Event event = new Event(Date.valueOf("2023-11-15"), Time.valueOf("10:00:00"),
                "London", "Original Event", 10, gameRepo.findGameByTitle(gameTitle),
                userAccountRepo.findUserAccountByUserAccountID(ownerId), "Hokey Pokey");
        event = eventRepo.save(event);

        String newDescription = "Updated Event Description";

        // Act - Use correct URL format with event ID
        client.put(BASE_URL + "/" + event.getEventID() + "/description", newDescription);

        // Assert - Fetch the event and verify the description is updated
        Event updatedEvent = eventRepo.findEventByEventID(event.getEventID());
        assertNotNull(updatedEvent);
        assertEquals(newDescription, updatedEvent.getDescription());
        assertEquals(event.getEventID(), updatedEvent.getEventID());
        assertEquals("London", updatedEvent.getLocation());
        assertEquals("Hokey Pokey", updatedEvent.getEventName());
        assertEquals(10, updatedEvent.getMaxParticipant());
        assertEquals(gameTitle, updatedEvent.getGameToPlay().getTitle());
        assertEquals(ownerId, updatedEvent.getCreator().getUserAccountID());
    }

    @Test
    @Order(8)
    public void testUpdateEventDescriptionInvalid() {

        long invalidEventId = 9999L; // Event ID that doesn't exist in the database
        String newDescription = "This shouldn't work";

        // Act & Assert
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            // Call the service method
            eventService.updateEventDescription(invalidEventId, newDescription);
        });

        // Assert - Check that the error message matches the expected
        assertEquals("Event with ID " + invalidEventId + " not found", thrown.getMessage());
    }

    @Test
    @Order(9)
    public void testDeleteEventValid() {

        Event event = new Event(Date.valueOf("2023-11-15"), Time.valueOf("10:00:00"),
                "London", "Event to be deleted", 10, gameRepo.findGameByTitle(gameTitle),
                userAccountRepo.findUserAccountByUserAccountID(ownerId), "Hokey Pokey");
        event = eventRepo.save(event);

        // Act - Delete the event
        client.delete(BASE_URL + "/" + event.getEventID());

        // Assert - Try to fetch the deleted event and verify it doesn't exist
        ResponseEntity<ErrorDto> response = client.getForEntity(BASE_URL + "/" + event.getEventID(), ErrorDto.class);

        // Assert - Check that the event is not found
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorDto error = response.getBody();
        assertNotNull(error);
        assertEquals("Event not found", error.getErrors().get(0));
    }

    @Test
    @Order(10)
    public void testDeleteExpiredEvents() {
        // Arrange - Create two events, one expired and one upcoming
        Event expiredEvent = new Event(Date.valueOf("2023-11-15"), Time.valueOf("10:00:00"),
                "London", "Expired Event", 10, gameRepo.findGameByTitle(gameTitle),
                userAccountRepo.findUserAccountByUserAccountID(ownerId), "Expired Hokey Pokey");
        expiredEvent = eventRepo.save(expiredEvent);


        // Act - Call the method that deletes expired events
        eventService.deleteExpiredEvents(); // Assume this method checks for events with past dates

        // Assert - Verify that the expired event is deleted
        ResponseEntity<ErrorDto> expiredEventResponse = client.getForEntity(BASE_URL + "/" + expiredEvent.getEventID(), ErrorDto.class);
        assertNotNull(expiredEventResponse);
        assertEquals(HttpStatus.NOT_FOUND, expiredEventResponse.getStatusCode());
        ErrorDto expiredEventError = expiredEventResponse.getBody();
        assertNotNull(expiredEventError);
        assertEquals("Event not found", expiredEventError.getErrors().get(0));

    }



}