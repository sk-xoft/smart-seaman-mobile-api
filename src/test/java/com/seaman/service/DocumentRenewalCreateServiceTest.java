package com.seaman.service;

import com.seaman.constant.DocumentRenewalAction;
import com.seaman.constant.DocumentRenewalStatus;
import com.seaman.entity.DeliveryAddressEntity;
import com.seaman.entity.DocumentRequestItemEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.request.DocumentRenewalCreateRequest;
import com.seaman.model.response.DocumentRenewalCreateResponse;
import com.seaman.model.response.DocumentRenewalPriceResponse;
import com.seaman.repository.DeliveryAddressRepository;
import com.seaman.repository.DocumentRenewalCreateRepository;
import com.seaman.repository.DocumentRenewalFoundationRepository;
import com.seaman.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentRenewalCreateServiceTest {
    @Mock DocumentRenewalCreateRepository createRepository;
    @Mock DocumentRenewalFoundationRepository foundationRepository;
    @Mock DocumentRenewalService renewalService;
    @Mock DocumentRepository documentRepository;
    @Mock DeliveryAddressRepository deliveryAddressRepository;
    @Mock HttpServletRequest httpServletRequest;

    private DocumentRenewalCreateService service;
    private DocumentRenewalCreateRequest input;
    private DeliveryAddressEntity address;

    @BeforeEach
    void setUp() {
        service = new DocumentRenewalCreateService(createRepository, foundationRepository,
                renewalService, documentRepository, deliveryAddressRepository, httpServletRequest);
        UsersEntity user = new UsersEntity();
        user.setMobileUuid("mobile-user-uuid");
        lenient().when(httpServletRequest.getAttribute("userObject")).thenReturn(user);
        input = new DocumentRenewalCreateRequest();
        input.setDocumentCode("doc001");
        input.setDeliveryAddressId(UUID.randomUUID().toString());
        address = new DeliveryAddressEntity();
        address.setId(input.getDeliveryAddressId());
    }

    @Test
    void createsUnpaidDraftWithServerOwnedValuesAndSnapshots() {
        DocumentRenewalPriceResponse price = price();
        when(renewalService.price("DOC001")).thenReturn(price);
        when(createRepository.countActiveRequiredItems("DOC001")).thenReturn(4);
        when(documentRepository.findMissingItemsByUserAndDocumentCode("mobile-user-uuid", "DOC001"))
                .thenReturn(Collections.emptyList());
        when(deliveryAddressRepository.findActiveOwned(input.getDeliveryAddressId(), "mobile-user-uuid"))
                .thenReturn(address);
        when(createRepository.nextRequestNo(anyString())).thenReturn("260700001");
        when(foundationRepository.findActiveStatusId(DocumentRenewalStatus.PAYMENT_PENDING))
                .thenReturn("payment-status-id");
        when(createRepository.insertRequestItems(anyString(), eq("DOC001"))).thenReturn(4);

        DocumentRenewalCreateResponse response = service.create(input);

        assertEquals("260700001", response.getRequestNo());
        assertEquals("DOC001", response.getDocumentCode());
        assertEquals("PAYMENT_PENDING", response.getStatus());
        assertEquals(new BigDecimal("1500.00"), response.getAmount());
        verify(createRepository).insertRequest(anyString(), eq("260700001"),
                eq("mobile-user-uuid"), eq("DOC001"), eq("payment-status-id"),
                eq("price-setting-id"), eq(input.getDeliveryAddressId()),
                eq(new BigDecimal("1500.00")));
        verify(foundationRepository).appendTransaction(anyString(), eq(DocumentRenewalAction.CREATE),
                isNull(), eq(DocumentRenewalStatus.PAYMENT_PENDING),
                eq("Unpaid draft created"), eq("mobile-user-uuid"));
    }

    @Test
    void ignoresCompleteRequiredItemsWhenCreatingRequest() {
        DocumentRenewalPriceResponse price = price();
        when(renewalService.price("DOC001")).thenReturn(price);
        when(createRepository.countActiveRequiredItems("DOC001")).thenReturn(2);
        when(documentRepository.findMissingItemsByUserAndDocumentCode("mobile-user-uuid", "DOC001"))
                .thenReturn(Arrays.asList(requiredItem("COMPLETE"), requiredItem("COMPLETE")));
        when(deliveryAddressRepository.findActiveOwned(input.getDeliveryAddressId(), "mobile-user-uuid"))
                .thenReturn(address);
        when(createRepository.nextRequestNo(anyString())).thenReturn("260700001");
        when(foundationRepository.findActiveStatusId(DocumentRenewalStatus.PAYMENT_PENDING))
                .thenReturn("payment-status-id");
        when(createRepository.insertRequestItems(anyString(), eq("DOC001"))).thenReturn(2);

        DocumentRenewalCreateResponse response = service.create(input);

        assertEquals("260700001", response.getRequestNo());
        verify(createRepository).insertRequest(anyString(), eq("260700001"),
                eq("mobile-user-uuid"), eq("DOC001"), eq("payment-status-id"),
                eq("price-setting-id"), eq(input.getDeliveryAddressId()),
                eq(new BigDecimal("1500.00")));
    }

    @Test
    void rejectsMissingRequiredUploadsBeforeCreatingRequest() {
        when(renewalService.price("DOC001")).thenReturn(price());
        when(createRepository.countActiveRequiredItems("DOC001")).thenReturn(4);
        when(documentRepository.findMissingItemsByUserAndDocumentCode("mobile-user-uuid", "DOC001"))
                .thenReturn(Collections.singletonList(new DocumentRequestItemEntity()));

        assertThrows(BusinessException.class, () -> service.create(input));
        verify(createRepository, never()).insertRequest(anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void rejectsAddressNotOwnedByAuthenticatedUser() {
        when(renewalService.price("DOC001")).thenReturn(price());
        when(createRepository.countActiveRequiredItems("DOC001")).thenReturn(4);
        when(documentRepository.findMissingItemsByUserAndDocumentCode("mobile-user-uuid", "DOC001"))
                .thenReturn(Collections.emptyList());
        when(deliveryAddressRepository.findActiveOwned(input.getDeliveryAddressId(), "mobile-user-uuid"))
                .thenReturn(null);

        assertThrows(BusinessException.class, () -> service.create(input));
        verify(createRepository, never()).insertRequest(anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void rejectsDocumentWithoutRequiredItemConfiguration() {
        when(renewalService.price("DOC001")).thenReturn(price());
        when(createRepository.countActiveRequiredItems("DOC001")).thenReturn(0);
        assertThrows(BusinessException.class, () -> service.create(input));
        verifyNoInteractions(documentRepository, deliveryAddressRepository);
    }

    private DocumentRenewalPriceResponse price() {
        DocumentRenewalPriceResponse price = new DocumentRenewalPriceResponse();
        price.setPriceSettingId("price-setting-id");
        price.setGovernmentFee(new BigDecimal("1000.00"));
        price.setDocumentProcessingFee(new BigDecimal("400.00"));
        price.setShippingFee(new BigDecimal("100.00"));
        price.setShippingDiscount(BigDecimal.ZERO.setScale(2));
        price.setServiceFeeDiscount(BigDecimal.ZERO.setScale(2));
        price.setTotal(new BigDecimal("1500.00"));
        price.setEffectiveFrom(LocalDate.of(2026, 7, 5));
        return price;
    }

    private DocumentRequestItemEntity requiredItem(String status) {
        DocumentRequestItemEntity item = new DocumentRequestItemEntity();
        item.setDocumentStatus(status);
        return item;
    }
}
