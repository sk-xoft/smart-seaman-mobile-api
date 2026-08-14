package com.seaman.service;

import com.seaman.entity.DeliveryAddressEntity;
import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.request.DeliveryAddressRequest;
import com.seaman.repository.DeliveryAddressRepository;
import com.seaman.repository.DocumentRenewalCreateRepository;
import com.seaman.repository.DocumentRenewalFoundationRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class DeliveryAddressServiceTest {
    @Mock DeliveryAddressRepository deliveryAddressRepository;
    @Mock DocumentRenewalCreateRepository documentRenewalCreateRepository;
    @Mock DocumentRenewalFoundationRepository documentRenewalFoundationRepository;
    @Mock ThailandAddressRepository thailandAddressRepository;
    @Mock HttpServletRequest httpServletRequest;

    private DeliveryAddressService service;
    private UsersEntity user;

    @BeforeEach
    void setUp() {
        service = new DeliveryAddressService(
                deliveryAddressRepository, documentRenewalCreateRepository, documentRenewalFoundationRepository,
                thailandAddressRepository, httpServletRequest);
        user = new UsersEntity();
        user.setMobileUuid("user-uuid");
        user.setMobileNumber("0812345678");
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
        when(documentRenewalCreateRepository.findDeliveryAddressSnapshotById(id, "user-uuid")).thenReturn(null);

        assertThrows(BusinessException.class, () -> service.update(id, request));
        verify(deliveryAddressRepository, never()).update(any());
    }

    @Test
    void updatesRenewalSnapshotWhenAddressIdIsSnapshotId() {
        DeliveryAddressRequest request = request(true);
        validMaster(request);
        String id = UUID.randomUUID().toString();
        DeliveryAddressEntity snapshot = addressEntity();
        snapshot.setId(id);
        snapshot.setMobileNumber("0812345678");
        when(deliveryAddressRepository.findActiveOwned(id, "user-uuid")).thenReturn(null);
        when(documentRenewalCreateRepository.findDeliveryAddressSnapshotById(id, "user-uuid"))
                .thenReturn(snapshot);

        DeliveryAddressResponse response = service.update(id, request);

        assertEquals(id, response.getId());
        assertEquals("0812345678", response.getMobileNumber());
        verify(deliveryAddressRepository, never()).update(any());
        verify(documentRenewalCreateRepository).updateDeliveryAddressSnapshot(any(DeliveryAddressEntity.class));
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
        assertEquals("1 Main Road ตำบลพระบรมมหาราชวัง อำเภอพระนคร จังหวัดกรุงเทพมหานคร 10200",
                response.getDescription());
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

    @Test
    void renewalAddressCreatesUserAddressWhenUserHasNoneAndSnapshotsRequest() {
        DeliveryAddressRequest request = request(true);
        validMaster(request);
        DocumentRenewalRequestEntity renewal = renewalRequest();
        when(documentRenewalFoundationRepository.lockOwnedRequestByNo("REQ001", "user-uuid"))
                .thenReturn(renewal);
        when(documentRenewalCreateRepository.countDeliveryAddressSnapshot("request-id", "user-uuid"))
                .thenReturn(0);
        when(deliveryAddressRepository.countActive("user-uuid")).thenReturn(0);
        when(documentRenewalCreateRepository.insertDeliveryAddressSnapshot(
                eq("request-id"), any(DeliveryAddressEntity.class), eq("0812345678")))
                .thenReturn("snapshot-address-id");

        DeliveryAddressResponse response = service.createForRenewal("REQ001", request);

        assertEquals("snapshot-address-id", response.getId());
        assertEquals("0812345678", response.getMobileNumber());
        assertTrue(response.getIsDefault());
        verify(deliveryAddressRepository).insert(any(DeliveryAddressEntity.class));
        verify(documentRenewalCreateRepository).insertDeliveryAddressSnapshot(
                eq("request-id"), any(DeliveryAddressEntity.class), eq("0812345678"));
    }

    @Test
    void renewalAddressDoesNotCreateDuplicateUserAddressWhenOneAlreadyExists() {
        DeliveryAddressRequest request = request(false);
        validMaster(request);
        DocumentRenewalRequestEntity renewal = renewalRequest();
        when(documentRenewalFoundationRepository.lockOwnedRequestByNo("REQ001", "user-uuid"))
                .thenReturn(renewal);
        when(documentRenewalCreateRepository.countDeliveryAddressSnapshot("request-id", "user-uuid"))
                .thenReturn(0);
        when(deliveryAddressRepository.countActive("user-uuid")).thenReturn(1);
        when(documentRenewalCreateRepository.insertDeliveryAddressSnapshot(
                eq("request-id"), any(DeliveryAddressEntity.class), eq("0812345678")))
                .thenReturn("snapshot-address-id");

        DeliveryAddressResponse response = service.createForRenewal("REQ001", request);

        assertEquals("snapshot-address-id", response.getId());
        assertEquals("0812345678", response.getMobileNumber());
        verify(deliveryAddressRepository, never()).insert(any(DeliveryAddressEntity.class));
        verify(documentRenewalCreateRepository).insertDeliveryAddressSnapshot(
                eq("request-id"), any(DeliveryAddressEntity.class), eq("0812345678"));
    }

    @Test
    void renewalAddressAcceptsRequestIdPathParameter() {
        DeliveryAddressRequest request = request(false);
        validMaster(request);
        DocumentRenewalRequestEntity renewal = renewalRequest();
        String requestId = UUID.randomUUID().toString();
        renewal.setId(requestId);
        when(documentRenewalFoundationRepository.lockOwnedRequest(requestId, "user-uuid"))
                .thenReturn(renewal);
        when(documentRenewalCreateRepository.countDeliveryAddressSnapshot(requestId, "user-uuid"))
                .thenReturn(0);
        when(deliveryAddressRepository.countActive("user-uuid")).thenReturn(1);
        when(documentRenewalCreateRepository.insertDeliveryAddressSnapshot(
                eq(requestId), any(DeliveryAddressEntity.class), eq("0812345678")))
                .thenReturn("snapshot-address-id");

        DeliveryAddressResponse response = service.createForRenewal(requestId, request);

        assertEquals("snapshot-address-id", response.getId());
        verify(documentRenewalFoundationRepository).lockOwnedRequest(requestId, "user-uuid");
        verify(documentRenewalFoundationRepository, never()).lockOwnedRequestByNo(any(), any());
        verify(documentRenewalCreateRepository).insertDeliveryAddressSnapshot(
                eq(requestId), any(DeliveryAddressEntity.class), eq("0812345678"));
    }

    @Test
    void renewalAddressRejectsDuplicateSnapshot() {
        DeliveryAddressRequest request = request(false);
        validMaster(request);
        DocumentRenewalRequestEntity renewal = renewalRequest();
        when(documentRenewalFoundationRepository.lockOwnedRequestByNo("REQ001", "user-uuid"))
                .thenReturn(renewal);
        when(documentRenewalCreateRepository.countDeliveryAddressSnapshot("request-id", "user-uuid"))
                .thenReturn(1);

        assertThrows(BusinessException.class, () -> service.createForRenewal("REQ001", request));
        verify(deliveryAddressRepository, never()).insert(any(DeliveryAddressEntity.class));
        verify(documentRenewalCreateRepository, never()).insertDeliveryAddressSnapshot(any(), any(), any());
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
        entity.setDescription("1 Main Road ตำบลพระบรมมหาราชวัง อำเภอพระนคร จังหวัดกรุงเทพมหานคร 10200");
        entity.setIsDefault(true);
        return entity;
    }

    private DocumentRenewalRequestEntity renewalRequest() {
        DocumentRenewalRequestEntity entity = new DocumentRenewalRequestEntity();
        entity.setId("request-id");
        entity.setRequestNo("REQ001");
        entity.setMobileUserUuid("user-uuid");
        return entity;
    }
}
