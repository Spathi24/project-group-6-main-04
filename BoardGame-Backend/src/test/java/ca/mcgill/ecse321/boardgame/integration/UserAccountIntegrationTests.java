package ca.mcgill.ecse321.boardgame.integration;

import ca.mcgill.ecse321.boardgame.dto.ErrorDto;
import ca.mcgill.ecse321.boardgame.dto.UserAccountListDto;
import ca.mcgill.ecse321.boardgame.dto.UserAccountRequestDto;
import ca.mcgill.ecse321.boardgame.dto.UserAccountResponseDto;
import ca.mcgill.ecse321.boardgame.model.AccountType;
import ca.mcgill.ecse321.boardgame.model.Game;
import ca.mcgill.ecse321.boardgame.model.GameCopy;
import ca.mcgill.ecse321.boardgame.repo.GameCopyRepository;
import ca.mcgill.ecse321.boardgame.repo.GameRepository;
import ca.mcgill.ecse321.boardgame.repo.UserAccountRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Kevin Jiang
 */

/**
 * This is the integration tests class for the UserAccount controller
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserAccountIntegrationTests {

    @Autowired
    private TestRestTemplate client;

    @Autowired
    private UserAccountRepository userAccountRepo;

    @Autowired
    private GameRepository gameRepo;

    @Autowired
    private GameCopyRepository gameCopyRepo;

    private static long createdUserAccountID;

    private static final String VALID_NAME = "mudgamepad";
    private static final String VALID_PASSWORD = "12345678";
    private static final String VALID_EMAIL = "sibo.jiang@mail.mcgill.ca";
    private static final AccountType VALID_ACCOUNTTYPE = AccountType.GAMEOWNER;

    private static final String INVALID_PASSWORD = "123456";

    private static final String INVALID_EMAIL = "sibo.jiangmail.mcgill.ca";

    private static final String NEW_NAME = "kevin";

    private static final String NEW_PASSWORD = "abcdefgh";

    private static final String NEW_EMAIL = "kevin.jiang@mail.mcgill.ca";

    private static final AccountType NEW_ACCOUNTTYPE = AccountType.PLAYER;

    // An ID such that no user account in the database have this ID
    private static long INVALID_USERACCOUNTID;

    @BeforeAll
    @AfterAll
    public void clean(){
        gameCopyRepo.deleteAll();
        gameRepo.deleteAll();
        userAccountRepo.deleteAll();
    }

    @Test
    @Order(0)
    public void testCreateValidUserAccount() throws URISyntaxException {

        // Arrange
        UserAccountRequestDto body = new UserAccountRequestDto(VALID_NAME,VALID_PASSWORD,VALID_EMAIL,VALID_ACCOUNTTYPE);

        // Act
        ResponseEntity<UserAccountResponseDto> response = client.postForEntity("/UserAccount",body, UserAccountResponseDto.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED,response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getUserAccountID() > 0,"the user account id should be a positive int");
        createdUserAccountID = response.getBody().getUserAccountID(); // Record the ID of this user
        INVALID_USERACCOUNTID = createdUserAccountID + 10; // Create an ID such that no user account in the database have this ID
        assertEquals(body.getName(),response.getBody().getName());
        assertEquals(body.getEmail(),response.getBody().getEmail());
        assertEquals(body.getAccountType(),response.getBody().getAccountType());

        // The codes below is used to check whether the correct password is recorded, so login into the created user account
        // Arrange
        String url = String.format("/UserAccount/login/%d/%s",createdUserAccountID,VALID_PASSWORD);
        URI uri = new URI(url);

        // Act
        response = client.exchange(uri,HttpMethod.POST,HttpEntity.EMPTY, UserAccountResponseDto.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdUserAccountID,response.getBody().getUserAccountID());
        assertEquals(VALID_NAME,response.getBody().getName());
        assertEquals(VALID_EMAIL,response.getBody().getEmail());
        assertEquals(VALID_ACCOUNTTYPE,response.getBody().getAccountType());
    }


    @Test
    @Order(1)
    public void testCreateUserAccountWithShortPassword(){

        // Arrange
        UserAccountRequestDto body = new UserAccountRequestDto(VALID_NAME,INVALID_PASSWORD,VALID_EMAIL,VALID_ACCOUNTTYPE);

        // Act
        ResponseEntity<ErrorDto> response = client.postForEntity("/UserAccount",body, ErrorDto.class);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode());
        assertNotNull(response.getBody());
        assertIterableEquals(List.of("password must be at least eight characters long"),response.getBody().getErrors());
    }


    @Test
    @Order(2)
    public void testFindAllUserAccount() throws URISyntaxException {

        // Arrange
        String url = "/UserAccount";
        URI uri = new URI(url);

        // Act
        ResponseEntity<UserAccountListDto> response = client.exchange(uri, HttpMethod.GET, HttpEntity.EMPTY, UserAccountListDto.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1,response.getBody().getUserAccounts().size());
        assertEquals(createdUserAccountID,response.getBody().getUserAccounts().get(0).getUserAccountID());
        assertEquals(VALID_NAME,response.getBody().getUserAccounts().get(0).getName());
        assertEquals(VALID_EMAIL,response.getBody().getUserAccounts().get(0).getEmail());
        assertEquals(VALID_ACCOUNTTYPE,response.getBody().getUserAccounts().get(0).getAccountType());

    }

    @Test
    @Order(3)
    public void testLoginByInvalidUserAccountID() throws URISyntaxException {

        // Arrange
        String url = String.format("/UserAccount/login/%d/%s",INVALID_USERACCOUNTID,NEW_PASSWORD);
        URI uri = new URI(url);

        // Act
        ResponseEntity<ErrorDto> response = client.exchange(uri,HttpMethod.POST,HttpEntity.EMPTY, ErrorDto.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND,response.getStatusCode());
        ErrorDto errorBody = response.getBody();
        assertNotNull(errorBody);
        assertEquals(1, errorBody.getErrors().size());
        assertEquals("no userAccount has userAccountID " + INVALID_USERACCOUNTID + ".", errorBody.getErrors().get(0));
    }

    @Test
    @Order(4)
    public void testSuccessLogin() throws URISyntaxException {
        // Arrange
        String url = String.format("/UserAccount/login/%d/%s",createdUserAccountID,VALID_PASSWORD);
        URI uri = new URI(url);

        // Act
        ResponseEntity<UserAccountResponseDto> response = client.exchange(uri,HttpMethod.POST,HttpEntity.EMPTY, UserAccountResponseDto.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdUserAccountID,response.getBody().getUserAccountID());
        assertEquals(VALID_NAME,response.getBody().getName());
        assertEquals(VALID_EMAIL,response.getBody().getEmail());
        assertEquals(VALID_ACCOUNTTYPE,response.getBody().getAccountType());
    }


    @Test
    @Order(5)
    public void testLoginByWrongPassword() throws URISyntaxException {
        // Arrange
        String url = String.format("/UserAccount/login/%d/%s",createdUserAccountID,NEW_PASSWORD);
        URI uri = new URI(url);

        // Act
        ResponseEntity<ErrorDto> response = client.exchange(uri,HttpMethod.POST,HttpEntity.EMPTY, ErrorDto.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED,response.getStatusCode());
        ErrorDto errorBody = response.getBody();
        assertNotNull(errorBody);
        assertEquals(1, errorBody.getErrors().size());
        assertEquals("wrong password was given.", errorBody.getErrors().get(0));
    }

    @Test
    @Order(6)
    public void testCreateUserAccountWithNullName(){
        // Arrange
        UserAccountRequestDto body = new UserAccountRequestDto(null,VALID_PASSWORD,VALID_EMAIL,VALID_ACCOUNTTYPE);

        // Act
        ResponseEntity<ErrorDto> response = client.postForEntity("/UserAccount",body, ErrorDto.class);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode());
        assertNotNull(response.getBody());
        assertIterableEquals(List.of("name is required"),response.getBody().getErrors());
    }

    @Test
    @Order(7)
    public void testCreateUserAccountWithUnformattedEmail(){
        // Arrange
        UserAccountRequestDto body = new UserAccountRequestDto(VALID_NAME,VALID_PASSWORD,INVALID_EMAIL,VALID_ACCOUNTTYPE);

        // Act
        ResponseEntity<ErrorDto> response = client.postForEntity("/UserAccount",body, ErrorDto.class);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode());
        assertNotNull(response.getBody());
        assertIterableEquals(List.of("email is in wrong format"),response.getBody().getErrors());
    }

    @Test
    @Order(8)
    public void testCreateUserAccountWithNullAccountType(){
        // Arrange
        UserAccountRequestDto body = new UserAccountRequestDto(VALID_NAME,VALID_PASSWORD,VALID_EMAIL,null);

        // Act
        ResponseEntity<ErrorDto> response = client.postForEntity("/UserAccount",body, ErrorDto.class);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode());
        assertNotNull(response.getBody());
        assertIterableEquals(List.of("account type is required"),response.getBody().getErrors());
    }


    @Test
    @Order(9)
    public void testFindUserAccountByValidUserAccountID(){

        // Arrange
        String url = String.format("/UserAccount/%d",createdUserAccountID);

        // Act
        ResponseEntity<UserAccountResponseDto> response = client.getForEntity(url, UserAccountResponseDto.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdUserAccountID,response.getBody().getUserAccountID());
        assertEquals(VALID_NAME,response.getBody().getName());
        assertEquals(VALID_EMAIL,response.getBody().getEmail());
        assertEquals(VALID_ACCOUNTTYPE,response.getBody().getAccountType());

    }

    @Test
    @Order(10)
    public void testFindUserAccountByInvalidUserAccountID(){

        // Arrange
        String url = String.format("/UserAccount/%d",INVALID_USERACCOUNTID);

        // Act
        ResponseEntity<ErrorDto> response = client.getForEntity(url, ErrorDto.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorDto body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.getErrors().size());
        assertEquals("no userAccount has userAccountID " + INVALID_USERACCOUNTID + ".", body.getErrors().get(0));

    }


    @Test
    @Order(11)
    public void testUpdatePasswordByValidNewPassword() throws URISyntaxException {

        // Arrange
        Map<String,String> body = new HashMap<String,String>();
        body.put("password",NEW_PASSWORD);
        String url = String.format("/UserAccount/%d/password",createdUserAccountID);
        URI uri = new URI(url);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Map<String,String>> httpEntity = new HttpEntity<>(body,headers);

        // Act
        ResponseEntity<UserAccountResponseDto> response = client.exchange(uri, HttpMethod.PATCH,httpEntity, UserAccountResponseDto.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdUserAccountID,response.getBody().getUserAccountID());
        assertEquals(VALID_NAME,response.getBody().getName());
        assertEquals(VALID_EMAIL,response.getBody().getEmail());
        assertEquals(VALID_ACCOUNTTYPE,response.getBody().getAccountType());

        // The code below is used to check whether the password is updated, so login the user account that was just updated
        // Arrange
        url = String.format("/UserAccount/login/%d/%s",createdUserAccountID,NEW_PASSWORD);
        uri = new URI(url);

        // Act
        response = client.exchange(uri,HttpMethod.POST,HttpEntity.EMPTY, UserAccountResponseDto.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdUserAccountID,response.getBody().getUserAccountID());
        assertEquals(VALID_NAME,response.getBody().getName());
        assertEquals(VALID_EMAIL,response.getBody().getEmail());
        assertEquals(VALID_ACCOUNTTYPE,response.getBody().getAccountType());
    }

    @Test
    @Order(12)
    public void testUpdatePasswordByInvalidColumnName() throws URISyntaxException {

        // Arrange
        Map<String,String> body = new HashMap<String,String>();
        body.put("wordpass",NEW_PASSWORD);
        String url = String.format("/UserAccount/%d/password",createdUserAccountID);
        URI uri = new URI(url);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Map<String,String>> httpEntity = new HttpEntity<>(body,headers);

        // Act
        ResponseEntity<ErrorDto> response = client.exchange(uri, HttpMethod.PATCH,httpEntity, ErrorDto.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorDto errorBody = response.getBody();
        assertNotNull(errorBody);
        assertEquals(1, errorBody.getErrors().size());
        assertEquals("invalid argument", errorBody.getErrors().get(0));

    }

    @Test
    @Order(13)
    public void testUpdatePasswordByUpdatingMultipleColumns() throws URISyntaxException{

        // Arrange
        Map<String,String> body = new HashMap<String,String>();
        body.put("password",NEW_PASSWORD);
        body.put("email",NEW_EMAIL);
        String url = String.format("/UserAccount/%d/password",createdUserAccountID);
        URI uri = new URI(url);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Map<String,String>> httpEntity = new HttpEntity<>(body,headers);

        // Act
        ResponseEntity<ErrorDto> response = client.exchange(uri, HttpMethod.PATCH,httpEntity, ErrorDto.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorDto errorBody = response.getBody();
        assertNotNull(errorBody);
        assertEquals(1, errorBody.getErrors().size());
        assertEquals("invalid argument", errorBody.getErrors().get(0));
    }

    @Test
    @Order(14)
    public void testUpdatePasswordWithShortPassword() throws URISyntaxException{

        // Arrange
        Map<String,String> body = new HashMap<String,String>();
        body.put("password",INVALID_PASSWORD);
        String url = String.format("/UserAccount/%d/password",createdUserAccountID);
        URI uri = new URI(url);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Map<String,String>> httpEntity = new HttpEntity<>(body,headers);

        // Act
        ResponseEntity<ErrorDto> response = client.exchange(uri, HttpMethod.PATCH,httpEntity, ErrorDto.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorDto errorBody = response.getBody();
        assertNotNull(errorBody);
        assertEquals(1, errorBody.getErrors().size());
        assertEquals("password must be at least eight characters long", errorBody.getErrors().get(0));
    }

    @Test
    @Order(15)
    public void testUpdatePasswordWithInvalidUserAccountID() throws URISyntaxException{

        // Arrange
        Map<String,String> body = new HashMap<String,String>();
        body.put("password",VALID_PASSWORD);
        String url = String.format("/UserAccount/%d/password",INVALID_USERACCOUNTID);
        URI uri = new URI(url);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Map<String,String>> httpEntity = new HttpEntity<>(body,headers);

        // Act
        ResponseEntity<ErrorDto> response = client.exchange(uri, HttpMethod.PATCH,httpEntity, ErrorDto.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorDto errorBody = response.getBody();
        assertNotNull(errorBody);
        assertEquals(1, errorBody.getErrors().size());
        assertEquals("no userAccount has userAccountID " + INVALID_USERACCOUNTID + ".", errorBody.getErrors().get(0));
    }


    @Test
    @Order(16)
    public void testUpdateGameOwnerWithGameCopyToPlayer() throws URISyntaxException {

        // Arrange
        String title = "Monopoly";
        String description = "Be richer";
        String category = "Roll and Move";
        Game monopoly = new Game(title,description,category);
        monopoly = gameRepo.save(monopoly);
        description = "mudgamepad's monopoly";
        GameCopy mudgamepadMonopoly = new GameCopy(new GameCopy.GameCopyKey(userAccountRepo.findUserAccountByUserAccountID(createdUserAccountID),monopoly),description);
        mudgamepadMonopoly = gameCopyRepo.save(mudgamepadMonopoly);
        UserAccountRequestDto body = new UserAccountRequestDto(NEW_NAME,NEW_PASSWORD,NEW_EMAIL,NEW_ACCOUNTTYPE);
        String url = String.format("/UserAccount/%d",createdUserAccountID);
        URI uri = new URI(url);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<UserAccountRequestDto> httpEntity = new HttpEntity<>(body,headers);

        // Act
        ResponseEntity<ErrorDto> response = client.exchange(uri, HttpMethod.PUT,httpEntity,ErrorDto.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ErrorDto errorBody = response.getBody();
        assertNotNull(errorBody);
        assertEquals(1, errorBody.getErrors().size());
        assertEquals("game owner need to delete all his game copies before become player", errorBody.getErrors().get(0));

        // Clean
        gameCopyRepo.deleteAll();
        gameRepo.deleteAll();
    }


    @Test
    @Order(17)
    public void testUpdateUserAccountSuccess() throws URISyntaxException {

        // Arrange
        UserAccountRequestDto body = new UserAccountRequestDto(NEW_NAME,NEW_PASSWORD,NEW_EMAIL,NEW_ACCOUNTTYPE);
        String url = String.format("/UserAccount/%d",createdUserAccountID);
        URI uri = new URI(url);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<UserAccountRequestDto> httpEntity = new HttpEntity<>(body,headers);

        // Act
        ResponseEntity<UserAccountResponseDto> response = client.exchange(uri, HttpMethod.PUT,httpEntity, UserAccountResponseDto.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdUserAccountID,response.getBody().getUserAccountID());
        assertEquals(NEW_NAME,response.getBody().getName());
        assertEquals(NEW_EMAIL,response.getBody().getEmail());
        assertEquals(NEW_ACCOUNTTYPE,response.getBody().getAccountType());

    }

    @Test
    @Order(18)
    public void testUpdateUserAccountByInvalidUserAccountID() throws URISyntaxException {

        // Arrange
        UserAccountRequestDto body = new UserAccountRequestDto(NEW_NAME,NEW_PASSWORD,NEW_EMAIL,NEW_ACCOUNTTYPE);
        String url = String.format("/UserAccount/%d",INVALID_USERACCOUNTID);
        URI uri = new URI(url);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<UserAccountRequestDto> httpEntity = new HttpEntity<>(body,headers);

        // Act
        ResponseEntity<ErrorDto> response = client.exchange(uri, HttpMethod.PUT,httpEntity,ErrorDto.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorDto errorBody = response.getBody();
        assertNotNull(errorBody);
        assertEquals(1, errorBody.getErrors().size());
        assertEquals("no userAccount has userAccountID " + INVALID_USERACCOUNTID + ".", errorBody.getErrors().get(0));


    }


    @Test
    @Order(19)
    public void testUpdateUserAccountWithShortPassword() throws URISyntaxException {

        // Arrange
        UserAccountRequestDto body = new UserAccountRequestDto(NEW_NAME,INVALID_PASSWORD,NEW_EMAIL,NEW_ACCOUNTTYPE);
        String url = String.format("/UserAccount/%d",createdUserAccountID);
        URI uri = new URI(url);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<UserAccountRequestDto> httpEntity = new HttpEntity<>(body,headers);

        // Act
        ResponseEntity<ErrorDto> response = client.exchange(uri, HttpMethod.PUT,httpEntity,ErrorDto.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorDto errorBody = response.getBody();
        assertNotNull(errorBody);
        assertEquals(1, errorBody.getErrors().size());
        assertEquals("password must be at least eight characters long", errorBody.getErrors().get(0));

    }

    @Test
    @Order(20)
    public void testUpdateUserAccountByNullAccountType() throws URISyntaxException {

        // Arrange
        UserAccountRequestDto body = new UserAccountRequestDto(NEW_NAME,NEW_PASSWORD,NEW_EMAIL,null);
        String url = String.format("/UserAccount/%d",createdUserAccountID);
        URI uri = new URI(url);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<UserAccountRequestDto> httpEntity = new HttpEntity<>(body,headers);

        // Act
        ResponseEntity<ErrorDto> response = client.exchange(uri, HttpMethod.PUT,httpEntity,ErrorDto.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorDto errorBody = response.getBody();
        assertNotNull(errorBody);
        assertEquals(1, errorBody.getErrors().size());
        assertEquals("account type is required", errorBody.getErrors().get(0));

    }


    @Test
    @Order(21)
    public void testDeleteUserAccountByInvalidUserAccountID() throws URISyntaxException{

        // Arrange
        String url = String.format("/UserAccount/%d",INVALID_USERACCOUNTID);
        URI uri = new URI(url);

        // Act
        ResponseEntity<ErrorDto> response = client.exchange(uri,HttpMethod.DELETE,HttpEntity.EMPTY, ErrorDto.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND,response.getStatusCode());
        ErrorDto errorBody = response.getBody();
        assertNotNull(errorBody);
        assertEquals(1, errorBody.getErrors().size());
        assertEquals("no userAccount has userAccountID " + INVALID_USERACCOUNTID + ".", errorBody.getErrors().get(0));
    }


    @Test
    @Order(22)
    public void testDeleteUserAccountByValidUserAccountID() throws URISyntaxException {

        // Arrange
        String url = String.format("/UserAccount/%d",createdUserAccountID);
        URI uri = new URI(url);

        // Act
        ResponseEntity<Void> r = client.exchange(uri,HttpMethod.DELETE,HttpEntity.EMPTY,Void.class);

        // Assert
        assertNotNull(r);
        assertEquals(HttpStatus.NO_CONTENT,r.getStatusCode());

        // The codes below is used to check the deleted user account is no longer in the system
        // Act
        ResponseEntity<ErrorDto> response = client.getForEntity(url, ErrorDto.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorDto body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.getErrors().size());
        assertEquals("no userAccount has userAccountID " + createdUserAccountID + ".", body.getErrors().get(0));

    }






}
