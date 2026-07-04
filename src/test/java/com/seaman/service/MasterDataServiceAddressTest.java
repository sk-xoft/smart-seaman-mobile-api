package com.seaman.service;

import com.seaman.entity.ThailandAddressEntity;
import com.seaman.exception.BusinessException;
import com.seaman.repository.CompanyRepository;
import com.seaman.repository.DocumentRepository;
import com.seaman.repository.PositionRepository;
import com.seaman.repository.ThailandAddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MasterDataServiceAddressTest {

    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private PositionRepository positionRepository;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private ThailandAddressRepository thailandAddressRepository;

    private MasterDataService service;

    @BeforeEach
    void setUp() {
        service = new MasterDataService(
                httpServletRequest,
                companyRepository,
                positionRepository,
                documentRepository,
                thailandAddressRepository);
    }

    @Test
    void provincesUseEnglishNameWhenRequested() {
        ThailandAddressEntity province = address(10, "กรุงเทพมหานคร", "Bangkok", null);
        when(httpServletRequest.getHeader("Accept-Language")).thenReturn("EN");
        when(thailandAddressRepository.findProvinces()).thenReturn(Collections.singletonList(province));

        var result = service.provinces();

        assertEquals("10", result.get(0).getCode());
        assertEquals("Bangkok", result.get(0).getName());
        assertEquals("กรุงเทพมหานคร", result.get(0).getNameTh());
    }

    @Test
    void districtsUseThaiNameByDefaultAndFilterByProvinceCode() {
        ThailandAddressEntity district = address(1001, "เขต พระนคร", "Phra Nakhon", null);
        when(thailandAddressRepository.findDistrictsByProvinceCode(10))
                .thenReturn(Collections.singletonList(district));

        var result = service.districts(10);

        assertEquals("เขต พระนคร", result.get(0).getName());
        verify(thailandAddressRepository).findDistrictsByProvinceCode(10);
    }

    @Test
    void subdistrictsIncludePostalCode() {
        ThailandAddressEntity subdistrict = address(
                100101, "พระบรมมหาราชวัง", "Phra Borom Maha Ratchawang", 10200);
        when(thailandAddressRepository.findSubdistrictsByDistrictCode(1001))
                .thenReturn(Collections.singletonList(subdistrict));

        var result = service.subdistricts(1001);

        assertEquals("10200", result.get(0).getPostalCode());
        verify(thailandAddressRepository).findSubdistrictsByDistrictCode(1001);
    }

    @Test
    void districtRejectsNonPositiveProvinceCode() {
        assertThrows(BusinessException.class, () -> service.districts(0));
    }

    @Test
    void subdistrictReturnsEmptyListWhenDistrictHasNoRows() {
        when(thailandAddressRepository.findSubdistrictsByDistrictCode(9999))
                .thenReturn(Collections.emptyList());

        assertEquals(0, service.subdistricts(9999).size());
    }

    private ThailandAddressEntity address(
            Integer code, String nameTh, String nameEn, Integer postalCode) {
        ThailandAddressEntity entity = new ThailandAddressEntity();
        entity.setCode(code);
        entity.setNameInThai(nameTh);
        entity.setNameInEnglish(nameEn);
        entity.setPostalCode(postalCode);
        return entity;
    }
}
