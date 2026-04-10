package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import api.Environment;
import api.Interpreter;
import api.Reference;
import api.ValueReference;

public class InterpreterTest {

	private Environment environment;
	private Interpreter interpreter;

	@BeforeEach
	void setUp() {
		environment = new Environment();
		interpreter = new Interpreter();
	}

	@Test
	void shouldEvaluateSetWithIntegerLiteral() {
		Reference result = interpreter.compute(environment, TestParserTools.expr("(set i 5)"));

		assertEquals(5, result.getReceiver());
		assertTrue(environment.hasReference("i"));
		assertEquals(5, environment.getReferenceByName("i").getReceiver());
	}

	@Test
	void shouldOverwriteVariableWithSet() {
		interpreter.compute(environment, TestParserTools.expr("(set i 1)"));
		interpreter.compute(environment, TestParserTools.expr("(set i 9)"));

		assertEquals(9, environment.getReferenceByName("i").getReceiver());
	}

	@Test
	void shouldSetVariableFromExpression() {
		interpreter.compute(environment, TestParserTools.expr("(set i (+ 2 3))"));

		assertEquals(5, environment.getReferenceByName("i").getReceiver());
	}

	@Test
	void shouldAddTwoIntegers() {
		Reference result = interpreter.compute(environment, TestParserTools.expr("(+ 2 3)"));

		assertEquals(5, result.getReceiver());
	}

	@Test
	void shouldSubtractTwoIntegers() {
		Reference result = interpreter.compute(environment, TestParserTools.expr("(- 10 4)"));

		assertEquals(6, result.getReceiver());
	}

	@Test
	void shouldMultiplyTwoIntegers() {
		Reference result = interpreter.compute(environment, TestParserTools.expr("(* 6 7)"));

		assertEquals(42, result.getReceiver());
	}

	@Test
	void shouldDivideTwoIntegers() {
		Reference result = interpreter.compute(environment, TestParserTools.expr("(/ 20 5)"));

		assertEquals(4, result.getReceiver());
	}

	@Test
	void shouldEvaluateNestedArithmetic() {
		Reference result = interpreter.compute(environment,
				TestParserTools.expr("(+ (* 2 3) (- 10 4))"));

		assertEquals(12, result.getReceiver());
	}

	@Test
	void shouldUseVariablesInsideArithmetic() {
		interpreter.compute(environment, TestParserTools.expr("(set x 7)"));
		interpreter.compute(environment, TestParserTools.expr("(set y 8)"));

		Reference result = interpreter.compute(environment, TestParserTools.expr("(+ x y)"));

		assertEquals(15, result.getReceiver());
	}

	@Test
	void shouldCompareLessThan() {
		Reference result = interpreter.compute(environment, TestParserTools.expr("(< 2 3)"));

		assertEquals(true, result.getReceiver());
	}

	@Test
	void shouldCompareGreaterThan() {
		Reference result = interpreter.compute(environment, TestParserTools.expr("(> 5 3)"));

		assertEquals(true, result.getReceiver());
	}

	@Test
	void shouldCompareEquality() {
		Reference result = interpreter.compute(environment, TestParserTools.expr("(= 5 5)"));

		assertEquals(true, result.getReceiver());
	}

	@Test
	void shouldReturnFalseForFailedComparison() {
		Reference result = interpreter.compute(environment, TestParserTools.expr("(= 5 6)"));

		assertEquals(false, result.getReceiver());
	}

	@Test
	void shouldEvaluateIfThenBranchWhenConditionIsTrue() {
		Reference result = interpreter.compute(environment, TestParserTools.expr("(if true 10 20)"));

		assertEquals(10, result.getReceiver());
	}

	@Test
	void shouldEvaluateIfElseBranchWhenConditionIsFalse() {
		Reference result = interpreter.compute(environment, TestParserTools.expr("(if false 10 20)"));

		assertEquals(20, result.getReceiver());
	}

	@Test
	void shouldReturnNullReferenceForIfWithoutElseAndFalseCondition() {
		Reference result = interpreter.compute(environment, TestParserTools.expr("(if false 10)"));

		assertNull(result.getReceiver());
	}

