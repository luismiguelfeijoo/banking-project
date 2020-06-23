package com.ironhack.midterm.service;

import com.ironhack.midterm.controller.dto.ThirdPartyDTO;
import com.ironhack.midterm.model.ThirdParty;
import com.ironhack.midterm.repository.ThirdPartyRepository;
import com.ironhack.midterm.utils.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ThirdPartyService {
    @Autowired
    private ThirdPartyRepository thirdPartyRepository;

    public ThirdParty create(ThirdPartyDTO thirdPartyDTO) {
        ThirdParty newThirdParty = new ThirdParty(thirdPartyDTO.getUsername(), thirdPartyDTO.getName(), Hashing.hash(thirdPartyDTO.getKey()));
        return thirdPartyRepository.save(newThirdParty);
    }
}
