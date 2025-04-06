package ca.mcgill.ecse321.boardgame.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import ca.mcgill.ecse321.boardgame.dto.GameCopyCreationDto;
import ca.mcgill.ecse321.boardgame.dto.GameCopyResponseDto;
import ca.mcgill.ecse321.boardgame.exception.ResourceNotFoundException;
import ca.mcgill.ecse321.boardgame.model.AccountType;
import ca.mcgill.ecse321.boardgame.model.Game;
import ca.mcgill.ecse321.boardgame.model.GameCopy;
import ca.mcgill.ecse321.boardgame.model.GameCopy.GameCopyKey;
import ca.mcgill.ecse321.boardgame.model.UserAccount;
import ca.mcgill.ecse321.boardgame.repo.GameCopyRepository;
import ca.mcgill.ecse321.boardgame.repo.GameRepository;
import ca.mcgill.ecse321.boardgame.repo.UserAccountRepository;

@SpringBootTest
public class GameCopyServiceTests {
    @Mock
    private GameCopyRepository gameCopyRepository;

    @Mock
    private GameService gameService;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameCopyService gameCopyService;

    @Test
    public void testCreateGameCopy() {
        // Arrange
        UserAccount owner = new UserAccount("Bob", "1234", "djt@mail.com", AccountType.GAMEOWNER);
        Game game = new Game("UNO", "A card game", "Card Game");

        when(userAccountRepository.findUserAccountByUserAccountID(owner.getUserAccountID())).thenReturn(owner);
        when(gameService.getGameByTitle(game.getTitle())).thenReturn(game);

        GameCopyCreationDto gameCopyCreationDto = new GameCopyCreationDto("UNO", "In good shape",
                owner.getUserAccountID());
        GameCopy gameCopy = new GameCopy(new GameCopyKey(owner, game), "In good shape");

        when(gameCopyRepository.save(any(GameCopy.class))).thenReturn(gameCopy);

        // Act
        GameCopy createdGameCopy = gameCopyService.createGameCopy(gameCopyCreationDto);

        // Assert
        assertNotNull(createdGameCopy);
        assertEquals(gameCopy.getDescription(), createdGameCopy.getDescription());
        assertEquals(gameCopy.getGameCopyKey().getGame(), createdGameCopy.getGameCopyKey().getGame());
        assertEquals(gameCopy.getGameCopyKey().getOwner(), createdGameCopy.getGameCopyKey().getOwner());
        verify(gameCopyRepository, times(1)).save(any(GameCopy.class));
    }

