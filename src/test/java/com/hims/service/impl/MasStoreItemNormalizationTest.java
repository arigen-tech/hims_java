package com.hims.service.impl;

import com.hims.entity.repository.MasStoreItemRepository;
import com.hims.entity.repository.StoreItemFacilityMapRepository;
import com.hims.projection.MasStoreItemsProjection;
import com.hims.response.ApiResponse;
import com.hims.response.MasStoreItemResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MasStoreItemNormalizationTest {

    @Mock
    private MasStoreItemRepository masStoreItemRepository;

    @Mock
    private StoreItemFacilityMapRepository storeItemFacilityMapRepository;

    @InjectMocks
    private MasStoreItemServiceImp service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "sectionId", 10);
    }

    @Test
    void testNormalizeYN() {
        assertEquals("y", service.normalizeYN("y"));
        assertEquals("y", service.normalizeYN("Y"));
        assertEquals("y", service.normalizeYN(" y "));
        assertEquals("n", service.normalizeYN("n"));
        assertEquals("n", service.normalizeYN("N"));
        assertEquals("n", service.normalizeYN(" N "));
        assertNull(service.normalizeYN(null));
        assertEquals("invalid", service.normalizeYN("invalid"));
        assertEquals("", service.normalizeYN(""));
    }

    @Test
    void testGetAllMasStoreItemWithOutStockPaginated() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        MasStoreItemsProjection projectionMock = mock(MasStoreItemsProjection.class);
        when(projectionMock.getItemId()).thenReturn(1L);
        when(projectionMock.getPvmsNo()).thenReturn("PVMS-123");
        when(projectionMock.getNomenclature()).thenReturn("Nomenclature 123");
        
        Page<MasStoreItemsProjection> projectionPage = new PageImpl<>(List.of(projectionMock), pageable, 1);
        
        when(masStoreItemRepository.findItemsWithOutStockPaginated(
                anyInt(), any(), any(), any(), any(), any(Pageable.class)
        )).thenReturn(projectionPage);
        
        when(storeItemFacilityMapRepository.findFacilityByItemIds(anyList())).thenReturn(Collections.emptyList());

        // Act
        ApiResponse<Page<MasStoreItemResponse>> apiResponse = service.getAllMasStoreItemWithOutStockPaginated(
                0, 0, 10, "test", null, null
        );

        // Assert
        assertNotNull(apiResponse);
        assertEquals(200, apiResponse.getStatus());
        assertNotNull(apiResponse.getResponse());
        assertEquals(1, apiResponse.getResponse().getTotalElements());
        assertEquals("Nomenclature 123", apiResponse.getResponse().getContent().get(0).getNomenclature());
        
        verify(masStoreItemRepository, times(1)).findItemsWithOutStockPaginated(
                eq(0), eq(10), eq("test"), eq(null), eq(null), any(Pageable.class)
        );
    }
}
