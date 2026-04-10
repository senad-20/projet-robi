package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import api.Environment;
import api.Reference;
import api.ValueReference;

public class EnvironmentTest {

	@Test
	void constructorShouldCreateEmptyEnvironment() {
		Environment environment = new Environment();

		assertFalse(environment.hasReference("x"));
	}

	@Test
	void addReferenceShouldRegisterReferenceAndSetItsName() {
		Environment environment = new Environment();
		Reference ref = new ValueReference(42);

		environment.addReference("x", ref);

		assertTrue(environment.hasReference("x"));
		assertSame(ref, environment.getReferenceByName("x"));
		assertEquals("x", ref.getName());
	}

	@Test
	void addReferenceShouldOverwriteExistingReferenceWithSameName() {
		Environment environment = new Environment();
		Reference first = new ValueReference(1);
		Reference second = new ValueReference(2);

		environment.addReference("x", first);
		environment.addReference("x", second);

		assertSame(second, environment.getReferenceByName("x"));
		assertEquals("x", second.getName());
	}

	@Test
	void getReferenceByNameShouldThrowForUnknownReference() {
		Environment environment = new Environment();

		Error error = assertThrows(Error.class, () -> environment.getReferenceByName("unknown"));

		assertTrue(error.getMessage().contains("Référence inconnue"));
	}

	@Test
	void removeReferenceShouldRemoveOnlyThatReference() {
		Environment environment = new Environment();
		environment.addReference("a", new ValueReference(1));
		environment.addReference("b", new ValueReference(2));

		environment.removeReference("a");

		assertFalse(environment.hasReference("a"));
		assertTrue(environment.hasReference("b"));
	}

	@Test
	void removeReferenceShouldDoNothingIfReferenceDoesNotExist() {
		Environment environment = new Environment();
		environment.addReference("a", new ValueReference(1));

		assertDoesNotThrow(() -> environment.removeReference("missing"));
		assertTrue(environment.hasReference("a"));
	}

	@Test
	void removeReferenceTreeShouldRemoveRootAndAllDescendants() {
		Environment environment = new Environment();
		environment.addReference("space", new ValueReference("space"));
		environment.addReference("space.robi", new ValueReference("robi"));
		environment.addReference("space.robi.eye", new ValueReference("eye"));
		environment.addReference("space.robi.eye.pupil", new ValueReference("pupil"));
		environment.addReference("space.other", new ValueReference("other"));

		environment.removeReferenceTree("space.robi");

		assertTrue(environment.hasReference("space"));
		assertFalse(environment.hasReference("space.robi"));
		assertFalse(environment.hasReference("space.robi.eye"));
		assertFalse(environment.hasReference("space.robi.eye.pupil"));
		assertTrue(environment.hasReference("space.other"));
	}

	@Test
	void removeReferenceTreeShouldNotRemoveSimilarPrefixThatIsNotAChild() {
		Environment environment = new Environment();
		environment.addReference("space.robi", new ValueReference("robi"));
		environment.addReference("space.robix", new ValueReference("robix"));
		environment.addReference("space.robi.child", new ValueReference("child"));

		environment.removeReferenceTree("space.robi");

		assertFalse(environment.hasReference("space.robi"));
		assertFalse(environment.hasReference("space.robi.child"));
		assertTrue(environment.hasReference("space.robix"));
	}

	@Test
	void removeReferenceTreeShouldWorkOnSingleLeaf() {
		Environment environment = new Environment();
		environment.addReference("x", new ValueReference(1));

		environment.removeReferenceTree("x");

		assertFalse(environment.hasReference("x"));
	}

	@Test
	void removeReferenceTreeShouldDoNothingForUnknownRoot() {
		Environment environment = new Environment();
		environment.addReference("x", new ValueReference(1));

		assertDoesNotThrow(() -> environment.removeReferenceTree("unknown"));
		assertTrue(environment.hasReference("x"));
	}
}