package ca.mcgill.ecse321.boardgame.dto;

import java.util.List;

/**
 * @author Kevin Jiang
 */

/**
 * This is the class for retrieving a list of UserAccounts in the response format
 */

public class UserAccountListDto {

    private List<UserAccountResponseDto> userAccountResponseDtoList;

    public UserAccountListDto() {}

    public UserAccountListDto(List<UserAccountResponseDto> userAccountListDtoList) {
        this.userAccountResponseDtoList = userAccountListDtoList;
    }

    public List<UserAccountResponseDto> getUserAccounts() {
        return userAccountResponseDtoList;
    }

    public void setUserAccounts(List<UserAccountResponseDto> userAccountResponseDtoList) {
        this.userAccountResponseDtoList = userAccountResponseDtoList;
    }
}
