package com.ironhack.midterm.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironhack.midterm.controller.dto.AccountDTO;
import com.ironhack.midterm.controller.dto.AmountDTO;
import com.ironhack.midterm.controller.dto.ThirdPartyDTO;
import com.ironhack.midterm.enums.AccountType;
import com.ironhack.midterm.exceptions.DuplicatedUsernameException;
import com.ironhack.midterm.exceptions.NoSuchAccountException;
import com.ironhack.midterm.exceptions.NoSuchAccountHolderException;
import com.ironhack.midterm.exceptions.NoSuchUserException;
import com.ironhack.midterm.model.*;
import com.ironhack.midterm.security.CustomSecurityUser;
import com.ironhack.midterm.service.*;
import com.ironhack.midterm.utils.Address;
import com.ironhack.midterm.utils.Money;
import com.ironhack.midterm.view_model.AccountBalance;
import com.ironhack.midterm.view_model.TransactionComplete;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@SpringBootTest
class AdminControllerImplUnitTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @MockBean
    private AccountService accountService;
    @MockBean
    private ThirdPartyService thirdPartyService;
    @MockBean
    private AccountHolderService accountHolderService;
    @MockBean
    private CheckingService checkingService;
    @MockBean
    private StudentCheckingService studentCheckingService;
    @MockBean
    private SavingsService savingsService;
    @MockBean
    private CreditCardService creditCardService;
    private MockMvc mockMvc;
    ObjectMapper mapper = new ObjectMapper();

    AccountHolder accountHolder;
    CustomSecurityUser user;
    Checking checking;
    BigDecimal amount;
    TransactionComplete transactionComplete = new TransactionComplete();
    AccountDTO accountDTO;
    AmountDTO amountDTO = new AmountDTO();

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        Calendar calendar = Calendar.getInstance();
        calendar.set(1992, 12, 26);
        Address address = new Address("test street", "test city", "test country", "00000");
        accountHolder = new AccountHolder("test1", "test1", "testPassword", calendar.getTime(), address);
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
        checking = new Checking(new Money(new BigDecimal("2000.00")), accountHolder);
        checking.setId((long) 1);
        amount = new BigDecimal("100");
        transactionComplete.setTransactionMakerId(accountHolder.getId());
        transactionComplete.setAmount(amount);
    }

    @Test
    public void createAccount_checkingAccount_AccountCreated() throws Exception {
        when(checkingService.create(Mockito.any(AccountDTO.class))).thenReturn(checking);
        mockMvc.perform(post("/admin/accounts").with(user(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(accountDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value("1"))
                .andExpect(jsonPath("balance.amount").value("2000.0"))
                .andExpect(jsonPath("primaryOwner.id").value("1"));
    }

    @Test
    public void createAccount_savingsAccount_AccountCreated() throws Exception {
        accountDTO.setAccountType(AccountType.SAVINGS);
        Savings savings = new Savings(new Money(new BigDecimal("2000.00")), accountHolder);
        savings.setId((long) 2);
        when(savingsService.create(Mockito.any(AccountDTO.class))).thenReturn(savings);
        mockMvc.perform(post("/admin/accounts").with(user(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(accountDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value("2"))
                .andExpect(jsonPath("balance.amount").value("2000.0"))
                .andExpect(jsonPath("primaryOwner.id").value("1"));
    }

    @Test
    public void createAccount_creditAccount_AccountCreated() throws Exception {
        accountDTO.setAccountType(AccountType.CREDITCARD);
        accountDTO.setBalance(BigDecimal.ZERO);
        CreditCard creditCard = new CreditCard(accountHolder);
        creditCard.setId((long) 3);
        when(creditCardService.create(Mockito.any(AccountDTO.class))).thenReturn(creditCard);
        mockMvc.perform(post("/admin/accounts").with(user(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(accountDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value("3"))
                .andExpect(jsonPath("balance.amount").value("0.0"))
                .andExpect(jsonPath("primaryOwner.id").value("1"));
    }

    @Test
    public void createAccountHolder_ValidBody_CreatedAccountHolder() throws Exception {
        when(accountHolderService.create(Mockito.any(AccountHolder.class))).thenReturn(accountHolder);
        mockMvc.perform(post("/admin/account-holders").with(user(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(accountHolder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value("test1"))
                .andExpect(jsonPath("username").value("test1"))
                .andExpect(jsonPath("id").value("1"));
    }

    @Test
    public void createAccountHolder_missingRequestBody_BadRequest() throws Exception {
        when(accountHolderService.create(Mockito.any(AccountHolder.class))).thenReturn(accountHolder);
        mockMvc.perform(post("/admin/account-holders").with(user(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addThirdParty_ValidBody_CreatedThirdParty() throws Exception {
        ThirdPartyDTO thirdPartyDTO = new ThirdPartyDTO("third-party", "tp");
        ThirdParty thirdParty = new ThirdParty();
        thirdParty.setId((long) 1);
        thirdParty.setName(thirdPartyDTO.getName());
        thirdParty.setUsername(thirdPartyDTO.getUsername());
        when(thirdPartyService.create(Mockito.any(ThirdPartyDTO.class))).thenReturn(thirdParty);
        mockMvc.perform(post("/admin/third-party").with(user(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(thirdPartyDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value("third-party"))
                .andExpect(jsonPath("username").value("tp"))
                .andExpect(jsonPath("id").value("1"))
                .andExpect(jsonPath("hashedKey").value(thirdParty.getHashedKey()));
    }

    @Test
    public void addThirdParty_RepeatedUsername_Conflict() throws Exception {
        ThirdPartyDTO thirdPartyDTO = new ThirdPartyDTO("third-party", "tp");
        ThirdParty thirdParty = new ThirdParty();
        thirdParty.setId((long) 1);
        thirdParty.setName(thirdPartyDTO.getName());
        thirdParty.setUsername(thirdPartyDTO.getUsername());
        when(thirdPartyService.create(Mockito.any(ThirdPartyDTO.class))).thenThrow(DuplicatedUsernameException.class);
        mockMvc.perform(post("/admin/third-party").with(user(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(thirdPartyDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    public void addThirdParty_missingRequestBody_BadRequest() throws Exception {
        mockMvc.perform(post("/admin/third-party").with(user(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getAccountHolders_AccountHolders_ReturnList() throws Exception {
        when(accountHolderService.findAll()).thenReturn(Stream.of(accountHolder).collect(Collectors.toList()));
        mockMvc.perform(get("/admin/account-holders").with(user(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("test1"))
                .andExpect(jsonPath("$[0].username").value("test1"))
                .andExpect(jsonPath("$[0].id").value("1"));
    }

    @Test
    public void getAccountHolders_NoAccountHolders_ReturnListEmpty() throws Exception {
        when(accountHolderService.findAll()).thenReturn(new ArrayList<>());
        String result = mockMvc.perform(get("/admin/account-holders").with(user(user)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(result.contains("[]"));
    }

    @Test
    public void getAccountHolder_ValidId_AccountHolder() throws Exception {
        when(accountHolderService.findById((long) 1)).thenReturn(accountHolder);
        mockMvc.perform(get("/admin/account-holders/1").with(user(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value("test1"))
                .andExpect(jsonPath("username").value("test1"))
                .andExpect(jsonPath("id").value("1"));
    }

    @Test
    public void getAccountHolder_NotValidId_AccountHolder() throws Exception {
        when(accountHolderService.findById((long) 2)).thenThrow(NoSuchAccountHolderException.class);
        mockMvc.perform(get("/admin/account-holders/2").with(user(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void accessAccount_ExistingAccount_BalanceReturned() throws Exception {
        when(accountService.getBalanceById((long) 1)).thenReturn(new AccountBalance(checking.getBalance()));
        mockMvc.perform(get("/admin/accounts/1").with(user(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("balance.amount").value("2000.0"));
    }

    @Test
    public void accessAccount_NotExistingAccount_NotFound() throws Exception {
        when(accountService.getBalanceById((long) 2)).thenThrow(NoSuchAccountHolderException.class);
        mockMvc.perform(get("/admin/accounts/2").with(user(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void debitAccount_CompleteRequest_TransactionComplete() throws Exception {
        amountDTO.setAmount(new BigDecimal("100.00"));
        transactionComplete.setTransactionMakerId(user.getId());
        checking.debitAccount(new Money(amountDTO.getAmount()));
        transactionComplete.setUserAccount(new AccountBalance(checking.getBalance()));
        when(accountService.debitAccount(Mockito.anyLong(), Mockito.any(SecuredUser.class), Mockito.any(AmountDTO.class))).thenReturn(transactionComplete);
        mockMvc.perform(put("/admin/accounts/1/debit").with(user(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(amountDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("amount").value("100"))
                .andExpect(jsonPath("userAccount.balance.amount").value("1900.0"))
                .andExpect(jsonPath("transactionMakerId").value("9"));
    }

    @Test
    public void debitAccount_NoUser_NotFound() throws Exception {
        amountDTO.setAmount(new BigDecimal("100.00"));
        when(accountService.debitAccount(Mockito.anyLong(), Mockito.any(SecuredUser.class), Mockito.any(AmountDTO.class))).thenThrow(NoSuchUserException.class);
        mockMvc.perform(put("/admin/accounts/1/debit").with(user(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(amountDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void debitAccount_NoAccountId_NotFound() throws Exception {
        amountDTO.setAmount(new BigDecimal("100.00"));
        when(accountService.debitAccount(Mockito.anyLong(), Mockito.any(SecuredUser.class), Mockito.any(AmountDTO.class))).thenThrow(NoSuchAccountException.class);
        mockMvc.perform(put("/admin/accounts/1/debit").with(user(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(amountDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void creditAccount_CompleteRequest_TransactionComplete() throws Exception {
        amountDTO.setAmount(new BigDecimal("100.00"));
        transactionComplete.setTransactionMakerId(user.getId());
        checking.creditAccount(new Money(amountDTO.getAmount()));
        transactionComplete.setUserAccount(new AccountBalance(checking.getBalance()));
        when(accountService.creditAccount(Mockito.anyLong(), Mockito.any(SecuredUser.class), Mockito.any(AmountDTO.class))).thenReturn(transactionComplete);
        mockMvc.perform(put("/admin/accounts/1/credit").with(user(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(amountDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("amount").value("100"))
                .andExpect(jsonPath("userAccount.balance.amount").value("2100.0"))
                .andExpect(jsonPath("transactionMakerId").value("9"));
    }



}