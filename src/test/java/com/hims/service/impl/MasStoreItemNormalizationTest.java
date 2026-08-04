package com.hims.service.impl;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MasStoreItemNormalizationTest {

    private final MasStoreItemServiceImp service = new MasStoreItemServiceImp();

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
}
