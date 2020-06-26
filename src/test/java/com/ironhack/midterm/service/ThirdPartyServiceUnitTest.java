package com.ironhack.midterm.service;

import com.ironhack.midterm.controller.dto.ThirdPartyDTO;
import com.ironhack.midterm.model.ThirdParty;
import com.ironhack.midterm.repository.ThirdPartyRepository;
import com.ironhack.midterm.utils.Hashing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class ThirdPartyServiceUnitTest {
    @Autowired
    private ThirdPartyService thirdPartyService;
    @MockBean
    private ThirdPartyRepository thirdPartyRepository;

    ThirdParty thirdParty;
    ThirdPartyDTO thirdPartyDTO;
    @BeforeEach
    public void setUp() {
        thirdPartyDTO = new ThirdPartyDTO("testPass", "test", "test");
        when(thirdPartyRepository.save(Mockito.any(ThirdParty.class))).thenReturn(new ThirdParty(thirdPartyDTO.getUsername(), thirdPartyDTO.getName(), Hashing.hash(thirdPartyDTO.getKey())));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void create_Admin_CreatedThirdParty() {
        ThirdParty newThirdParty = thirdPartyService.create(thirdPartyDTO);
        assertEquals(thirdPartyDTO.getName(), newThirdParty.getName());
        assertEquals(thirdPartyDTO.getUsername(), newThirdParty.getUsername());
        assertFalse(thirdPartyDTO.getKey().equals(newThirdParty.getHashedKey()));
    }

    @Test
    public void create_NotAdmin_Error() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, ()-> thirdPartyService.create(thirdPartyDTO));
    }

}