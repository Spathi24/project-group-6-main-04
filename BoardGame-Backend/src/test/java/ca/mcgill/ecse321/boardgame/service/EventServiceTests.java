package ca.mcgill.ecse321.boardgame.service;

import ca.mcgill.ecse321.boardgame.exception.ResourceNotFoundException;
import ca.mcgill.ecse321.boardgame.model.*;
import ca.mcgill.ecse321.boardgame.repo.*;
import ca.mcgill.ecse321.boardgame.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class EventServiceTests {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private EventService eventService;

    @Mock
    private GameCopyRepository gameCopyRepository;

    @Mock
    private BorrowRequestRepository borrowRequestRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ----- TEST CASES FOR createEvent -----
    @Test
    public void testCreateEventValid() {
        // Arrange
        EventCreationDto dto = new EventCreationDto("Test Event", Date.valueOf("2023-11-15"), Time.valueOf("10:00:00"),
                "London", "Bob's event", 10, "UNO");
        Game game = new Game("UNO", "A card game", "Card Game");

        UserAccount user = new UserAccount("Bob", "1234", "Bob@gmail.com", AccountType.GAMEOWNER);
        GameCopy gameCopy = new GameCopy(new GameCopy.GameCopyKey(user, game), "In good shape");

        when(userAccountRepository.findUserAccountByUserAccountID(user.getUserAccountID())).thenReturn(user);
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gameRepository.findGameByTitle("UNO")).thenReturn(game);
        when(gameCopyRepository.findGameCopyByGameCopyKey(gameCopy.getGameCopyKey())).thenReturn(gameCopy);
        // Act
        Event result = eventService.createEvent(dto, user.getUserAccountID());

        // Assert
        assertNotNull(result);
        assertEquals("Test Event", result.getEventName());
        assertEquals(user, result.getCreator());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    public void testCreateEventUserNotFound() {
        // Arrange
        EventCreationDto dto = new EventCreationDto("Test Event", Date.valueOf("2023-11-15"), Time.valueOf("10:00:00"),
                "London", "Bob's event", 10, "MONOPOLY");
        Game game = new Game("UNO", "A card game", "Card Game");

        UserAccount user = new UserAccount("Bob", "1234", "Bob@gmail.com", AccountType.GAMEOWNER);
        GameCopy gameCopy = new GameCopy(new GameCopy.GameCopyKey(user, game), "In good shape");
        System.out.println(user.getUserAccountID());

        when(userAccountRepository.findUserAccountByUserAccountID(user.getUserAccountID())).thenReturn(user);
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gameRepository.findGameByTitle("MONOPOLY")).thenReturn(null);
        when(gameCopyRepository.findGameCopyByGameCopyKey(gameCopy.getGameCopyKey())).thenReturn(gameCopy);

        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            eventService.createEvent(dto, user.getUserAccountID());
        });
        assertEquals("Game with title " + dto.getGameTitle() + " not found.", exception.getMessage());
    }

    // ----- TEST CASES FOR updateEventDescription -----
    @Test
    public void testUpdateEventDescriptionValid() {
        // Arrange
        UserAccount user = new UserAccount("Bob", "1234", "Bob@gmail.com", AccountType.GAMEOWNER);
        Game game = new Game("UNO", "A card game", "Card Game");

        Event event = new Event(Date.valueOf("2023-11-15"), Time.valueOf("10:00:00"),
                "London", "Bob's event", 10, game, user, "Hokey Pokey");

        when(eventRepository.findEventByEventID(event.getEventID())).thenReturn(event);

        // Act
        eventService.updateEventDescription(event.getEventID(), "UNO event for pros");

        // Assert
        assertEquals("UNO event for pros", event.getDescription());
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    public void testUpdateEventDescriptionNotFound() {
        // Arrange
        UserAccount user = new UserAccount("Bob", "1234", "Bob@gmail.com", AccountType.GAMEOWNER);
        Game game = new Game("UNO", "A card game", "Card Game");

        Event event = new Event(Date.valueOf("2023-11-15"), Time.valueOf("10:00:00"),
                "London", "Bob's event", 10, game, user, "Hokey Pokey");
        when(eventRepository.findEventByEventID(event.getEventID())).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            eventService.updateEventDescription(event.getEventID(), "New Description");
        });
        assertEquals("Event with ID " + event.getEventID() + " not found", exception.getMessage());
    }

    // ----- TEST CASES FOR getEventById -----
    @Test
    public void testGetEventByIdValid() {
        // Arrange
        UserAccount user = new UserAccount("Bob", "1234", "Bob@gmail.com", AccountType.GAMEOWNER);
        Game game = new Game("UNO", "A card game", "Card Game");

        Event event = new Event(Date.valueOf("2023-11-15"), Time.valueOf("10:00:00"),
                "London", "Bob's event", 10, game, user, "Hokey Pokey");

        when(eventRepository.findEventByEventID(event.getEventID())).thenReturn(event);

        // Act
        Event result = eventService.getEventById(event.getEventID());

        // Assert
        assertNotNull(result);
        assertEquals(event.getEventID(), result.getEventID());
        assertEquals(event.getEventName(), result.getEventName());
    }

    @Test
    public void testGetEventByIdNotFound() {
        // Arrange

        UserAccount user = new UserAccount("Bob", "1234", "Bob@gmail.com", AccountType.GAMEOWNER);
        Game game = new Game("UNO", "A card game", "Card Game");

        Event event = new Event(Date.valueOf("2023-11-15"), Time.valueOf("10:00:00"),
                "London", "Bob's event", 10, game, user, "Hokey Pokey");

        when(eventRepository.findEventByEventID(event.getEventID())).thenReturn(null);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            eventService.getEventById(event.getEventID());
        });
        assertEquals("Event not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    // ----- TEST CASES FOR getAllEvents -----
    @Test
    public void testGetAllEventsValid() {
        // Arrange
        UserAccount user1 = new UserAccount("Bob", "1234", "Bob@gmail.com", AccountType.GAMEOWNER);
        Game game1 = new Game("UNO", "A card game", "Card Game");
        UserAccount user2 = new UserAccount("Ann", "4321", "Ann@gmail.com", AccountType.GAMEOWNER);
        Game game2 = new Game("MONOPOLY", "A board game", "Board Game");

        Event event1 = new Event(Date.valueOf("2023-11-15"), Time.valueOf("10:00:00"),
                "London", "Bob's event", 10, game1, user1, "Hokey Pokey");
        Event event2 = new Event(Date.valueOf("2023-11-13"), Time.valueOf("9:00:00"),
                "Montreal", "Ann's event", 6, game1, user1, "Disney Lovers");

        EventResponseDto eventResponseDto1 = new EventResponseDto(event1);
        EventResponseDto eventResponseDto2 = new EventResponseDto(event2);

        List<Event> eventList = Arrays.asList(event1, event2);

        when(eventRepository.findAll()).thenReturn(eventList);

        // Act
        List<EventResponseDto> result = eventService.getAllEvents();
        assertEquals(2, result.size());
        assertTrue(result.stream()
                .anyMatch(dto -> dto.getDescription().equals(eventResponseDto1.getDescription()) &&
                        dto.getEventName() == (eventResponseDto1.getEventName()) &&
                        (dto.getCreatorId().equals(eventResponseDto1.getCreatorId()) &&
                                dto.getGameTitle().equals(eventResponseDto1.getGameTitle()))));
        assertTrue(result.stream()
                .anyMatch(dto -> dto.getDescription().equals(eventResponseDto2.getDescription()) &&
                        dto.getEventName() == (eventResponseDto2.getEventName()) &&
                        (dto.getCreatorId().equals(eventResponseDto2.getCreatorId()) &&
                                dto.getGameTitle().equals(eventResponseDto2.getGameTitle()))));

    }

    @Test
    public void testOwnerBorrowsGame() {
        EventCreationDto dto = new EventCreationDto("Test Event", Date.valueOf("2023-11-15"), Time.valueOf("10:00:00"),
                "London", "Bob's event", 10, "UNO");
        Game game = new Game("UNO", "A card game", "Card Game");

        UserAccount user1 = new UserAccount("Bob", "1234", "Bob@gmail.com", AccountType.PLAYER);
        UserAccount user2 = new UserAccount("Ann", "4321", "Ann@gmail.com", AccountType.GAMEOWNER);
        user2.setUserAccountID(4);
        GameCopy gameCopy = new GameCopy(new GameCopy.GameCopyKey(user1, game), "In good shape");

        BorrowRequest borrowRequest = new BorrowRequest(RequestStatus.ACCEPTED, Date.valueOf("2022-11-15"),
                Date.valueOf("2023-06-15"), Date.valueOf("2023-11-10"), Date.valueOf("2023-11-18"),
                user2, gameCopy);

        List<BorrowRequest> borrowRequests = new ArrayList<>();
        borrowRequests.add(borrowRequest);

        when(borrowRequestRepository.findByBorrowerAndGameTitle(user2, game.getTitle())).thenReturn(borrowRequests);
        when(userAccountRepository.findUserAccountByUserAccountID(user1.getUserAccountID())).thenReturn(user1);
        when(userAccountRepository.findUserAccountByUserAccountID(user2.getUserAccountID())).thenReturn(user2);
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gameRepository.findGameByTitle("UNO")).thenReturn(game);
        when(gameCopyRepository.findGameCopyByGameCopyKey(gameCopy.getGameCopyKey())).thenReturn(gameCopy);
        // Act
        Event result = eventService.createEvent(dto, user2.getUserAccountID());

        // Assert
        assertNotNull(result);
        assertEquals("Test Event", result.getEventName());
        assertEquals(user2, result.getCreator());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    public void testOwnerBorrowsGame2() {
        // Arrange
        EventCreationDto dto = new EventCreationDto("Test Event", Date.valueOf("2023-11-20"), // Outside borrow period
                Time.valueOf("10:00:00"), "London", "Bob's event", 10, "UNO");
        Game game = new Game("UNO", "A card game", "Card Game");

        UserAccount user1 = new UserAccount("Bob", "1234", "Bob@gmail.com", AccountType.PLAYER);
        UserAccount user2 = new UserAccount("Ann", "4321", "Ann@gmail.com", AccountType.GAMEOWNER);
        user2.setUserAccountID(4);
        GameCopy gameCopy = new GameCopy(new GameCopy.GameCopyKey(user1, game), "In good shape");

        BorrowRequest borrowRequest = new BorrowRequest(RequestStatus.ACCEPTED, Date.valueOf("2022-11-15"),
                Date.valueOf("2023-06-15"), Date.valueOf("2023-11-10"), Date.valueOf("2023-11-18"),
                user2, gameCopy);

        List<BorrowRequest> borrowRequests = new ArrayList<>();
        borrowRequests.add(borrowRequest);

        when(borrowRequestRepository.findByBorrowerAndGameTitle(user2, game.getTitle())).thenReturn(borrowRequests);
        when(userAccountRepository.findUserAccountByUserAccountID(user1.getUserAccountID())).thenReturn(user1);
        when(userAccountRepository.findUserAccountByUserAccountID(user2.getUserAccountID())).thenReturn(user2);
        when(gameRepository.findGameByTitle("UNO")).thenReturn(game);
        when(gameCopyRepository.findGameCopyByGameCopyKey(gameCopy.getGameCopyKey())).thenReturn(gameCopy);

        // Ensure eventRepository.save() is not called when game is not available
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            eventService.createEvent(dto, user2.getUserAccountID());
        });

        // Assert exception
        assertNotNull(exception);
        assertEquals("The game: " + game.getTitle() + " is not available at the specified date/time.",
                exception.getMessage());

        // Verify save is not called due to exception
        verify(eventRepository, times(0)).save(any(Event.class));
    }

    @Test
    public void testDeleteEvent() {
        // Arrange

        UserAccount user = new UserAccount("Bob", "1234", "Bob@gmail.com", AccountType.GAMEOWNER);
        Game game = new Game("UNO", "A card game", "Card Game");

        Event event = new Event(Date.valueOf("2023-11-15"), Time.valueOf("10:00:00"),
                "London", "Bob's event", 10, game, user, "Hokey Pokey");

        // Mock the behavior
        when(eventRepository.findEventByEventID(event.getEventID())).thenReturn(event);

        // Act
        eventService.deleteEvent(event.getEventID());

        // Assert
        verify(eventRepository, times(1)).findEventByEventID(event.getEventID());
        verify(eventRepository, times(1)).delete(event);
    }

    @Test
    public void testDeleteExpiredEvents_Success() {

        UserAccount user1 = new UserAccount("Bob", "1234", "Bob@gmail.com", AccountType.GAMEOWNER);
        Game game1 = new Game("UNO", "A card game", "Card Game");
        UserAccount user2 = new UserAccount("Ann", "4321", "Ann@gmail.com", AccountType.GAMEOWNER);
        Game game2 = new Game("MONOPOLY", "A board game", "Board Game");

        Event event1 = new Event(Date.valueOf("2023-11-15"), Time.valueOf("10:00:00"),
                "London", "Bob's event", 10, game1, user1, "Hokey Pokey");
        Event event2 = new Event(Date.valueOf("2023-11-13"), Time.valueOf("9:00:00"),
                "Montreal", "Ann's event", 6, game1, user1, "Disney Lovers");

        List<Event> expiredEvents = Arrays.asList(event1, event2);
        when(eventRepository.findByDateBefore(any())).thenReturn(expiredEvents);

        eventService.deleteExpiredEvents();

        verify(eventRepository, times(1)).deleteAll(expiredEvents);
    }

}