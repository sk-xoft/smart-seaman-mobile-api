package com.seaman.service;

import com.seaman.entity.DeliveryAddressEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.request.DeliveryAddressRequest;
import com.seaman.repository.DeliveryAddressRepository;
import com.seaman.repository.ThailandAddressRepository;
import com.seaman.model.response.DeliveryAddressResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class DeliveryAddressServiceTest {
    @Mock DeliveryAddressRepository deliveryAddressRepository;
    @Mock ThailandAddressRepository thailandAddressRepository;
    @Mock HttpServletRequest httpServletRequest;

    private DeliveryAddressService service;
    private UsersEntity user;

    @BeforeEach
    void setUp() {
        service = new DeliveryAddressService(
                deliveryAddressRepository, thailandAddressRepository, httpServletRequest);
        user = new UsersEntity();
        user.setMobileUuid("user-uuid");
        lenient().when(httpServletRequest.getAttribute("userObject")).thenReturn(user);
    }

    @Test
    void firstAddressIsAlwaysDefault() {
        DeliveryAddressRequest request = request(false);
        validMaster(request);
        when(deliveryAddressRepository.countActive("user-uuid")).thenReturn(0);

        assertTrue(service.create(request).getIsDefault());
        verify(deliveryAddressRepository).clearDefault("user-uuid");
        verify(deliveryAddressRepository).insert(any(DeliveryAddressEntity.class));
    }

    @Test
    void subsequentAddressCanRemainNonDefault() {
        DeliveryAddressRequest request = request(false);
        validMaster(request);
        when(deliveryAddressRepository.countActive("user-uuid")).thenReturn(1);

        assertFalse(service.create(request).getIsDefault());
        verify(deliveryAddressRepository, never()).clearDefault("user-uuid");
    }

    @Test
    void rejectsAddressNotInMasterData() {
        DeliveryAddressRequest request = request(false);
        when(thailandAddressRepository.isValidAddress(
                request.getProvince(), request.getDistrict(), request.getSubDistrict(), request.getPostalCode()))
                .thenReturn(false);

        assertThrows(BusinessException.class, () -> service.create(request));
        verify(deliveryAddressRepository, never()).insert(any());
    }

    @Test
    void cannotUpdateAnotherUsersOrInactiveAddress() {
        DeliveryAddressRequest request = request(true);
        validMaster(request);
        String id = UUID.randomUUID().toString();
        when(deliveryAddressRepository.findActiveOwned(id, "user-uuid")).thenReturn(null);

        assertThrows(BusinessException.class, () -> service.update(id, request));
        verify(deliveryAddressRepository, never()).update(any());
    }

    @Test
    void cannotUnsetCurrentDefault() {
        DeliveryAddressRequest request = request(false);
        validMaster(request);
        String id = UUID.randomUUID().toString();
        DeliveryAddressEntity current = new DeliveryAddressEntity();
        current.setIsDefault(true);
        when(deliveryAddressRepository.findActiveOwned(id, "user-uuid")).thenReturn(current);

        assertThrows(BusinessException.class, () -> service.update(id, request));
        verify(deliveryAddressRepository, never()).update(any());
    }

    @Test
    void returnsActiveDefaultForAuthenticatedUser() {
        DeliveryAddressEntity entity = addressEntity();
        when(deliveryAddressRepository.findActiveDefaults("user-uuid"))
                .thenReturn(java.util.Collections.singletonList(entity));

        DeliveryAddressResponse response = service.getDefault();
        assertEquals("address-id", response.getId());
        assertTrue(response.getIsDefault());
        verify(deliveryAddressRepository).findActiveDefaults("user-uuid");
    }

    @Test
    void rejectsWhenDefaultAddressDoesNotExist() {
        when(deliveryAddressRepository.findActiveDefaults("user-uuid"))
                .thenReturn(java.util.Collections.emptyList());
        assertThrows(BusinessException.class, () -> service.getDefault());
    }

    @Test
    void rejectsDuplicateActiveDefaults() {
        when(deliveryAddressRepository.findActiveDefaults("user-uuid"))
                .thenReturn(java.util.Arrays.asList(addressEntity(), addressEntity()));
        assertThrows(BusinessException.class, () -> service.getDefault());
    }

    @Test
    void updatesOwnedActiveAddress() {
        DeliveryAddressRequest request = request(false);
        validMaster(request);
        String id = UUID.randomUUID().toString();
        DeliveryAddressEntity current = addressEntity();
        current.setIsDefault(false);
        when(deliveryAddressRepository.findActiveOwned(id, "user-uuid")).thenReturn(current);

        DeliveryAddressResponse response = service.update(id, request);

        assertEquals(id, response.getId());
        verify(deliveryAddressRepository).lockUser("user-uuid");
        verify(deliveryAddressRepository).update(any(DeliveryAddressEntity.class));
    }

    @Test
    void replacingDefaultIsAtomicAndClearsOldDefaultFirst() {
        DeliveryAddressRequest request = request(true);
        validMaster(request);
        String id = UUID.randomUUID().toString();
        DeliveryAddressEntity current = addressEntity();
        current.setIsDefault(false);
        when(deliveryAddressRepository.findActiveOwned(id, "user-uuid")).thenReturn(current);

        service.update(id, request);

        org.mockito.InOrder order = inOrder(deliveryAddressRepository);
        order.verify(deliveryAddressRepository).lockUser("user-uuid");
        order.verify(deliveryAddressRepository).lockActiveAddresses("user-uuid");
        order.verify(deliveryAddressRepository).findActiveOwned(id, "user-uuid");
        order.verify(deliveryAddressRepository).clearDefault("user-uuid");
        order.verify(deliveryAddressRepository).update(any(DeliveryAddressEntity.class));
    }

    @Test
    void rejectsUnauthenticatedMutation() {
        when(httpServletRequest.getAttribute("userObject")).thenReturn(null);
        assertThrows(BusinessException.class, () -> service.create(request(true)));
        verifyNoInteractions(deliveryAddressRepository, thailandAddressRepository);
    }

    private void validMaster(DeliveryAddressRequest request) {
        when(thailandAddressRepository.isValidAddress(
                request.getProvince(), request.getDistrict(), request.getSubDistrict(), request.getPostalCode()))
                .thenReturn(true);
    }

    private DeliveryAddressRequest request(boolean isDefault) {
        DeliveryAddressRequest request = new DeliveryAddressRequest();
        request.setFirstName("Somchai");
        request.setLastName("Sailor");
        request.setAddressLine("1 Main Road");
        request.setProvince("Bangkok");
        request.setDistrict("Phra Nakhon");
        request.setSubDistrict("Phra Borom Maha Ratchawang");
        request.setPostalCode("10200");
        request.setIsDefault(isDefault);
        return request;
    }

    private DeliveryAddressEntity addressEntity() {
        DeliveryAddressEntity entity = new DeliveryAddressEntity();
        entity.setId("address-id");
        entity.setFirstName("Somchai");
        entity.setLastName("Sailor");
        entity.setAddressLine("1 Main Road");
        entity.setProvince("10");
        entity.setDistrict("1001");
        entity.setSubDistrict("100101");
        entity.setPostalCode("10200");
        entity.setIsDefault(true);
        return entity;
    }
}
