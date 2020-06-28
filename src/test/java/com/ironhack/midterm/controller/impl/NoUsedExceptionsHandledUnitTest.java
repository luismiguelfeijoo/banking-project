package com.ironhack.midterm.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironhack.midterm.controller.dto.AccountDTO;
import com.ironhack.midterm.enums.AccountType;
import com.ironhack.midterm.exceptions.NoSuchCheckingAccountException;
import com.ironhack.midterm.exceptions.NoSuchCreditCardException;
import com.ironhack.midterm.exceptions.NoSuchSavingsAccountException;
import com.ironhack.midterm.exceptions.NoSuchStudentCheckingAccountException;
import com.ironhack.midterm.model.AccountHolder;
import com.ironhack.midterm.model.Checking;
import com.ironhack.midterm.model.Role;
import com.ironhack.midterm.model.SecuredUser;
import com.ironhack.midterm.security.CustomSecurityUser;
import com.ironhack.midterm.service.CheckingService;
import com.ironhack.midterm.service.CreditCardService;
import com.ironhack.midterm.service.SavingsService;
import com.ironhack.midterm.service.StudentCheckingService;
import com.ironhack.midterm.utils.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class NoUsedExceptionsHandledUnitTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @MockBean
    private CheckingService checkingService;
    @MockBean
    private SavingsService savingsService;
    @MockBean
    private CreditCardService creditCardService;

    private MockMvc mockMvc;
    Checking checking;
    CustomSecurityUser user;
    AccountDTO accountDTO;
    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        Calendar calendar = Calendar.getInstance();
        calendar.set(1992, 12, 26);
        Address address = new Address("test street", "test city", "test country", "00000");
        AccountHolder accountHolder = new AccountHolder("test1", "test1", "testPassword", calendar.getTime(), address);
        accountHolder.setId((long) 1);
        Role role = new Role();
        role.setRole("ROLE_ADMIN");
        SecuredUser securedUser = new SecuredUser(accountHolder.getUsername(), accountHolder.getName(), accountHolder.getPassword());
        Set<Role> roles = securedUser.getRoles();
        roles.add(role);
        securedUser.setRoles(roles);
        user = new CustomSecurityUser(securedUser);
        user.setId((long) 9);
        accountDTO = new AccountDTO(AccountType.CHECKING, new BigDecimal("2000"), accountHolder);
    }

    @Test
    public void notFoundCheckingAccount_NotFound() throws Exception {
        when(checkingService.create(Mockito.any(AccountDTO.class))).thenThrow(NoSuchCheckingAccountException.class);
        mockMvc.perform(post("/admin/accounts").with(user(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(accountDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void notFoundStudentCheckingAccount_NotFound() throws Exception {
        when(checkingService.create(Mockito.any(AccountDTO.class))).thenThrow(NoSuchStudentCheckingAccountException.class);
        mockMvc.perform(post("/admin/accounts").with(user(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(accountDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void notFoundSavingsAccount_NotFound() throws Exception {
        accountDTO.setAccountType(AccountType.SAVINGS);
        when(savingsService.create(Mockito.any(AccountDTO.class))).thenThrow(NoSuchSavingsAccountException.class);
        mockMvc.perform(post("/admin/accounts").with(user(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(accountDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void notFoundCreditCardAccount_NotFound() throws Exception {
        accountDTO.setAccountType(AccountType.CREDITCARD);
        when(creditCardService.create(Mockito.any(AccountDTO.class))).thenThrow(NoSuchCreditCardException.class);
        mockMvc.perform(post("/admin/accounts").with(user(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(accountDTO)))
                .andExpect(status().isNotFound());
    }
}
