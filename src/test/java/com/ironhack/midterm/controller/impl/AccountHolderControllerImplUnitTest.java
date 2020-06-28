package com.ironhack.midterm.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironhack.midterm.controller.dto.ThirdPartyOperationDTO;
import com.ironhack.midterm.controller.dto.TransferDTO;
import com.ironhack.midterm.exceptions.FraudDetectionException;
import com.ironhack.midterm.exceptions.NoEnoughBalanceException;
import com.ironhack.midterm.exceptions.NoPermissionForUserException;
import com.ironhack.midterm.exceptions.NoSuchAccountException;
import com.ironhack.midterm.model.*;
import com.ironhack.midterm.security.CustomSecurityUser;
import com.ironhack.midterm.service.AccountService;
import com.ironhack.midterm.utils.Address;
import com.ironhack.midterm.utils.Money;
import com.ironhack.midterm.view_model.AccountBalance;
import com.ironhack.midterm.view_model.TransactionComplete;
import org.hamcrest.number.BigDecimalCloseTo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AccountHolderControllerImplUnitTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @MockBean
    private AccountService accountService;
    private MockMvc mockMvc;
    ObjectMapper mapper = new ObjectMapper();

    Checking checking;
    AccountHolder accountHolder;
    AccountBalance accountBalance;
    TransactionComplete transactionComplete = new TransactionComplete();
    BigDecimal amount;
    CustomSecurityUser user;


    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        Calendar calendar = Calendar.getInstance();
        calendar.set(1992, 12, 26);
        Address address = new Address("test street", "test city", "test country", "00000");
        accountHolder = new AccountHolder("test1", "test1", "testPassword", calendar.getTime(), address);
        accountHolder.setId((long) 1);

        Role role = new Role();
        role.setRole("ROLE_ACCOUNTHOLDER");
        SecuredUser securedUser = new SecuredUser(accountHolder.getUsername(), accountHolder.getName(), accountHolder.getPassword());
        Set<Role> roles = securedUser.getRoles();
        roles.add(role);
        securedUser.setRoles(roles);
        user = new CustomSecurityUser(securedUser);
        checking = new Checking(new Money(new BigDecimal("2000.00")), accountHolder);
        checking.setId((long) 1);
        amount = new BigDecimal("100");
        transactionComplete.setTransactionMakerId(accountHolder.getId());
        transactionComplete.setAmount(amount);
    }

    @Test
    public void getAllBalance_UserWithAccounts_ListOfBalance() throws Exception {
        accountBalance = new AccountBalance(checking.getBalance());
        List<AccountBalance> result =  Stream.of(accountBalance).collect(Collectors.toList());
        when(accountService.getAllBalanceByUserId(Mockito.any(SecuredUser.class))).thenReturn(result);
        mockMvc.perform(get("/accounts")
                .with(user(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].balance.amount").value(new BigDecimal("2000.0")));
    }

    @Test
    public void getAllBalance_NotOwnerOfAnyAccount_NotFound() throws Exception {
        accountBalance = new AccountBalance(checking.getBalance());
        when(accountService.getAllBalanceByUserId(Mockito.any(SecuredUser.class))).thenReturn(new ArrayList<>());
        MvcResult result = mockMvc.perform(get("/accounts")
                .with(user(user)))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains("[]"));
    }

    @Test
    public void getBalance_ExistingAccount_Balance() throws Exception {
        accountBalance = new AccountBalance(checking.getBalance());
        when(accountService.getBalanceById(Mockito.anyLong(),Mockito.any(SecuredUser.class))).thenReturn(accountBalance);
        mockMvc.perform(get("/accounts/1")
                .with(user(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("balance.amount").value(new BigDecimal("2000.0")));
    }

    @Test
    public void getBalance_NotExistingAccount_NotFound() throws Exception {
        accountBalance = new AccountBalance(checking.getBalance());
        when(accountService.getBalanceById(Mockito.anyLong(), Mockito.any(SecuredUser.class))).thenThrow(NoSuchAccountException.class);
        mockMvc.perform(get("/accounts/1")
                .with(user(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void transfer_CompleteFields_Result() throws Exception {
        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setAmount(new BigDecimal("1000.00"));
        transferDTO.setReceiverAccountId((long) 2);
        transferDTO.setReceiverName("testName");
        transactionComplete.setAmount(new BigDecimal("1000.00"));
        transactionComplete.setUserAccount(new AccountBalance(checking.getBalance()));
        transactionComplete.setTransactionMakerId((long) 1);
        when(accountService.transfer(Mockito.anyLong(), Mockito.any(SecuredUser.class), Mockito.any(TransferDTO.class))).thenReturn(transactionComplete);
        mockMvc.perform(post("/accounts/1/transfer").with(user(user))
                .content(mapper.writeValueAsString(transferDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("transactionMakerId").value("1"))
                .andExpect(jsonPath("userAccount.balance.amount").value("2000.0"))
                .andExpect(jsonPath("amount").value("1000.0")).andReturn().getResponse().getContentAsString();
    }

    @Test
    public void transfer_NoAccessToUser_Forbidden() throws Exception {
        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setAmount(new BigDecimal("1000.00"));
        transferDTO.setReceiverAccountId((long) 2);
        transferDTO.setReceiverName("testName");
        when(accountService.transfer(Mockito.anyLong(), Mockito.any(SecuredUser.class), Mockito.any(TransferDTO.class))).thenThrow(NoPermissionForUserException.class);
        mockMvc.perform(post("/accounts/1/transfer").with(user(user))
                .content(mapper.writeValueAsString(transferDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void transfer_PossibleFraud_Conflict() throws Exception {
        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setAmount(new BigDecimal("1000.00"));
        transferDTO.setReceiverAccountId((long) 2);
        transferDTO.setReceiverName("testName");
        when(accountService.transfer(Mockito.anyLong(), Mockito.any(SecuredUser.class), Mockito.any(TransferDTO.class))).thenThrow(FraudDetectionException.class);
        mockMvc.perform(post("/accounts/1/transfer").with(user(user))
                .content(mapper.writeValueAsString(transferDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    public void transfer_NoAccessToUser_Conflict() throws Exception {
        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setAmount(new BigDecimal("1000.00"));
        transferDTO.setReceiverAccountId((long) 2);
        transferDTO.setReceiverName("testName");
        when(accountService.transfer(Mockito.anyLong(), Mockito.any(SecuredUser.class), Mockito.any(TransferDTO.class))).thenThrow(NoEnoughBalanceException.class);
        mockMvc.perform(post("/accounts/1/transfer").with(user(user))
                .content(mapper.writeValueAsString(transferDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }


}