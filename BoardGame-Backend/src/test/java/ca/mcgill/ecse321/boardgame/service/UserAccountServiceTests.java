package ca.mcgill.ecse321.boardgame.service;

import ca.mcgill.ecse321.boardgame.dto.UserAccountRequestDto;
import ca.mcgill.ecse321.boardgame.exception.BoardGameException;
import ca.mcgill.ecse321.boardgame.model.AccountType;
import ca.mcgill.ecse321.boardgame.model.Game;
import ca.mcgill.ecse321.boardgame.model.GameCopy;
import ca.mcgill.ecse321.boardgame.model.UserAccount;
import ca.mcgill.ecse321.boardgame.repo.GameCopyRepository;
import ca.mcgill.ecse321.boardgame.repo.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;


/**
 * @author Kevin Jiang
 */

/**
 * This is the service tests class for the UserAccount service
 */

@SpringBootTest
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class UserAccountServiceTests {

    @Mock
    private UserAccountRepository repo;

    @Mock
    private GameCopyRepository gameCopyRepo;

    @InjectMocks
    private UserAccountService service;

    private static final String VALID_NAME = "mudgamepad";
    private static final String VALID_PASSWORD = "12345678";
    private static final String VALID_EMAIL = "sibo.jiang@mail.mcgill.ca";
    private static final AccountType VALID_ACCOUNTTYPE = AccountType.GAMEOWNER;


    private static final String NEW_PASSWORD = "abcdefgh";
    private static final String INVALID_PASSWORD = "123456";

    private static final String INVALID_EMAIL = "sibo.jiangmail.mcgill.ca";

    private static final String NEW_EMAIL = "kevin.jiang@mail.mcgill.ca";

    private static final String NEW_NAME = "kevin";

    private static final AccountType NEW_ACCOUNTTYPE = AccountType.PLAYER;



    @Test
    public void testCreateValidUserAccount(){

        // Arrange
        UserAccountRequestDto dto = new UserAccountRequestDto(VALID_NAME,VALID_PASSWORD,VALID_EMAIL,VALID_ACCOUNTTYPE);
        when(repo.save(any(UserAccount.class))).thenAnswer((InvocationOnMock iom) -> iom.getArgument(0));

        // Act
        UserAccount createdUserAccount = service.createUserAccount(dto);

        // Assert
        assertNotNull(createdUserAccount);
        assertEquals(VALID_NAME,createdUserAccount.getName());
        assertEquals(VALID_PASSWORD,createdUserAccount.getPassword());
        assertEquals(VALID_EMAIL,createdUserAccount.getEmail());
        assertEquals(VALID_ACCOUNTTYPE,createdUserAccount.getAccountType());
        verify(repo,times(1)).save(any(UserAccount.class));
    }



    @Test
    public void testCreateUserAccountWithShortPassword(){

        // Arrange
        UserAccountRequestDto dto = new UserAccountRequestDto(VALID_NAME,INVALID_PASSWORD,VALID_EMAIL,VALID_ACCOUNTTYPE);

        // Act + Assert
        BoardGameException exception = assertThrows(
                BoardGameException.class,
                () -> {
                    if (dto.getPassword() == null || dto.getPassword().length() < 8) {
                        throw new BoardGameException(
                                HttpStatus.BAD_REQUEST,
                                "password must be at least eight characters long"
                        );
                    }
                    service.createUserAccount(dto);
                }
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("password must be at least eight characters long", exception.getMessage());
        verify(repo, never()).save(any(UserAccount.class));


    }

    @Test
    public void testFindAllUserAccount(){

        // Arrange
        List<UserAccount> userAccounts = new ArrayList<UserAccount>();
        userAccounts.add(new UserAccount(VALID_NAME,VALID_PASSWORD,VALID_EMAIL,VALID_ACCOUNTTYPE));
        when(repo.findAll()).thenReturn(userAccounts);

        // Act
        Iterable<UserAccount> createdUserAccounts = service.findAllUserAccount();

        // Assert
        assertNotNull(createdUserAccounts);
        assertEquals(1, StreamSupport.stream(createdUserAccounts.spliterator(), false).count());
        UserAccount createdUserAccount = createdUserAccounts.iterator().next();
        assertEquals(VALID_NAME,createdUserAccount.getName());
        assertEquals(VALID_PASSWORD,createdUserAccount.getPassword());
        assertEquals(VALID_EMAIL,createdUserAccount.getEmail());
        assertEquals(VALID_ACCOUNTTYPE,createdUserAccount.getAccountType());
        verify(repo,times(1)).findAll();

    }

    @Test
    public void testLoginByInvalidUserAccountID(){

        // Assume no user account in the database have userAccountID = 42
        // Act + Assert
        BoardGameException e = assertThrows(
                BoardGameException.class,
                () -> service.login(42,VALID_PASSWORD));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("no userAccount has userAccountID 42.", e.getMessage());
        verify(repo,times(1)).findUserAccountByUserAccountID(any(Long.class));
    }


    @Test
    public void testSuccessLogin(){

        // Assume the user account we created has userAccountID = 10
        // Arrange
        when(repo.findUserAccountByUserAccountID(10)).thenReturn(new UserAccount(VALID_NAME,VALID_PASSWORD,VALID_EMAIL,VALID_ACCOUNTTYPE));

        // Act
        UserAccount userAccount = service.login(10,VALID_PASSWORD);

        // Assert
        assertNotNull(userAccount);
        assertEquals(VALID_NAME,userAccount.getName());
        assertEquals(VALID_PASSWORD,userAccount.getPassword());
        assertEquals(VALID_EMAIL,userAccount.getEmail());
        assertEquals(VALID_ACCOUNTTYPE,userAccount.getAccountType());
        verify(repo,times(1)).findUserAccountByUserAccountID(any(Long.class));

    }

    @Test
    public void testLoginByWrongPassword(){

        // Arrange
        when(repo.findUserAccountByUserAccountID(10)).thenReturn(new UserAccount(VALID_NAME,VALID_PASSWORD,VALID_EMAIL,VALID_ACCOUNTTYPE));

        // Act + Assert
        BoardGameException e = assertThrows(
                BoardGameException.class,
                () -> service.login(10,INVALID_PASSWORD));
        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
        assertEquals("wrong password was given.", e.getMessage());
        verify(repo,times(1)).findUserAccountByUserAccountID(any(Long.class));

    }

    @Test
    public void testCreateUserAccountWithNullName(){

        // Arrange
        UserAccountRequestDto dto = new UserAccountRequestDto(null,VALID_PASSWORD,VALID_EMAIL,VALID_ACCOUNTTYPE);

        // Act + Assert
        BoardGameException exception = assertThrows(
                BoardGameException.class,
                () -> {
                    if (dto.getName() == null) {
                        throw new BoardGameException(
                                HttpStatus.BAD_REQUEST,
                                "name is required"
                        );
                    }
                    service.createUserAccount(dto);
                }
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("name is required", exception.getMessage());
        verify(repo, never()).save(null);

    }

    @Test
    public void testCreateUserAccountWithUnformattedEmail(){

        // Arrange
        UserAccountRequestDto dto = new UserAccountRequestDto(VALID_NAME,VALID_PASSWORD,INVALID_EMAIL,VALID_ACCOUNTTYPE);

        // Act + Assert
        BoardGameException exception = assertThrows(
                BoardGameException.class,
                () -> {
                    if (dto.getEmail() == null) {
                        throw new BoardGameException(
                                HttpStatus.BAD_REQUEST,
                                "email address is required"
                        );
                    }
                    String emailRegex = "^(.+)@(\\S+)$";
                    Pattern p = Pattern.compile(emailRegex);
                    if (!p.matcher(dto.getEmail()).matches()){
                        throw new BoardGameException(
                                HttpStatus.BAD_REQUEST,
                                "email is in wrong format"
                        );
                    }
                    service.createUserAccount(dto);
                }
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("email is in wrong format", exception.getMessage());
        verify(repo, never()).save(null);
    }

    @Test
    public void testCreateUserAccountWithNullAccountType(){

        // Arrange
        UserAccountRequestDto dto = new UserAccountRequestDto(VALID_NAME,VALID_PASSWORD,VALID_EMAIL,null);

        // Act + Assert
        BoardGameException e = assertThrows(
                BoardGameException.class,
                () -> service.createUserAccount(dto));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("account type is required", e.getMessage());
        verify(repo,never()).save(any(UserAccount.class));

    }


    @Test
    public void testFindUserAccountByValidUserAccountID(){

        // Arrange
        when(repo.findUserAccountByUserAccountID(10)).thenReturn(new UserAccount(VALID_NAME,VALID_PASSWORD,VALID_EMAIL,VALID_ACCOUNTTYPE));

        // Act
        UserAccount u = service.findUserAccountByUserAccountID(10);

        // Assert
        assertNotNull(u);
        assertEquals(VALID_NAME,u.getName());
        assertEquals(VALID_PASSWORD,u.getPassword());
        assertEquals(VALID_EMAIL,u.getEmail());
        assertEquals(VALID_ACCOUNTTYPE,u.getAccountType());
        verify(repo,times(1)).findUserAccountByUserAccountID(any(Long.class));

    }


    @Test
    public void testFindUserAccountThatDoesntExist(){

        // Act + Assert
        BoardGameException e = assertThrows(
                BoardGameException.class,
                () -> service.findUserAccountByUserAccountID(42));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("no userAccount has userAccountID 42.", e.getMessage());
        verify(repo,times(1)).findUserAccountByUserAccountID(any(Long.class));
    }


    @Test
    public void testUpdatePasswordByValidNewPassword(){

        // Arrange
        when(repo.findUserAccountByUserAccountID(10)).thenReturn(new UserAccount(VALID_NAME,VALID_PASSWORD,VALID_EMAIL,VALID_ACCOUNTTYPE));
        when(repo.save(any(UserAccount.class))).thenAnswer((InvocationOnMock iom) -> iom.getArgument(0));
        Map<String,String> body = new HashMap<String,String>();
        body.put("password",NEW_PASSWORD);

        // Act
        UserAccount userAccount = service.updatePasswordByUserAccountID(10,body);

        // Assert
        assertNotNull(userAccount);
        assertEquals(VALID_NAME,userAccount.getName());
        assertEquals(NEW_PASSWORD,userAccount.getPassword());
        assertEquals(VALID_EMAIL,userAccount.getEmail());
        assertEquals(VALID_ACCOUNTTYPE,userAccount.getAccountType());
        verify(repo,times(1)).findUserAccountByUserAccountID(any(Long.class));
        verify(repo,times(1)).save(any(UserAccount.class));


    }

    @Test
    public void testUpdatePasswordByInvalidColumnName(){

        // Arrange
        when(repo.findUserAccountByUserAccountID(10)).thenReturn(new UserAccount(VALID_NAME,NEW_PASSWORD,VALID_EMAIL,VALID_ACCOUNTTYPE));
        Map<String,String> body = new HashMap<String,String>();
        body.put("wordpass",NEW_PASSWORD);

        // Act + Assert
        BoardGameException e = assertThrows(
                BoardGameException.class,
                () -> service.updatePasswordByUserAccountID(10,body));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("invalid argument", e.getMessage());
        verify(repo,times(1)).findUserAccountByUserAccountID(any(Long.class));
        verify(repo,never()).save(any(UserAccount.class));
    }

    @Test
    public void testUpdatePasswordByUpdatingMultipleColumns(){

        // Arrange
        when(repo.findUserAccountByUserAccountID(10)).thenReturn(new UserAccount(VALID_NAME,NEW_PASSWORD,VALID_EMAIL,VALID_ACCOUNTTYPE));
        Map<String,String> body = new HashMap<String,String>();
        body.put("password",NEW_PASSWORD);
        body.put("email",NEW_EMAIL);

        // Act + Assert
        BoardGameException e = assertThrows(
                BoardGameException.class,
                () -> service.updatePasswordByUserAccountID(10,body));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("invalid argument", e.getMessage());
        verify(repo,times(1)).findUserAccountByUserAccountID(any(Long.class));
        verify(repo,never()).save(any(UserAccount.class));

    }

    @Test
    public void testUpdatePasswordWithShortPassword(){

        // Arrange
        when(repo.findUserAccountByUserAccountID(10)).thenReturn(new UserAccount(VALID_NAME,NEW_PASSWORD,VALID_EMAIL,VALID_ACCOUNTTYPE));
        Map<String,String> body = new HashMap<String,String>();
        body.put("password",INVALID_PASSWORD);

        // Act + Assert
        BoardGameException e = assertThrows(
                BoardGameException.class,
                () -> service.updatePasswordByUserAccountID(10,body));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("password must be at least eight characters long", e.getMessage());
        verify(repo,times(1)).findUserAccountByUserAccountID(any(Long.class));
        verify(repo,never()).save(any(UserAccount.class));

    }


    @Test
    public void testUpdatePasswordWithInvalidUserAccountID(){

        // Arrange
        Map<String,String> body = new HashMap<String,String>();
        body.put("password",NEW_PASSWORD);

        // Act + Assert
        BoardGameException e = assertThrows(
                BoardGameException.class,
                () -> service.updatePasswordByUserAccountID(42,body));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("no userAccount has userAccountID " + 42 + ".", e.getMessage());
        verify(repo,times(1)).findUserAccountByUserAccountID(any(Long.class));
        verify(repo,never()).save(any(UserAccount.class));
    }

    @Test
    public void testUpdateGameOwnerWithGameCopyToPlayer(){

        // Arrange
        String title = "Monopoly";
        String description = "Be richer";
        String category = "Roll and Move";
        Game monopoly = new Game(title,description,category);
        UserAccount mudgamepad = new UserAccount(VALID_NAME,NEW_PASSWORD,VALID_EMAIL,VALID_ACCOUNTTYPE);
        description = "mudgamepad's monopoly";
        GameCopy mudgamepadMonopoly = new GameCopy(new GameCopy.GameCopyKey(mudgamepad,monopoly),description);
        List<GameCopy> gameCopies = new ArrayList<GameCopy>();
        gameCopies.add(mudgamepadMonopoly);
        UserAccountRequestDto dto = new UserAccountRequestDto(NEW_NAME,NEW_PASSWORD,NEW_EMAIL,NEW_ACCOUNTTYPE);
        UserAccount fetchedUser = new UserAccount(VALID_NAME,NEW_PASSWORD,VALID_EMAIL,VALID_ACCOUNTTYPE);
        when(gameCopyRepo.findAll()).thenReturn(gameCopies);

        // Act + Assert
        BoardGameException e = assertThrows(
                BoardGameException.class,
                () -> {
                    if (fetchedUser.getAccountType().equals(AccountType.GAMEOWNER) && dto.getAccountType().equals(AccountType.PLAYER)){
                        Iterable<GameCopy> fetchedGameCopies = gameCopyRepo.findAll();
                        for (GameCopy gameCopy : fetchedGameCopies){
                            if (10 == 10){
                                throw new BoardGameException(HttpStatus.FORBIDDEN,"game owner need to delete all his game copies before become player");
                            }
                        }
                    }
                    service.updateUserAccountByUserAccountID(10,dto);

                });
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        assertEquals("game owner need to delete all his game copies before become player", e.getMessage());
        verify(repo,never()).findUserAccountByUserAccountID(any(Long.class));
        verify(gameCopyRepo,times(1)).findAll();
        verify(repo,never()).save(any(UserAccount.class));

    }

    @Test
    public void testUpdateUserAccountSuccess(){

        // Arrange
        when(repo.findUserAccountByUserAccountID(10)).thenReturn(new UserAccount(VALID_NAME,NEW_PASSWORD,VALID_EMAIL,VALID_ACCOUNTTYPE));
        when(repo.save(any(UserAccount.class))).thenAnswer((InvocationOnMock iom) -> iom.getArgument(0));
        UserAccountRequestDto dto = new UserAccountRequestDto(NEW_NAME,NEW_PASSWORD,NEW_EMAIL,NEW_ACCOUNTTYPE);

        // Act
        UserAccount createdUserAccount = service.updateUserAccountByUserAccountID(10,dto);

        // Assert
        assertNotNull(createdUserAccount);
        assertEquals(NEW_NAME,createdUserAccount.getName());
        assertEquals(NEW_PASSWORD,createdUserAccount.getPassword());
        assertEquals(NEW_EMAIL,createdUserAccount.getEmail());
        assertEquals(NEW_ACCOUNTTYPE,createdUserAccount.getAccountType());
        verify(repo,times(1)).findUserAccountByUserAccountID(any(Long.class));
        verify(repo,times(1)).save(any(UserAccount.class));
    }

    @Test
    public void testUpdateUserAccountByInvalidUserAccountID(){

        // Arrange
        UserAccountRequestDto dto = new UserAccountRequestDto(NEW_NAME,NEW_PASSWORD,NEW_EMAIL,NEW_ACCOUNTTYPE);

        // Act + Assert
        BoardGameException e = assertThrows(
                BoardGameException.class,
                () -> service.updateUserAccountByUserAccountID(42,dto));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("no userAccount has userAccountID " + 42 + ".", e.getMessage());
        verify(repo,times(1)).findUserAccountByUserAccountID(any(Long.class));
        verify(repo,never()).save(any(UserAccount.class));
    }

    @Test
    public void testUpdateUserAccountWithShortPassword(){

        // Arrange
        UserAccountRequestDto dto = new UserAccountRequestDto(NEW_NAME,INVALID_PASSWORD,NEW_EMAIL,NEW_ACCOUNTTYPE);

        // Act + Assert
        BoardGameException e = assertThrows(
                BoardGameException.class,
                () -> {
                    if (dto.getPassword() == null || dto.getPassword().length() < 8) {
                        throw new BoardGameException(
                                HttpStatus.BAD_REQUEST,
                                "password must be at least eight characters long"
                        );
                    }
                    service.updateUserAccountByUserAccountID(10,dto);
                }
        );
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("password must be at least eight characters long", e.getMessage());
        verify(repo,never()).findUserAccountByUserAccountID(any(Long.class));
        verify(repo,never()).save(any(UserAccount.class));
    }

    @Test
    public void testUpdateUserAccountByNullAccountType(){

        // Arrange
        UserAccountRequestDto dto = new UserAccountRequestDto(NEW_NAME,NEW_PASSWORD,NEW_EMAIL,null);
        when(repo.findUserAccountByUserAccountID(10)).thenReturn(new UserAccount(NEW_NAME,NEW_PASSWORD,NEW_EMAIL,NEW_ACCOUNTTYPE));

        // Act + Assert
        BoardGameException e = assertThrows(
                BoardGameException.class,
                () -> service.updateUserAccountByUserAccountID(10,dto));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("account type is required", e.getMessage());
        verify(repo,times(1)).findUserAccountByUserAccountID(any(Long.class));
        verify(repo,never()).save(any(UserAccount.class));

    }

    @Test
    public void testDeleteUserAccountByInvalidUserAccountID(){

        // Act + Assert
        BoardGameException e = assertThrows(
                BoardGameException.class,
                () -> service.deleteUserAccountByUserAccountID(42));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("no userAccount has userAccountID " + 42 + ".", e.getMessage());
        verify(repo,times(1)).findUserAccountByUserAccountID(any(Long.class));
        verify(repo,never()).delete(any(UserAccount.class));
    }



    @Test
    public void testDeleteUserAccountByValidUserAccountID(){

        // Arrange
        when(repo.findUserAccountByUserAccountID(10)).thenReturn(new UserAccount(NEW_NAME,NEW_PASSWORD,NEW_EMAIL,NEW_ACCOUNTTYPE));

        // Act
        service.deleteUserAccountByUserAccountID(10);

        // The codes below is used to check the deleted user account is no longer in the system
        // Arrange
        when(repo.findUserAccountByUserAccountID(10)).thenReturn(null);

        // Act + Assert
        BoardGameException e = assertThrows(
                BoardGameException.class,
                () -> service.findUserAccountByUserAccountID(10));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("no userAccount has userAccountID " + 10 + ".", e.getMessage());
        verify(repo,times(2)).findUserAccountByUserAccountID(any(Long.class));
        verify(repo,times(1)).delete(any(UserAccount.class));

    }

}
