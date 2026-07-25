package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.entity.DeliveryAddressEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.request.DeliveryAddressRequest;
import com.seaman.model.response.DeliveryAddressResponse;
import com.seaman.repository.DeliveryAddressRepository;
import com.seaman.repository.ThailandAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryAddressService {

    private final DeliveryAddressRepository deliveryAddressRepository;
    private final ThailandAddressRepository thailandAddressRepository;
    private final HttpServletRequest httpServletRequest;

    public DeliveryAddressResponse getDefault() {
        List<DeliveryAddressEntity> addresses =
                deliveryAddressRepository.findActiveDefaults(currentUserUuid());
        if (addresses.isEmpty()) {
            throw new BusinessException(AppStatus.DATA_NOT_FOUND, "deliveryAddress");
        }
        if (addresses.size() != 1) {
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE,
                    "Duplicate active default delivery address");
        }
        return toResponse(addresses.get(0));
    }

    @Transactional
    public DeliveryAddressResponse create(DeliveryAddressRequest request) {
        String mobileUserUuid = currentUserUuid();
        validateMasterAddress(request);
        deliveryAddressRepository.lockUser(mobileUserUuid);
        deliveryAddressRepository.lockActiveAddresses(mobileUserUuid);

        boolean isDefault = deliveryAddressRepository.countActive(mobileUserUuid) == 0
                || Boolean.TRUE.equals(request.getIsDefault());
        if (isDefault) {
            deliveryAddressRepository.clearDefault(mobileUserUuid);
        }

        DeliveryAddressEntity entity = toEntity(UUID.randomUUID().toString(), mobileUserUuid, request);
        entity.setIsDefault(isDefault);
        deliveryAddressRepository.insert(entity);
        return toResponse(entity);
    }

    @Transactional
    public DeliveryAddressResponse update(String addressId, DeliveryAddressRequest request) {
        String mobileUserUuid = currentUserUuid();
        validateAddressId(addressId);
        validateMasterAddress(request);
        deliveryAddressRepository.lockUser(mobileUserUuid);
        deliveryAddressRepository.lockActiveAddresses(mobileUserUuid);

        DeliveryAddressEntity current = deliveryAddressRepository.findActiveOwned(addressId, mobileUserUuid);
        if (current == null) {
            throw new BusinessException(AppStatus.DATA_NOT_FOUND, "deliveryAddress");
        }
        if (Boolean.TRUE.equals(current.getIsDefault()) && !Boolean.TRUE.equals(request.getIsDefault())) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, "isDefault");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            deliveryAddressRepository.clearDefault(mobileUserUuid);
        }

        DeliveryAddressEntity entity = toEntity(addressId, mobileUserUuid, request);
        deliveryAddressRepository.update(entity);
        return toResponse(entity);
    }

    private String currentUserUuid() {
        UsersEntity user = (UsersEntity) httpServletRequest.getAttribute("userObject");
        if (user == null || user.getMobileUuid() == null || user.getMobileUuid().trim().isEmpty()) {
            throw new BusinessException(AppStatus.USERNAME_IS_NOT_FOUND_SECURITY_CONTEXT, "userObject");
        }
        return user.getMobileUuid();
    }

    private void validateAddressId(String addressId) {
        try {
            UUID.fromString(addressId);
        } catch (Exception ex) {
            throw new BusinessException(AppStatus.INVALID_UUID, "addressId");
        }
    }

    private void validateMasterAddress(DeliveryAddressRequest request) {
        if (!thailandAddressRepository.isValidAddress(
                request.getProvince(), request.getDistrict(), request.getSubDistrict(), request.getPostalCode())) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, "address");
        }
    }

    private DeliveryAddressEntity toEntity(
            String id, String mobileUserUuid, DeliveryAddressRequest request) {
        DeliveryAddressEntity entity = new DeliveryAddressEntity();
        entity.setId(id);
        entity.setMobileUserUuid(mobileUserUuid);
        entity.setFirstName(request.getFirstName().trim());
        entity.setLastName(request.getLastName().trim());
        entity.setAddressLine(request.getAddressLine().trim());
        entity.setProvince(request.getProvince().trim());
        entity.setDistrict(request.getDistrict().trim());
        entity.setSubDistrict(request.getSubDistrict().trim());
        entity.setPostalCode(request.getPostalCode());
        entity.setIsDefault(request.getIsDefault());
        entity.setIsActive("YES");
        return entity;
    }

    private DeliveryAddressResponse toResponse(DeliveryAddressEntity entity) {
        DeliveryAddressResponse response = new DeliveryAddressResponse();
        response.setId(entity.getId());
        response.setFirstName(entity.getFirstName());
        response.setLastName(entity.getLastName());
        response.setAddressLine(entity.getAddressLine());
        response.setProvince(entity.getProvince());
        response.setDistrict(entity.getDistrict());
        response.setSubDistrict(entity.getSubDistrict());
        response.setPostalCode(entity.getPostalCode());
        response.setMobileNumber(entity.getMobileNumber());
        response.setDescription(entity.getDescription());
        response.setIsDefault(entity.getIsDefault());
        return response;
    }
}
