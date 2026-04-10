package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import api.ValueReference;

public class ValueReferenceTest {

	@Test
	void shouldStoreIntegerValue() {
		ValueReference ref = new ValueReference(123);

		assertEquals(123, ref.getReceiver());
		assertEquals(123, ref.getValue());
	}

	@Test
	void shouldStoreBooleanValue() {
		ValueReference ref = new ValueReference(true);

		assertEquals(true, ref.getReceiver());
		assertEquals(true, ref.getValue());
	}

	@Test
	void shouldStoreStringValue() {
		ValueReference ref = new ValueReference("hello");

		assertEquals("hello", ref.getReceiver());
		assertEquals("hello", ref.getValue());
	}

	@Test
	void shouldStoreNullValue() {
		ValueReference ref = new ValueReference(null);

		assertNull(ref.getReceiver());
		assertNull(ref.getValue());
	}
}