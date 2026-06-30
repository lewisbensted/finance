package finance.unit.entities;

import finance.entities.HoldingId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class HoldingIdTests {
    HoldingId testId1;
    HoldingId testId2;

    @BeforeEach
    void setUp() {
        testId1 = new HoldingId(1L, "test");
        testId2 = new HoldingId(1L, "test");
    }


    @Test
    void equalObjects() {
        assertEquals(testId1, testId1);
        assertEquals(testId1.hashCode(), testId1.hashCode());
    }

    @Test
    void identicalObjects() {
        assertEquals(testId1, testId2);
        assertEquals(testId1.hashCode(), testId2.hashCode());
    }

    @Test
    void differentObjects() {
        HoldingId testId3 = new HoldingId(2L, "test");
        HoldingId testId4 = new HoldingId(1L, "testt");
        assertNotEquals(testId1, testId3);
        assertNotEquals(testId1, testId4);
    }

    @Test
    void incorrectType() {
        Object obj = new Object();
        assertNotEquals(obj, testId1);
        assertNotEquals(null, testId1);
    }
}
