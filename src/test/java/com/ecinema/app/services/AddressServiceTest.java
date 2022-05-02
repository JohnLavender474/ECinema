package com.ecinema.app.services;

import com.ecinema.app.domain.dtos.AddressDto;
import com.ecinema.app.domain.entities.Address;
import com.ecinema.app.repositories.AddressRepository;
import com.ecinema.app.services.implementations.AddressServiceImpl;
import com.ecinema.app.utils.UsState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    private AddressService addressService;
    @Mock
    private AddressRepository addressRepository;

    @BeforeEach
    void setUp() {
        addressService = new AddressServiceImpl(addressRepository);
    }

    @Test
    void addressDto() {
        // given
        Address address = new Address();
        address.setId(1L);
        address.setCity("test city");
        address.setStreet("test street");
        address.setUsState(UsState.GEORGIA);
        address.setZipcode("12345");
        given(addressRepository.findById(1L))
                .willReturn(Optional.of(address));
        addressService.save(address);
        // when
        AddressDto addressDto = addressService.convertToDto(1L);
        // then
        assertEquals(address.getId(), addressDto.getId());
        assertEquals(address.getCity(), addressDto.getCity());
        assertEquals(address.getStreet(), addressDto.getStreet());
        assertEquals(address.getZipcode(), addressDto.getZipcode());
        assertEquals(address.getUsState(), addressDto.getUsState());
    }

}