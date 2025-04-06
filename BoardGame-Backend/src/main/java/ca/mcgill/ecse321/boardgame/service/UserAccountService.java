package ca.mcgill.ecse321.boardgame.service;

import ca.mcgill.ecse321.boardgame.dto.UserAccountRequestDto;
import ca.mcgill.ecse321.boardgame.exception.BoardGameException;
import ca.mcgill.ecse321.boardgame.model.AccountType;
import ca.mcgill.ecse321.boardgame.model.GameCopy;
import ca.mcgill.ecse321.boardgame.model.UserAccount;
import ca.mcgill.ecse321.boardgame.repo.GameCopyRepository;
import ca.mcgill.ecse321.boardgame.repo.UserAccountRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.annotation.Validated;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author Kevin Jiang
 */

/**
 *  This is the UserAccount service class
 */
@Service
@Validated
public class UserAccountService {

    @Autowired
    private UserAccountRepository userAccountRepo;

    @Autowired
    private GameCopyRepository gameCopyRepo;

    /**
     * Return all the user accounts in the database
     *
     * @return All the user accounts in the database
     */
    @Transactional
    public Iterable<UserAccount> findAllUserAccount(){
        return userAccountRepo.findAll();
    }


    /**
     * Find a user account
     *
     * @param userAccountID The id of the user account to be found
     * @return The user account that has the userAccountID
     */
    @Transactional
    public UserAccount findUserAccountByUserAccountID(long userAccountID){
        UserAccount userAccount = userAccountRepo.findUserAccountByUserAccountID(userAccountID);
        // No user account with userAccountID was found
        if (userAccount == null) {
            throw new BoardGameException(HttpStatus.NOT_FOUND,"no userAccount has userAccountID " + userAccountID + ".");
        }
        return userAccount;
    }

    /**
     * Create a new user account
     *
     * @param userAccount The user account that is to be created
     * @return The user account that is created
     */
    @Transactional
    public UserAccount createUserAccount(@Valid UserAccountRequestDto userAccount){
        // The account type cannot be empty
        if (userAccount.getAccountType() == null){
            throw new BoardGameException(HttpStatus.BAD_REQUEST,"account type is required");
        }
        UserAccount userToCreate = new UserAccount(userAccount.getName(),userAccount.getPassword(),userAccount.getEmail(),userAccount.getAccountType());
        return userAccountRepo.save(userToCreate);
    }

    /**
     * Update the password of a user account
     *
     * @param userAccountID The id of the user account to be updated
     * @param fields The new password
     * @return The user account after updated
     */
    @Transactional
    public UserAccount updatePasswordByUserAccountID(long userAccountID,Map<String, String> fields){
        UserAccount userAccount = userAccountRepo.findUserAccountByUserAccountID(userAccountID);
        // No user account with userAccountID was found
        if (userAccount == null) {
            throw new BoardGameException(HttpStatus.NOT_FOUND,"no userAccount has userAccountID " + userAccountID + ".");
        }
        // Cannot update multiple attributes and must specify the column name "password" in the request
        if (fields.size() != 1 || fields.get("password") == null){
            throw new BoardGameException(HttpStatus.BAD_REQUEST,"invalid argument");
        }
        // The password cannot have a length less than 8 characters
        if (fields.get("password").length() < 8){
            throw new BoardGameException(HttpStatus.BAD_REQUEST,"password must be at least eight characters long");
        }

        fields.forEach((key,value) ->{
            Field field = ReflectionUtils.findField(UserAccount.class,key);
            field.setAccessible(true);
            ReflectionUtils.setField(field,userAccount,value);
        });
        return userAccountRepo.save(userAccount);
    }

    /**
     * Update a user account
     *
     * @param userAccountID The id of the user account to be updated
     * @param userAccount New attribute values of the user account
     * @return The user account after updated
     */
    @Transactional
    public UserAccount updateUserAccountByUserAccountID(long userAccountID,@Valid UserAccountRequestDto userAccount){
        UserAccount u = userAccountRepo.findUserAccountByUserAccountID(userAccountID);
        // No user account with userAccountID was found
        if (u == null) {
            throw new BoardGameException(HttpStatus.NOT_FOUND,"no userAccount has userAccountID " + userAccountID + ".");
        }
        u.setName(userAccount.getName());
        u.setPassword(userAccount.getPassword());
        u.setEmail(userAccount.getEmail());
        // The account type cannot be empty
        if (userAccount.getAccountType() == null){
            throw new BoardGameException(HttpStatus.BAD_REQUEST,"account type is required");
        }
        // When a game owner tries to become a player, he must already deleted all the game copies he owned
        if (u.getAccountType().equals(AccountType.GAMEOWNER) && userAccount.getAccountType().equals(AccountType.PLAYER)){
            Iterable<GameCopy> gameCopies = gameCopyRepo.findAll();
            for (GameCopy gameCopy : gameCopies){
                if (gameCopy.getGameCopyKey().getOwner().getUserAccountID() == userAccountID){
                    throw new BoardGameException(HttpStatus.FORBIDDEN,"game owner need to delete all his game copies before become player");
                }
            }
        }
        u.setAccountType(userAccount.getAccountType());
        return userAccountRepo.save(u);
    }

    /**
     * Delete a user account
     *
     * @param userAccountID The id of the user account to be deleted
     */
    @Transactional
    public void deleteUserAccountByUserAccountID(long userAccountID){
        UserAccount userAccount = userAccountRepo.findUserAccountByUserAccountID(userAccountID);
        // No user account with userAccountID was found
        if (userAccount == null) {
            throw new BoardGameException(HttpStatus.NOT_FOUND,"no userAccount has userAccountID " + userAccountID + ".");
        }
        userAccountRepo.delete(userAccount);
    }

    /**
     * Login to a user account
     *
     * @param userAccountID The id of the user account to be logged-in
     * @param password The password of the user account to be logged-in
     * @return The user account that was logged-into
     */
    @Transactional
    public UserAccount login(long userAccountID,String password){
        UserAccount userAccount = userAccountRepo.findUserAccountByUserAccountID(userAccountID);
        // No user account with userAccountID was found
        if (userAccount == null) {
            throw new BoardGameException(HttpStatus.NOT_FOUND,"no userAccount has userAccountID " + userAccountID + ".");
        }
        // The entered password has to match the password of the user account that is logged-in
        if (!password.equals(userAccount.getPassword())){
            throw new BoardGameException(HttpStatus.UNAUTHORIZED,"wrong password was given.");
        }
        return userAccount;
    }
}
