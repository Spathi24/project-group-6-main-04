package ca.mcgill.ecse321.boardgame.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ca.mcgill.ecse321.boardgame.dto.GameCopyCreationDto;
import ca.mcgill.ecse321.boardgame.dto.GameCopyResponseDto;
import ca.mcgill.ecse321.boardgame.exception.ResourceNotFoundException;
import ca.mcgill.ecse321.boardgame.model.AccountType;
import ca.mcgill.ecse321.boardgame.model.Game;
import ca.mcgill.ecse321.boardgame.model.GameCopy;
import ca.mcgill.ecse321.boardgame.model.GameStatus;
import ca.mcgill.ecse321.boardgame.model.GameCopy.GameCopyKey;
import ca.mcgill.ecse321.boardgame.model.UserAccount;
import ca.mcgill.ecse321.boardgame.repo.GameCopyRepository;
import ca.mcgill.ecse321.boardgame.repo.UserAccountRepository;
import ca.mcgill.ecse321.boardgame.repo.BorrowRequestRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

/**
 * Service class for managing game copies.
 */
@Service
public class GameCopyService {

    @Autowired
    private GameCopyRepository gameCopyRepository;

    @Autowired
    private GameService gameService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private BorrowRequestRepository borrowRequestRepository;

    /**
     * Creates a new game copy.
     *
     * @param gameCopyCreationDto the details of the game copy to be created
     * @return the created GameCopy
     */
    @Transactional
    public GameCopy createGameCopy(@Valid GameCopyCreationDto gameCopyCreationDto) {
        UserAccount owner = userAccountRepository.findUserAccountByUserAccountID(gameCopyCreationDto.getOwner());

        if (owner == null) {
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "UserAccount with ID " + gameCopyCreationDto.getOwner() + " not found");
        }

        if (owner.getAccountType() != AccountType.GAMEOWNER) {
            throw new ResourceNotFoundException(HttpStatus.FORBIDDEN, "Only game owners can own game copies");
        }

        Game game = gameService.getGameByTitle(gameCopyCreationDto.getTitle());

        if (game == null) {
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "Game with title " + gameCopyCreationDto.getTitle() + " not found");
        }

        GameCopyKey gameCopyKey = new GameCopyKey(owner, game);

        // Check if the user already owns a copy of the game
        GameCopy existingGameCopy = gameCopyRepository.findGameCopyByGameCopyKey(gameCopyKey);
        if (existingGameCopy != null) {
            throw new ResourceNotFoundException(HttpStatus.CONFLICT,
                    "User already owns a copy of the game " + game.getTitle() + ".");
        }

        GameCopy gameCopy = new GameCopy(gameCopyKey, gameCopyCreationDto.getDescription());
        GameCopy test = gameCopyRepository.save(gameCopy);
        return test;
    }

    /**
     * Updates the status of a game copy.
     *
     * @param title         the title of the game copy
     * @param userAccountId the ID of the user who owns the game copy
     * @param status        the new status of the game copy
     */
    @Transactional
    public void updateStatus(String title, long userAccountId, String status) {
        GameCopy gameCopy = getGameCopyByTitle(userAccountId, title);
        try {
            GameStatus gameStatus = GameStatus.valueOf(status.toUpperCase());
            gameCopy.setStatus(gameStatus);
            gameCopyRepository.save(gameCopy);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException(HttpStatus.BAD_REQUEST, "Invalid game status: " + status);
        }
    }

    /**
     * Retrieves all game copies for a user.
     *
     * @param userAccountId the ID of the user
     * @return a list of GameCopyResponseDto objects
     */
    @Transactional
    public List<GameCopyResponseDto> getUserGameCopies(long userAccountId) {
        UserAccount owner = userAccountRepository.findUserAccountByUserAccountID(userAccountId);
        List<GameCopy> gameCopies = gameCopyRepository.findAllByGameCopyKeyOwner(owner);
        return gameCopies.stream().map(GameCopyResponseDto::new).collect(Collectors.toList());
    }

    /**
     * Deletes a game copy.
     *
     * @param title         the title of the game copy
     * @param userAccountId the ID of the user who owns the game copy
     */
    @Transactional
    public void deleteGameCopy(String title, long userAccountId) {
        GameCopy gameCopy = getGameCopyByTitle(userAccountId, title);
        if (gameCopy.getStatus() == GameStatus.BORROWED) {
            throw new ResourceNotFoundException(HttpStatus.FORBIDDEN,
                    "GameCopy with title " + title + " is borrowed and cannot be deleted");
        }

        // Find and delete all borrow requests for this game copy
        UserAccount owner = userAccountRepository.findUserAccountByUserAccountID(userAccountId);
        Game game = gameService.getGameByTitle(title);
        borrowRequestRepository.deleteBorrowRequestsByOwnerAndGameTitle(owner, title);

        gameCopyRepository.delete(gameCopy);
    }

    /**
     * Updates the description of a game copy.
     *
     * @param title          the title of the game copy
     * @param userAccountId  the ID of the user who owns the game copy
     * @param newDescription the new description of the game copy
     */
    @Transactional
    public void updateDescription(String title, long userAccountId, String newDescription) {

        if (newDescription == null || newDescription.isEmpty()) {
            throw new ResourceNotFoundException(HttpStatus.BAD_REQUEST, "Description cannot be empty");
        }

        GameCopy gameCopy = getGameCopyByTitle(userAccountId, title);
        gameCopy.setDescription(newDescription);
        gameCopyRepository.save(gameCopy);
    }

    /**
     * Retrieves a game copy by title and user ID.
     *
     * @param userAccountId the ID of the user who owns the game copy
     * @param title         the title of the game copy
     * @return the GameCopy object
     */
    private GameCopy getGameCopyByTitle(long userAccountId, String title) {
        UserAccount owner = userAccountRepository.findUserAccountByUserAccountID(userAccountId);
        if (owner == null) {
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "UserAccount with ID " + userAccountId + " not found");
        }

        Game game = gameService.getGameByTitle(title);
        if (game == null) {
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND,
                    "Game with title " + title + " not found");
        }
        GameCopyKey gameCopyKey = new GameCopyKey(owner, game);
        return gameCopyRepository.findGameCopyByGameCopyKey(gameCopyKey);
    }

    /**
     * Retrieves all game copies.
     *
     * @return a list of all GameCopy objects
     */
    @Transactional
    public List<GameCopy> getAllGameCopies() {
        return (List<GameCopy>) gameCopyRepository.findAll();
    }
}
