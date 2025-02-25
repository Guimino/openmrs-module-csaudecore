package org.openmrs.module.csaudecore.idgen;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.openmrs.module.idgen.prefixprovider.LocationBasedPrefixProvider;
import org.openmrs.test.jupiter.BaseContextMockTest;

public class TarvNidPrefixProviderTest extends BaseContextMockTest {
	
	@Mock
	private LocationBasedPrefixProvider locationBasedPrefixProvider;
	
	@Test
    public void shouldNotAllowNegativeNumbers() {

        assertThrows(IllegalArgumentException.class, () -> {
            new NidPrefixProvider(locationBasedPrefixProvider) {
                @Override
                public int getServiceCode() {
                    return -1;
                }
            };
        });
    }
	
	@Test
    public void shouldNotAllowNumbersWithThreeDigitsOrMore() {

        assertThrows(IllegalArgumentException.class, () -> {
            new NidPrefixProvider(locationBasedPrefixProvider) {
                @Override
                public int getServiceCode() {
                    return 100;
                }
            };
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new NidPrefixProvider(locationBasedPrefixProvider) {
                @Override
                public int getServiceCode() {
                    return 1000;
                }
            };
        });
    }
}
