package com.ironhack.midterm.service;

import com.ironhack.midterm.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AccountServiceUnitTest {
    @Autowired
    private AccountService accountService;
    @MockBean
    private AccountRepository accountRepository;
    @MockBean
    private CreditCardRepository creditCardRepository;
    @MockBean
    private CheckingRepository checkingRepository;
    @MockBean
    private StudentCheckingRepository studentCheckingRepository;
    @MockBean
    private SavingsRepository savingsRepository;
    @MockBean
    private TransactionRepository transactionRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private ThirdPartyRepository thirdPartyRepository;

    public void findById() {}
    public void getBalanceById() {}
    public void getAllBalanceByUserId() {}
    public void transfer() {}
    public void creditAccount() {}
    public void debitAccount() {}
}