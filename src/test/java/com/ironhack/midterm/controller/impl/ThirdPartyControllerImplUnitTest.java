package com.ironhack.midterm.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironhack.midterm.controller.dto.ThirdPartyOperationDTO;
import com.ironhack.midterm.model.Account;
import com.ironhack.midterm.model.AccountHolder;
import com.ironhack.midterm.model.Checking;
import com.ironhack.midterm.model.ThirdParty;
import com.ironhack.midterm.service.AccountService;
import com.ironhack.midterm.utils.Address;
import com.ironhack.midterm.utils.Money;
import com.ironhack.midterm.exceptions.*;
import com.ironhack.midterm.view_model.AccountBalance;
import com.ironhack.midterm.exceptions.*;
import com.ironhack.midterm.view_model.TransactionComplete;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ThirdPartyControllerImplUnitTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @MockBean
    private AccountService accountService;
    private MockMvc mockMvc;
    ObjectMapper mapper = new ObjectMapper();

    ThirdParty thirdParty;
    Checking checking;
    AccountHolder accountHolder;
    ThirdPartyOperationDTO operationDTO;
    TransactionComplete transactionComplete = new TransactionComplete();
    BigDecimal amount;


    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        Calendar calendar = Calendar.getInstance();
        calendar.set(1992, 12, 26);
        Address address = new Address("test street", "test city", "test country", "00000");
        accountHolder = new AccountHolder("test1", "test1", "testPassword", calendar.getTime(), address);
        thirdParty = new ThirdParty("third-party", "third-party");
        thirdParty.setId((long) 1);
        checking = new Checking(new Money(new BigDecimal("2000")), accountHolder);
        checking.setId((long) 1);
        operationDTO = new ThirdPartyOperationDTO();
        operationDTO.setAccountSecretKey(checking.getSecretKey());
        amount = new BigDecimal("100");
        operationDTO.setAmount(amount);
        transactionComplete.setTransactionMakerId(thirdParty.getId());
        transactionComplete.setAmount(amount);
    }

    @Test
    public void debitAccount_completeRequest_TransactionComplete() throws Exception{
        when(accountService.debitAccount(Mockito.any(UUID.class), Mockito.anyLong(), Mockito.any(ThirdPartyOperationDTO.class))).thenReturn(transactionComplete);
        mockMvc.perform(put("/third-party/accounts/1/debit")
                .header("Hashed-Key", thirdParty.getHashedKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(operationDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("amount").value("100"))
                .andExpect(jsonPath("transactionMakerId").value(thirdParty.getId()));
    }

    @Test
    public void debitAccount_badHeader_Unauthorized() throws Exception {
        when(accountService.debitAccount(Mockito.any(UUID.class), Mockito.anyLong(), Mockito.any(ThirdPartyOperationDTO.class))).thenThrow(NoSuchThirdPartyException.class);
        mockMvc.perform(put("/third-party/accounts/1/debit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Hashed-Key", UUID.randomUUID())
                .content(mapper.writeValueAsString(operationDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void debitAccount_missingHeader_BadRequest() throws Exception{
        mockMvc.perform(put("/third-party/accounts/1/debit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(operationDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void creditAccount_completeRequest_TransactionComplete() throws Exception {
        when(accountService.creditAccount(Mockito.any(UUID.class), Mockito.anyLong(), Mockito.any(ThirdPartyOperationDTO.class))).thenReturn(transactionComplete);
        mockMvc.perform(put("/third-party/accounts/1/credit")
                .header("Hashed-Key", thirdParty.getHashedKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(operationDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("amount").value("100"))
                .andExpect(jsonPath("transactionMakerId").value(thirdParty.getId()));
    }

    @Test
    public void creditAccount_badHeader_Unauthorized() throws Exception {
        when(accountService.creditAccount(Mockito.any(UUID.class), Mockito.anyLong(), Mockito.any(ThirdPartyOperationDTO.class))).thenThrow(NoSuchThirdPartyException.class);
        mockMvc.perform(put("/third-party/accounts/1/credit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Hashed-Key", UUID.randomUUID())
                .content(mapper.writeValueAsString(operationDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void creditAccount_missingHeader_TransactionComplete() throws Exception{
        mockMvc.perform(put("/third-party/accounts/1/credit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(operationDTO)))
                .andExpect(status().isBadRequest());
    }
}