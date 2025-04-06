package ca.mcgill.ecse321.boardgame.controller;

import ca.mcgill.ecse321.boardgame.dto.UserAccountListDto;
import ca.mcgill.ecse321.boardgame.dto.UserAccountRequestDto;
import ca.mcgill.ecse321.boardgame.dto.UserAccountResponseDto;
import ca.mcgill.ecse321.boardgame.model.UserAccount;
import ca.mcgill.ecse321.boardgame.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Kevin Jiang
 */

/**
 * This is the UserAccount controller class
 */
@RestController
@CrossOrigin(origins = "*")
public class UserAccountController {

    @Autowired
    private UserAccountService userAccountService;

    /**
     * Return all the user accounts in the database
     *
     * @return All the user accounts in the database
     */
    @GetMapping("/UserAccount")
    public UserAccountListDto findAllUserAccount() {
        List<UserAccountResponseDto> userAccountResponseDtoList = new ArrayList<UserAccountResponseDto>();
        for (UserAccount model : userAccountService.findAllUserAccount()) {
            userAccountResponseDtoList.add(new UserAccountResponseDto(model));
        }
        return new UserAccountListDto(userAccountResponseDtoList);
    }

    /**
     * Find a user account
     *
     * @param userAccountID The id of the user account to be found
     * @return The user account that has the userAccountID
     */
    @GetMapping("/UserAccount/{userAccountID}")
    public UserAccountResponseDto findUserAccountByUserAccountID(@PathVariable long userAccountID) {
        UserAccount userAccount = userAccountService.findUserAccountByUserAccountID(userAccountID);
        return new UserAccountResponseDto(userAccount);
    }

    /**
     * Create a new user account
     *
     * @param userAccount The user account that is to be created
     * @return The user account that is created
     */
    @PostMapping("/UserAccount")
    @ResponseStatus(HttpStatus.CREATED)
    public UserAccountResponseDto createUserAccount(@RequestBody UserAccountRequestDto userAccount) {
        UserAccount createdUserAccount = userAccountService.createUserAccount(userAccount);
        return new UserAccountResponseDto(createdUserAccount);
    }

    @PostMapping("/UserAccount/userAccountID")
    @ResponseStatus(HttpStatus.CREATED)
    public Long createUserAccountReturnUserAccountID(@RequestBody UserAccountRequestDto userAccount) throws Exception {
        UserAccount createdUserAccount = userAccountService.createUserAccount(userAccount);
        return createdUserAccount.getUserAccountID();
    }

    /**
     * Update the password of a user account
     *
     * @param userAccountID The id of the user account to be updated
     * @param fields        The new password
     * @return The user account after updated
     */
    @PatchMapping("/UserAccount/{userAccountID}/password")
    @ResponseStatus(HttpStatus.OK)
    public UserAccountResponseDto updatePasswordByUserAccountID(@PathVariable long userAccountID,
            @RequestBody Map<String, String> fields) {
        UserAccount updatedUserAccount = userAccountService.updatePasswordByUserAccountID(userAccountID, fields);
        return new UserAccountResponseDto(updatedUserAccount);
    }

    /**
     * Update a user account
     *
     * @param userAccountID The id of the user account to be updated
     * @param userAccount   New attribute values of the user account
     * @return The user account after updated
     */
    @PutMapping("/UserAccount/{userAccountID}")
    @ResponseStatus(HttpStatus.OK)
    public UserAccountResponseDto updateUserAccountByUserAccountID(@PathVariable long userAccountID,
            @RequestBody UserAccountRequestDto userAccount) {
        UserAccount updatedUserAccount = userAccountService.updateUserAccountByUserAccountID(userAccountID,
                userAccount);
        return new UserAccountResponseDto(updatedUserAccount);
    }

    /**
     * Delete a user account
     *
     * @param userAccountID The id of the user account to be deleted
     */
    @DeleteMapping("/UserAccount/{userAccountID}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserAccountByUserAccountID(@PathVariable long userAccountID) {
        userAccountService.deleteUserAccountByUserAccountID(userAccountID);
    }

    /**
     * Login to a user account
     *
     * @param userAccountID The id of the user account to be logged-in
     * @param password      The password of the user account to be logged-in
     * @return The user account that was logged-into
     */
    @PostMapping("/UserAccount/login/{userAccountID}/{password}")
    @ResponseStatus(HttpStatus.OK)
    public UserAccountResponseDto login(@PathVariable long userAccountID, @PathVariable String password) {
        UserAccount loginUserAccount = userAccountService.login(userAccountID, password);
        return new UserAccountResponseDto(loginUserAccount);
    }

}