	@Test
	void shouldUseComparisonInsideIfCondition() {
		Reference result = interpreter.compute(environment, TestParserTools.expr("(if (< 2 3) 100 200)"));

		assertEquals(100, result.getReceiver());
	}

	@Test
	void shouldExecuteBeginInOrderAndReturnLastValue() {
		Reference result = interpreter.compute(environment,
				TestParserTools.expr("(begin (set i 1) (set i (+ i 3)) (* i 2))"));

		assertEquals(8, result.getReceiver());
		assertEquals(4, environment.getReferenceByName("i").getReceiver());
	}

	@Test
	void shouldExecuteWhileUntilConditionBecomesFalse() {
		Reference result = interpreter.compute(environment,
				TestParserTools.expr("(begin (set i 0) (while (< i 4) (set i (+ i 1))) i)"));

		assertEquals(4, result.getReceiver());
		assertEquals(4, environment.getReferenceByName("i").getReceiver());
	}

	@Test
	void whileShouldReturnLastBodyValue() {
		Reference result = interpreter.compute(environment,
				TestParserTools.expr("(begin (set i 0) (while (< i 3) (set i (+ i 1))))"));

		assertEquals(3, result.getReceiver());
	}

	@Test
	void whileWithSeveralBodyExpressionsShouldReturnLastExecutedExpression() {
		Reference result = interpreter.compute(environment,
				TestParserTools.expr("(begin (set i 0) (set out 0) (while (< i 3) (set i (+ i 1)) (set out (* i 10))) out)"));

		assertEquals(30, result.getReceiver());
		assertEquals(3, environment.getReferenceByName("i").getReceiver());
	}

	@Test
	void shouldAcceptBooleanVariableInIf() {
		interpreter.compute(environment, TestParserTools.expr("(set ok true)"));

		Reference result = interpreter.compute(environment, TestParserTools.expr("(if ok 1 0)"));

		assertEquals(1, result.getReceiver());
	}

	@Test
	void shouldDispatchToReferenceCommandWhenHeadMatchesEnvironmentReference() {
		Reference obj = new Reference("receiver");
		obj.addCommand("echo", (receiver, method) -> new ValueReference(method.get(2).contents()));
		environment.addReference("obj", obj);

		Reference result = interpreter.compute(environment, TestParserTools.expr("(obj echo hello)"));

		assertEquals("hello", result.getReceiver());
	}

	@Test
	void shouldThrowForUnknownReceiverName() {
		Error error = assertThrows(Error.class,
				() -> interpreter.compute(environment, TestParserTools.expr("(unknown doSomething)")));

		assertTrue(error.getMessage().contains("Référence inconnue"));
	}

	@Test
	void shouldTreatUnknownAtomAsStringValue() {
		Reference result = interpreter.compute(environment, TestParserTools.expr("(set name hello)"));

		assertEquals("hello", result.getReceiver());
		assertEquals("hello", environment.getReferenceByName("name").getReceiver());
	}

	@Test
	void shouldThrowWhenArithmeticGetsNonIntegerLeftOperand() {
		Error error = assertThrows(Error.class,
				() -> interpreter.compute(environment, TestParserTools.expr("(+ hello 2)")));

		assertTrue(error.getMessage().contains("Valeur entière attendue"));
	}

	@Test
	void shouldThrowWhenArithmeticGetsNonIntegerRightOperand() {
		Error error = assertThrows(Error.class,
				() -> interpreter.compute(environment, TestParserTools.expr("(+ 2 hello)")));

		assertTrue(error.getMessage().contains("Valeur entière attendue"));
	}

	@Test
	void shouldThrowWhenIfConditionIsNotBoolean() {
		Error error = assertThrows(Error.class,
				() -> interpreter.compute(environment, TestParserTools.expr("(if 12 1 0)")));

		assertTrue(error.getMessage().contains("Booléen attendu"));
	}

	@Test
	void shouldThrowWhenWhileConditionIsNotBoolean() {
		Error error = assertThrows(Error.class,
				() -> interpreter.compute(environment, TestParserTools.expr("(while 12 (set i 1))")));

		assertTrue(error.getMessage().contains("Booléen attendu"));
	}
}