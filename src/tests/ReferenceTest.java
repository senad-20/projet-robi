package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import api.Command;
import api.Reference;
import api.ValueReference;
import stree.parser.SNode;

public class ReferenceTest {

	@Test
	void constructorShouldStoreReceiver() {
		Object receiverObject = "hello";
		Reference ref = new Reference(receiverObject);

		assertSame(receiverObject, ref.getReceiver());
	}

	@Test
	void setNameShouldStoreName() {
		Reference ref = new Reference("x");

		ref.setName("myRef");

		assertEquals("myRef", ref.getName());
	}

	@Test
	void addCommandShouldMakeCommandAvailable() {
		Reference ref = new Reference("x");
		Command cmd = (receiver, method) -> receiver;

		ref.addCommand("ping", cmd);

		assertTrue(ref.hasCommand("ping"));
	}

	@Test
	void hasCommandShouldReturnFalseForMissingCommand() {
		Reference ref = new Reference("x");

		assertFalse(ref.hasCommand("missing"));
	}

	@Test
	void runShouldDispatchToRegisteredCommand() {
		Reference ref = new Reference("receiver");
		ref.setName("obj");

		ref.addCommand("echo", (receiver, method) -> new ValueReference(method.get(2).contents()));

		SNode expr = TestParserTools.expr("(obj echo hello)");
		ValueReference result = (ValueReference) ref.run(expr);

		assertEquals("hello", result.getValue());
	}

	@Test
	void runShouldPassReceiverReferenceToCommand() {
		Reference ref = new Reference("receiver");
		ref.setName("obj");

		ref.addCommand("identity", (receiver, method) -> receiver);

		SNode expr = TestParserTools.expr("(obj identity)");
		Reference result = ref.run(expr);

		assertSame(ref, result);
	}

	@Test
	void runShouldThrowWhenCommandIsUnknown() {
		Reference ref = new Reference("receiver");
		ref.setName("obj");

		SNode expr = TestParserTools.expr("(obj missing)");

		Error error = assertThrows(Error.class, () -> ref.run(expr));

		assertTrue(error.getMessage().contains("Commande inconnue"));
		assertTrue(error.getMessage().contains("obj"));
	}

	@Test
	void runShouldAllowCommandToInspectWholeExpression() {
		Reference ref = new Reference("receiver");
		ref.setName("obj");

		ref.addCommand("count", (receiver, method) -> new ValueReference(method.size()));

		SNode expr = TestParserTools.expr("(obj count a b c)");
		ValueReference result = (ValueReference) ref.run(expr);

		assertEquals(5, result.getValue());
	}
}