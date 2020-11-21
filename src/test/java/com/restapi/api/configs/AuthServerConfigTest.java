package com.restapi.api.configs;


import com.restapi.api.account.Account;
import com.restapi.api.account.AccountRole;
import com.restapi.api.account.AccountService;
import com.restapi.api.common.BaseControllerTest;
import com.restapi.api.common.TestDescription;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

public class AuthServerConfigTest extends BaseControllerTest {

    @Autowired
    AccountService accountService;

    @Test
    @TestDescription("인증 토큰을 발급 받는 테스트")
    public void getAuthToken() throws Exception {
        // Given
        String username = "keesun@email.com";
        String password = "keesun";
        Account account = Account.builder()
                                            .email(username)
                                            .password(password)
                                            .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                                            .build();

        this.accountService.saveAccount(account);

        String clientId = "myApp";
        String clientSecret = "pass";

        // When & Then
        mockMvc.perform(post("/oauth/token")
                                .with(httpBasic(clientId, clientSecret))
                                .param("username", username)
                                .param("password", password)
                                .param("grant_type", "password"))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("access_token").exists());

    }

}
