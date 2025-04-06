package ca.mcgill.ecse321.boardgame.config;

import ca.mcgill.ecse321.boardgame.model.*;
import ca.mcgill.ecse321.boardgame.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Database initializer for populating the database with colorful user data.
 * This will run automatically when the application starts with the "dev"
 * profile.
 * Can also be run as a standalone application with the main() method.
 */
@Configuration
public class DbInitializer {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameCopyRepository gameCopyRepository;

    @Autowired
    private BorrowRequestRepository borrowRequestRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    /**
     * Initializes the database with sample data when the "dev" profile is active.
     */
    @Bean
    @Profile("dev")
    public CommandLineRunner initDatabaseWithColorfulUsers() {
        return args -> {
            System.out.println("Initializing database with colorful user data...");

            try {
                // Clear database first
                clearDatabase();

                // Create and save user accounts
                List<UserAccount> userAccounts = createUserAccounts();
                System.out.println("Created " + userAccounts.size() + " colorful user accounts");

                // Create and save games
                List<Game> games = createGames();
                System.out.println("Created " + games.size() + " games");

                // Create and save game copies
                List<GameCopy> gameCopies = createGameCopies(userAccounts, games);
                System.out.println("Created " + gameCopies.size() + " game copies");

                // Create and save events
                List<Event> events = createEvents(userAccounts, games);
                System.out.println("Created " + events.size() + " events");

                // Create and save event registrations
                List<EventRegistration> eventRegistrations = createEventRegistrations(userAccounts, events);
                System.out.println("Created " + eventRegistrations.size() + " event registrations");

                // Create and save borrow requests
                List<BorrowRequest> borrowRequests = createBorrowRequests(userAccounts, gameCopies);
                System.out.println("Created " + borrowRequests.size() + " borrow requests");

                // Create and save reviews
                List<Review> reviews = createReviews(userAccounts, games);
                System.out.println("Created " + reviews.size() + " reviews");

                System.out.println("Colorful database initialization completed successfully!");
            } catch (Exception e) {
                System.err.println("Error during colorful database initialization: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }

    // Replace the current main method with this simpler version
    public static void main(String[] args) {
        System.out.println("Starting standalone database initializer");

        // Create a Spring application that scans all necessary packages
        SpringApplication app = new SpringApplication(DbInitializer.class);

        // Explicitly enable component scanning for the entire application
        app.addInitializers(ctx -> {
            System.out.println("Spring context initialized");
        });

        // These settings create a non-web application with dev profile active
        app.setWebApplicationType(WebApplicationType.NONE);
        app.setAdditionalProfiles("dev");

        // Run the application
        app.run(args);

        System.out.println("Database initialization complete!");
    }

    /**
     * Clears all data from the database.
     */
    private void clearDatabase() {
        System.out.println("Clearing database...");
        try {
            borrowRequestRepository.deleteAll();
            System.out.println("Cleared borrow requests");
        } catch (Exception e) {
            System.err.println("Error clearing borrow requests: " + e.getMessage());
        }

        try {
            eventRegistrationRepository.deleteAll();
            System.out.println("Cleared event registrations");
        } catch (Exception e) {
            System.err.println("Error clearing event registrations: " + e.getMessage());
        }

        try {
            eventRepository.deleteAll();
            System.out.println("Cleared events");
        } catch (Exception e) {
            System.err.println("Error clearing events: " + e.getMessage());
        }

        try {
            reviewRepository.deleteAll();
            System.out.println("Cleared reviews");
        } catch (Exception e) {
            System.err.println("Error clearing reviews: " + e.getMessage());
        }

        try {
            gameCopyRepository.deleteAll();
            System.out.println("Cleared game copies");
        } catch (Exception e) {
            System.err.println("Error clearing game copies: " + e.getMessage());
        }

        try {
            gameRepository.deleteAll();
            System.out.println("Cleared games");
        } catch (Exception e) {
            System.err.println("Error clearing games: " + e.getMessage());
        }

        try {
            userAccountRepository.deleteAll();
            System.out.println("Cleared users");
        } catch (Exception e) {
            System.err.println("Error clearing users: " + e.getMessage());
        }
    }

    /**
     * Creates sample user accounts with colorful names.
     */
    private List<UserAccount> createUserAccounts() {
        System.out.println("Creating sample users with colorful names...");
        List<UserAccount> accounts = new ArrayList<>();

        try {
            // Game Owners
            UserAccount trump = new UserAccount("DonaldTrump", "password123", "donald.trump@example.com",
                    AccountType.GAMEOWNER);
            UserAccount musk = new UserAccount("ElonMusk", "password123", "elon.musk@example.com",
                    AccountType.GAMEOWNER);
            UserAccount putin = new UserAccount("VladimirPutin", "password123", "vladimir.putin@example.com",
                    AccountType.GAMEOWNER);
            accounts.add(userAccountRepository.save(trump));
            accounts.add(userAccountRepository.save(musk));
            accounts.add(userAccountRepository.save(putin));

            // Players
            UserAccount kim = new UserAccount("KimJongUn", "password123", "kim.jong.un@example.com",
                    AccountType.PLAYER);
            UserAccount vance = new UserAccount("JDVance", "password123", "jdvance@example.com", AccountType.PLAYER);
            UserAccount swift = new UserAccount("TaylorSwift", "password123", "taylor.swift@example.com",
                    AccountType.PLAYER);
            UserAccount sanders = new UserAccount("BernieSanders", "password123", "bernie@example.com",
                    AccountType.PLAYER);
            UserAccount biden = new UserAccount("JoeBiden", "password123", "joe.biden@example.com", AccountType.PLAYER);
            accounts.add(userAccountRepository.save(kim));
            accounts.add(userAccountRepository.save(vance));
            accounts.add(userAccountRepository.save(swift));
            accounts.add(userAccountRepository.save(sanders));
            accounts.add(userAccountRepository.save(biden));

            System.out.println(
                    "Created colorful users: DonaldTrump, ElonMusk, VladimirPutin, KimJongUn, JDVance, TaylorSwift, BernieSanders, JoeBiden, AdminUser");
        } catch (Exception e) {
            System.err.println("Error creating users: " + e.getMessage());
            e.printStackTrace();
        }

        return accounts;
    }

    /**
     * Creates sample games.
     */
    private List<Game> createGames() {
        System.out.println("Creating sample games...");
        List<Game> games = new ArrayList<>();

        try {
            Game chess = new Game("Chess", "A classic strategy board game for two players.", "Strategy");
            Game monopoly = new Game("Monopoly", "A real estate trading game for two to eight players.", "Family");
            Game catan = new Game("Settlers of Catan",
                    "A multiplayer board game where players collect resources and build settlements.", "Strategy");
            Game uno = new Game("UNO", "A card game where players match colors or numbers.", "Card Game");
            Game scrabble = new Game("Scrabble", "A word game in which players score points by placing tiles.",
                    "Word Game");
            Game risk = new Game("Risk", "A strategy board game of diplomacy, conflict and conquest.", "Strategy");
            Game ticket = new Game("Ticket to Ride",
                    "A cross-country train adventure where players collect cards of various types of train cars.",
                    "Family");
            Game pandemic = new Game("Pandemic",
                    "A cooperative game where players work as a team to treat infections around the world.",
                    "Cooperative");
            Game clue = new Game("Clue",
                    "A murder mystery game where players try to figure out who committed the crime.", "Mystery");
            Game jenga = new Game("Jenga",
                    "A game of physical skill where players take turns removing blocks from a tower.", "Dexterity");

            games.add(gameRepository.save(chess));
            games.add(gameRepository.save(monopoly));
            games.add(gameRepository.save(catan));
            games.add(gameRepository.save(uno));
            games.add(gameRepository.save(scrabble));
            games.add(gameRepository.save(risk));
            games.add(gameRepository.save(ticket));
            games.add(gameRepository.save(pandemic));
            games.add(gameRepository.save(clue));
            games.add(gameRepository.save(jenga));
        } catch (Exception e) {
            System.err.println("Error creating games: " + e.getMessage());
            e.printStackTrace();
        }

        return games;
    }

    /**
     * Creates sample game copies.
     */
    private List<GameCopy> createGameCopies(List<UserAccount> users, List<Game> games) {
        System.out.println("Creating sample game copies...");
        List<GameCopy> gameCopies = new ArrayList<>();

        try {
            // Get users by name
            UserAccount trump = getUserByName(users, "DonaldTrump");
            UserAccount musk = getUserByName(users, "ElonMusk");
            UserAccount putin = getUserByName(users, "VladimirPutin");

            // Get games by title
            Game chess = getGameByTitle(games, "Chess");
            Game monopoly = getGameByTitle(games, "Monopoly");
            Game catan = getGameByTitle(games, "Settlers of Catan");
            Game uno = getGameByTitle(games, "UNO");
            Game scrabble = getGameByTitle(games, "Scrabble");
            Game risk = getGameByTitle(games, "Risk");
            Game ticket = getGameByTitle(games, "Ticket to Ride");
            Game pandemic = getGameByTitle(games, "Pandemic");
            Game clue = getGameByTitle(games, "Clue");
            Game jenga = getGameByTitle(games, "Jenga");

            // Trump's game copies
            if (trump != null) {
                gameCopies.add(createAndSaveGameCopy(trump, chess,
                        "Gold-plated chess set, the best chess set, truly tremendous"));
                gameCopies.add(createAndSaveGameCopy(trump, monopoly, "Trump Edition with real estate tycoon rules"));
                gameCopies.add(createAndSaveGameCopy(trump, uno, "Limited MAGA edition with extra Skip cards"));
                gameCopies.add(createAndSaveGameCopy(trump, risk,
                        "America First edition - all pieces are red white and blue"));

                // Set one game to BORROWED status to test that functionality
                GameCopy trumpClue = createAndSaveGameCopy(trump, clue,
                        "Gold-plated Clue set, it's got the best murder weapons");
                trumpClue.setStatus(GameStatus.BORROWED);
                gameCopyRepository.save(trumpClue);
                gameCopies.add(trumpClue);
            }

            // Musk's game copies
            if (musk != null) {
                gameCopies.add(createAndSaveGameCopy(musk, catan, "Mars colonization special edition"));
                gameCopies.add(createAndSaveGameCopy(musk, scrabble, "With extra X Æ A-12 bonus tiles"));
                gameCopies.add(createAndSaveGameCopy(musk, pandemic,
                        "BioTech Edition with special Tesla-branded vaccine cards"));

                // Set one game to DAMAGED status
                GameCopy muskTicket = createAndSaveGameCopy(musk, ticket, "Hyperloop extension pack included");
                muskTicket.setStatus(GameStatus.DAMAGED);
                gameCopyRepository.save(muskTicket);
                gameCopies.add(muskTicket);
            }

            // Putin's game copies
            if (putin != null) {
                gameCopies.add(createAndSaveGameCopy(putin, chess, "Antique Soviet chess set from the Cold War era"));
                gameCopies.add(
                        createAndSaveGameCopy(putin, risk, "Special edition with extra pieces for the Eastern Bloc"));
                gameCopies.add(createAndSaveGameCopy(putin, jenga, "Solid titanium blocks - extremely difficult"));
            }
        } catch (Exception e) {
            System.err.println("Error creating game copies: " + e.getMessage());
            e.printStackTrace();
        }

        return gameCopies;
    }

    /**
     * Helper method to create and save a game copy.
     */
    private GameCopy createAndSaveGameCopy(UserAccount owner, Game game, String description) {
        GameCopy gameCopy = new GameCopy(new GameCopy.GameCopyKey(owner, game), description);
        return gameCopyRepository.save(gameCopy);
    }

    /**
     * Creates sample events.
     */
    private List<Event> createEvents(List<UserAccount> users, List<Game> games) {
        System.out.println("Creating sample events...");
        List<Event> events = new ArrayList<>();

        try {
            // Get users
            UserAccount trump = getUserByName(users, "DonaldTrump");
            UserAccount musk = getUserByName(users, "ElonMusk");
            UserAccount putin = getUserByName(users, "VladimirPutin");

            // Get games
            Game chess = getGameByTitle(games, "Chess");
            Game monopoly = getGameByTitle(games, "Monopoly");
            Game catan = getGameByTitle(games, "Settlers of Catan");
            Game risk = getGameByTitle(games, "Risk");
            Game pandemic = getGameByTitle(games, "Pandemic");

            // Calculate dates
            Date tomorrow = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
            Date nextWeek = new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000);
            Date twoWeeksLater = new Date(System.currentTimeMillis() + 14 * 24 * 60 * 60 * 1000);
            Date pastDate = new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000);

            // Times
            Time morningTime = Time.valueOf(LocalTime.of(10, 0));
            Time afternoonTime = Time.valueOf(LocalTime.of(14, 0));
            Time eveningTime = Time.valueOf(LocalTime.of(19, 0));

            // Trump's events
            if (trump != null && monopoly != null) {
                Event trumpMonopolyNight = new Event(
                        tomorrow,
                        eveningTime,
                        "Trump Tower, NY",
                        "The best monopoly tournament ever held, believe me",
                        8,
                        monopoly,
                        trump,
                        "Trump's Monopoly Championship");
                events.add(eventRepository.save(trumpMonopolyNight));

                Event trumpPastEvent = new Event(
                        pastDate,
                        afternoonTime,
                        "Mar-a-Lago, FL",
                        "A tremendous game night with only the best people",
                        10,
                        chess,
                        trump,
                        "Trump's Chess Exhibition");
                events.add(eventRepository.save(trumpPastEvent));
            }

            // Musk's events
            if (musk != null && catan != null) {
                Event muskCatanEvent = new Event(
                        nextWeek,
                        morningTime,
                        "SpaceX HQ, CA",
                        "Mars colonization simulation using Catan mechanics",
                        12,
                        catan,
                        musk,
                        "SpaceX Catan Challenge");
                events.add(eventRepository.save(muskCatanEvent));
            }

            // Putin's events
            if (putin != null && risk != null) {
                Event putinRiskEvent = new Event(
                        twoWeeksLater,
                        eveningTime,
                        "Moscow, Russia",
                        "Strategic world domination exercise using Risk",
                        6,
                        risk,
                        putin,
                        "Putin's Risk Tournament");
                events.add(eventRepository.save(putinRiskEvent));

                Event putinPandemicEvent = new Event(
                        nextWeek,
                        afternoonTime,
                        "St. Petersburg, Russia",
                        "Cooperative pandemic response simulation",
                        4,
                        pandemic,
                        putin,
                        "Putin's Pandemic Response Exercise");
                events.add(eventRepository.save(putinPandemicEvent));
            }
        } catch (Exception e) {
            System.err.println("Error creating events: " + e.getMessage());
            e.printStackTrace();
        }

        return events;
    }

    /**
     * Creates sample event registrations.
     */
    private List<EventRegistration> createEventRegistrations(List<UserAccount> users, List<Event> events) {
        System.out.println("Creating sample event registrations...");
        List<EventRegistration> registrations = new ArrayList<>();

        try {
            if (events.isEmpty() || users.isEmpty()) {
                return registrations;
            }

            // Get players
            UserAccount kim = getUserByName(users, "KimJongUn");
            UserAccount vance = getUserByName(users, "JDVance");
            UserAccount swift = getUserByName(users, "TaylorSwift");
            UserAccount sanders = getUserByName(users, "BernieSanders");
            UserAccount biden = getUserByName(users, "JoeBiden");

            // Register players for events with different statuses
            if (events.size() >= 1 && kim != null) {
                EventRegistration.EventRegistrationKey key = new EventRegistration.EventRegistrationKey(kim,
                        events.get(0));
                EventRegistration reg = new EventRegistration(key, ParticipationStatus.PENDING);
                registrations.add(eventRegistrationRepository.save(reg));
            }

            if (events.size() >= 2 && vance != null) {
                EventRegistration.EventRegistrationKey key = new EventRegistration.EventRegistrationKey(vance,
                        events.get(0));
                EventRegistration reg = new EventRegistration(key, ParticipationStatus.ATTEND);
                registrations.add(eventRegistrationRepository.save(reg));

                // Vance also registers for another event
                if (events.size() >= 3) {
                    EventRegistration.EventRegistrationKey key2 = new EventRegistration.EventRegistrationKey(vance,
                            events.get(2));
                    EventRegistration reg2 = new EventRegistration(key2, ParticipationStatus.PENDING);
                    registrations.add(eventRegistrationRepository.save(reg2));
                }
            }

            if (events.size() >= 3 && swift != null) {
                EventRegistration.EventRegistrationKey key = new EventRegistration.EventRegistrationKey(swift,
                        events.get(2));
                EventRegistration reg = new EventRegistration(key, ParticipationStatus.ABSENT);
                registrations.add(eventRegistrationRepository.save(reg));
            }

            if (events.size() >= 4 && sanders != null) {
                EventRegistration.EventRegistrationKey key = new EventRegistration.EventRegistrationKey(sanders,
                        events.get(3));
                EventRegistration reg = new EventRegistration(key, ParticipationStatus.ATTEND);
                registrations.add(eventRegistrationRepository.save(reg));
            }

            if (events.size() >= 5 && biden != null) {
                EventRegistration.EventRegistrationKey key = new EventRegistration.EventRegistrationKey(biden,
                        events.get(4));
                EventRegistration reg = new EventRegistration(key, ParticipationStatus.PENDING);
                registrations.add(eventRegistrationRepository.save(reg));
            }
        } catch (Exception e) {
            System.err.println("Error creating event registrations: " + e.getMessage());
            e.printStackTrace();
        }

        return registrations;
    }

    /**
     * Creates sample borrow requests with different statuses.
     */
    private List<BorrowRequest> createBorrowRequests(List<UserAccount> users, List<GameCopy> gameCopies) {
        System.out.println("Creating sample borrow requests...");
        List<BorrowRequest> borrowRequests = new ArrayList<>();

        // Current date and future dates for borrowing
        Date today = new Date(System.currentTimeMillis());
        Date startDate = new Date(System.currentTimeMillis() + 86400000); // tomorrow
        Date endDate = new Date(System.currentTimeMillis() + 604800000); // one week later
        Date laterStart = new Date(System.currentTimeMillis() + 172800000); // two days later
        Date laterEnd = new Date(System.currentTimeMillis() + 864000000); // ten days later
        Date pastStart = new Date(System.currentTimeMillis() - 604800000); // one week ago
        Date pastEnd = new Date(System.currentTimeMillis() + 86400000); // tomorrow

        try {
            // Get players
            UserAccount kim = getUserByName(users, "KimJongUn");
            UserAccount vance = getUserByName(users, "JDVance");
            UserAccount swift = getUserByName(users, "TaylorSwift");
            UserAccount sanders = getUserByName(users, "BernieSanders");
            UserAccount biden = getUserByName(users, "JoeBiden");

            // Pending borrow requests
            if (kim != null && gameCopies.size() > 0) {
                BorrowRequest request = new BorrowRequest(
                        RequestStatus.PENDING,
                        today,
                        null,
                        startDate,
                        endDate,
                        kim,
                        gameCopies.get(0));
                borrowRequests.add(borrowRequestRepository.save(request));
            }

            if (vance != null && gameCopies.size() > 1) {
                BorrowRequest request = new BorrowRequest(
                        RequestStatus.PENDING,
                        today,
                        null,
                        laterStart,
                        laterEnd,
                        vance,
                        gameCopies.get(1));
                borrowRequests.add(borrowRequestRepository.save(request));
            }

            // Accepted borrow request
            if (swift != null && gameCopies.size() > 2) {
                BorrowRequest request = new BorrowRequest(
                        RequestStatus.ACCEPTED,
                        today,
                        today,
                        startDate,
                        endDate,
                        swift,
                        gameCopies.get(2));
                borrowRequests.add(borrowRequestRepository.save(request));

                // Update game copy status to BORROWED
                gameCopies.get(2).setStatus(GameStatus.BORROWED);
                gameCopyRepository.save(gameCopies.get(2));
            }

            // Declined borrow request
            if (sanders != null && gameCopies.size() > 3) {
                BorrowRequest request = new BorrowRequest(
                        RequestStatus.DECLINED,
                        today,
                        today,
                        laterStart,
                        laterEnd,
                        sanders,
                        gameCopies.get(3));
                borrowRequests.add(borrowRequestRepository.save(request));
            }

            // Active borrow with past start date
            if (biden != null && gameCopies.size() > 4) {
                BorrowRequest request = new BorrowRequest(
                        RequestStatus.ACCEPTED,
                        pastStart,
                        pastStart,
                        pastStart,
                        pastEnd,
                        biden,
                        gameCopies.get(4));
                borrowRequests.add(borrowRequestRepository.save(request));

                // Update game copy status to BORROWED
                gameCopies.get(4).setStatus(GameStatus.BORROWED);
                gameCopyRepository.save(gameCopies.get(4));
            }
        } catch (Exception e) {
            System.err.println("Error creating borrow requests: " + e.getMessage());
            e.printStackTrace();
        }

        return borrowRequests;
    }

    /**
     * Creates sample reviews for games.
     */
    private List<Review> createReviews(List<UserAccount> users, List<Game> games) {
        System.out.println("Creating sample reviews...");
        List<Review> reviews = new ArrayList<>();

        Date today = new Date(System.currentTimeMillis());
        Date yesterday = new Date(System.currentTimeMillis() - 86400000);
        Date lastWeek = new Date(System.currentTimeMillis() - 604800000);

        try {
            // Get players
            UserAccount kim = getUserByName(users, "KimJongUn");
            UserAccount vance = getUserByName(users, "JDVance");
            UserAccount swift = getUserByName(users, "TaylorSwift");
            UserAccount sanders = getUserByName(users, "BernieSanders");
            UserAccount biden = getUserByName(users, "JoeBiden");

            // Get games
            Game chess = getGameByTitle(games, "Chess");
            Game monopoly = getGameByTitle(games, "Monopoly");
            Game catan = getGameByTitle(games, "Settlers of Catan");
            Game uno = getGameByTitle(games, "UNO");
            Game scrabble = getGameByTitle(games, "Scrabble");
            Game risk = getGameByTitle(games, "Risk");

            // Kim's reviews
            if (kim != null) {
                if (chess != null) {
                    Review.ReviewKey key = new Review.ReviewKey(kim, chess);
                    Review review = new Review(key, 5, "A strategic game that perfectly models international diplomacy",
                            today);
                    reviews.add(reviewRepository.save(review));
                }

                if (risk != null) {
                    Review.ReviewKey key = new Review.ReviewKey(kim, risk);
                    Review review = new Review(key, 5, "My favorite game - I love the world domination aspect!",
                            yesterday);
                    reviews.add(reviewRepository.save(review));
                }
            }

            // Vance's reviews
            if (vance != null) {
                if (monopoly != null) {
                    Review.ReviewKey key = new Review.ReviewKey(vance, monopoly);
                    Review review = new Review(key, 4, "Great game for understanding the working class struggle",
                            today);
                    reviews.add(reviewRepository.save(review));
                }
            }

            // Swift's reviews
            if (swift != null) {
                if (scrabble != null) {
                    Review.ReviewKey key = new Review.ReviewKey(swift, scrabble);
                    Review review = new Review(key, 5,
                            "Perfect game for a songwriter - helps me find new words for lyrics", lastWeek);
                    reviews.add(reviewRepository.save(review));
                }

                if (uno != null) {
                    Review.ReviewKey key = new Review.ReviewKey(swift, uno);
                    Review review = new Review(key, 3, "Fun but can get repetitive after a while", yesterday);
                    reviews.add(reviewRepository.save(review));
                }
            }

            // Sanders' reviews
            if (sanders != null && catan != null) {
                Review.ReviewKey key = new Review.ReviewKey(sanders, catan);
                Review review = new Review(key, 4, "Great game about resource distribution and fair trade", lastWeek);
                reviews.add(reviewRepository.save(review));
            }

            // Biden's reviews
            if (biden != null) {
                if (chess != null) {
                    Review.ReviewKey key = new Review.ReviewKey(biden, chess);
                    Review review = new Review(key, 3, "Been playing this since before you were born, Jack!", today);
                    reviews.add(reviewRepository.save(review));
                }

                if (monopoly != null) {
                    Review.ReviewKey key = new Review.ReviewKey(biden, monopoly);
                    Review review = new Review(key, 2, "The banker always seems to cheat in this game", yesterday);
                    reviews.add(reviewRepository.save(review));
                }
            }
        } catch (Exception e) {
            System.err.println("Error creating reviews: " + e.getMessage());
            e.printStackTrace();
        }

        return reviews;
    }

    // Helper methods to find entities by name/title
    private UserAccount getUserByName(List<UserAccount> users, String name) {
        return users.stream()
                .filter(u -> u.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private Game getGameByTitle(List<Game> games, String title) {
        return games.stream()
                .filter(g -> g.getTitle().equals(title))
                .findFirst()
                .orElse(null);
    }
}
