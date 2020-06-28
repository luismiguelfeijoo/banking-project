package com.ironhack.midterm.service;

import com.ironhack.midterm.controller.dto.ThirdPartyDTO;
import com.ironhack.midterm.exceptions.DuplicatedUsernameException;
import com.ironhack.midterm.model.ThirdParty;
import com.ironhack.midterm.repository.ThirdPartyRepository;
import com.ironhack.midterm.utils.Hashing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ThirdPartyService {
    @Autowired
    private ThirdPartyRepository thirdPartyRepository;

    private final static Logger LOGGER = LogManager.getLogger(ThirdPartyService.class);

    @Secured({"ROLE_ADMIN"})
    @Transactional
    public ThirdParty create(ThirdPartyDTO thirdPartyDTO) {
        LOGGER.info("[CREATE THIRD PARTY (admin)]");
        ThirdParty newThirdParty = new ThirdParty(thirdPartyDTO.getUsername(), thirdPartyDTO.getName());
        try {
            newThirdParty = thirdPartyRepository.save(newThirdParty);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicatedUsernameException("There's already an user with the provided username");
        }
        return newThirdParty;
    }
}