    @Test
    public void testCreateGameCopyWithInvalidTitle() {
        // Arrange
        UserAccount owner = new UserAccount("Bob", "1234", "djt@mail.com", AccountType.GAMEOWNER);

        GameCopyCreationDto gameCopyCreationDto = new GameCopyCreationDto("Chess", "In good shape",
                owner.getUserAccountID());

        when(userAccountRepository.findUserAccountByUserAccountID(owner.getUserAccountID())).thenReturn(owner);
        when(gameService.getGameByTitle("Chess")).thenReturn(null);

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            gameCopyService.createGameCopy(gameCopyCreationDto);
        });

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Game with title Chess not found", exception.getMessage());
    }

    @Test
    public void CreateGameCopyWithInvalidOwner2() {
        // Arrange
        UserAccount owner = new UserAccount("Bob", "1234", "djt@mail.com", AccountType.PLAYER);
        Game game = new Game("UNO", "A card game", "Card Game");

        when(userAccountRepository.findUserAccountByUserAccountID(owner.getUserAccountID())).thenReturn(owner);
        when(gameService.getGameByTitle(game.getTitle())).thenReturn(game);

        GameCopyCreationDto gameCopyCreationDto = new GameCopyCreationDto("UNO", "In good shape",
                owner.getUserAccountID());

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            gameCopyService.createGameCopy(gameCopyCreationDto);
        });

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Only game owners can own game copies", exception.getMessage());
    }

    @Test
    public void tesCreateGameCopyWithInvalidOwner1() {
        // Arrange
        Game game = new Game("UNO", "A card game", "Card Game");

        when(userAccountRepository.findUserAccountByUserAccountID(1)).thenReturn(null);
        when(gameService.getGameByTitle(game.getTitle())).thenReturn(game);

        GameCopyCreationDto gameCopyCreationDto = new GameCopyCreationDto("UNO", "In good shape",
                1);

        // Act
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            gameCopyService.createGameCopy(gameCopyCreationDto);
        });

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("UserAccount with ID 1 not found", exception.getMessage());
    }

    @Test
    public void testUpdateGameCopyDescription() {
        // Arrange
        UserAccount owner = new UserAccount("Bob", "1234", "djt@mail.com", AccountType.GAMEOWNER);
        Game game = new Game("UNO", "A card game", "Card Game");

        GameCopy gameCopy = new GameCopy(new GameCopyKey(owner, game), "In good shape");

        when(userAccountRepository.findUserAccountByUserAccountID(owner.getUserAccountID())).thenReturn(owner);
        when(gameService.getGameByTitle("UNO")).thenReturn(game);
        when(gameCopyRepository.findGameCopyByGameCopyKey(any(GameCopyKey.class))).thenReturn(gameCopy);
        when(gameCopyRepository.save(any(GameCopy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        gameCopyService.updateDescription("UNO", owner.getUserAccountID(), "In bad shape");

        // Assert
        assertEquals("In bad shape", gameCopy.getDescription());
        verify(gameCopyRepository, times(1)).save(gameCopy);
    }

    @Test
    public void testUpdateGameCopyNullDescription() {
        // Arrange
        UserAccount owner = new UserAccount("Bob", "1234", "djt@mail.com", AccountType.GAMEOWNER);
        Game game = new Game("UNO", "A card game", "Card Game");

        GameCopy gameCopy = new GameCopy(new GameCopyKey(owner, game), "In good shape");

        when(userAccountRepository.findUserAccountByUserAccountID(owner.getUserAccountID())).thenReturn(owner);
        when(gameService.getGameByTitle("UNO")).thenReturn(game);
        when(gameCopyRepository.findGameCopyByGameCopyKey(any(GameCopyKey.class))).thenReturn(gameCopy);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            gameCopyService.updateDescription("UNO", owner.getUserAccountID(), null);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Description cannot be empty", exception.getMessage());
    }

    @Test
    public void testDeleteGameCopy() {
        UserAccount owner = new UserAccount("Bob", "1234", "djt@mail.com", AccountType.GAMEOWNER);
        Game game = new Game("UNO", "A card game", "Card Game");

        GameCopy gameCopy = new GameCopy(new GameCopyKey(owner, game), "In good shape");

        when(userAccountRepository.findUserAccountByUserAccountID(owner.getUserAccountID())).thenReturn(owner);
        when(gameService.getGameByTitle("UNO")).thenReturn(game);
        when(gameCopyRepository.findGameCopyByGameCopyKey(any(GameCopyKey.class))).thenReturn(gameCopy);

        // Act
        gameCopyService.deleteGameCopy("UNO", owner.getUserAccountID());

        // Assert
        verify(gameCopyRepository, times(1)).delete(gameCopy);
        verify(gameCopyRepository, times(1)).findGameCopyByGameCopyKey(any(GameCopyKey.class));
    }

    @Test
    public void testUpdateGameCopyStatus() {
        // Arrange
        UserAccount owner = new UserAccount("Bob", "1234", "djt@mail.com", AccountType.GAMEOWNER);
        Game game = new Game("UNO", "A card game", "Card Game");

        GameCopy gameCopy = new GameCopy(new GameCopyKey(owner, game), "In good shape");

        when(userAccountRepository.findUserAccountByUserAccountID(owner.getUserAccountID())).thenReturn(owner);
        when(gameService.getGameByTitle("UNO")).thenReturn(game);
        when(gameCopyRepository.findGameCopyByGameCopyKey(any(GameCopyKey.class))).thenReturn(gameCopy);
        when(gameCopyRepository.save(any(GameCopy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        gameCopyService.updateStatus("UNO", owner.getUserAccountID(), "DAMAGED");

        // Assert
        assertEquals("DAMAGED", gameCopy.getStatus().toString());
        verify(gameCopyRepository, times(1)).save(gameCopy);
    }

    @Test
    public void testUpdateGameCopyStatusWithInvalidStatus() {
        // Arrange
        UserAccount owner = new UserAccount("Bob", "1234", "djt@mail.com", AccountType.GAMEOWNER);
        Game game = new Game("UNO", "A card game", "Card Game");

        GameCopy gameCopy = new GameCopy(new GameCopyKey(owner, game), "In good shape");

        when(userAccountRepository.findUserAccountByUserAccountID(owner.getUserAccountID())).thenReturn(owner);
        when(gameService.getGameByTitle("UNO")).thenReturn(game);
        when(gameCopyRepository.findGameCopyByGameCopyKey(any(GameCopyKey.class))).thenReturn(gameCopy);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            gameCopyService.updateStatus("UNO", owner.getUserAccountID(), "WATER");
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Invalid game status: WATER", exception.getMessage());
    }

    @Test
    public void testGetGameCopiesByOwner() {
        // Arrange
        UserAccount owner = new UserAccount("Bob", "1234", "djt@mail.com", AccountType.GAMEOWNER);
        Game game1 = new Game("UNO", "A card game", "Card Game");
        Game game2 = new Game("Chess", "A board game", "Board Game");

        when(userAccountRepository.findUserAccountByUserAccountID(owner.getUserAccountID())).thenReturn(owner);
        when(gameService.getGameByTitle("UNO")).thenReturn(game1);
        when(gameService.getGameByTitle("Chess")).thenReturn(game2);

        GameCopy gameCopy1 = new GameCopy(new GameCopyKey(owner, game1), "In good shape");
        GameCopy gameCopy2 = new GameCopy(new GameCopyKey(owner, game2), "In excellent shape");

        GameCopyResponseDto gameCopyResponseDto1 = new GameCopyResponseDto(gameCopy1);
        GameCopyResponseDto gameCopyResponseDto2 = new GameCopyResponseDto(gameCopy2);

        List<GameCopy> gameCopies = Arrays.asList(gameCopy2, gameCopy1);

        when(gameCopyRepository.findAllByGameCopyKeyOwner(owner)).thenReturn(gameCopies);

        // Act
        List<GameCopyResponseDto> returnedGameCopies = gameCopyService.getUserGameCopies(owner.getUserAccountID());

        // Assert
        assertEquals(2, returnedGameCopies.size());

        assertTrue(returnedGameCopies.stream()
                .anyMatch(dto -> dto.getDescription().equals(gameCopyResponseDto1.getDescription()) &&
                        dto.getTitle().equals(gameCopyResponseDto1.getTitle()) &&
                        (dto.getOwner() == (gameCopyResponseDto1.getOwner()) &&
                                dto.getStatus().equals(gameCopyResponseDto1.getStatus()) &&
                                dto.getOwnerName().equals(gameCopyResponseDto1.getOwnerName()))));

        assertTrue(returnedGameCopies.stream()
                .anyMatch(dto -> dto.getDescription().equals(gameCopyResponseDto2.getDescription()) &&
                        dto.getTitle().equals(gameCopyResponseDto2.getTitle()) &&
                        (dto.getOwner() == (gameCopyResponseDto2.getOwner()) &&
                                dto.getStatus().equals(gameCopyResponseDto2.getStatus()) &&
                                dto.getOwnerName().equals(gameCopyResponseDto2.getOwnerName()))));
    }

}
