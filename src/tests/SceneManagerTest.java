package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import server.SceneManager;
import shared.ClientRequest;
import shared.ServerResponse;
import shared.ElementData;

class SceneManagerTest {

	private SceneManager manager;

	@BeforeEach
	void setup() {
		manager = new SceneManager();
	}

	@Test
	void move_valid_shouldWork() {
		ClientRequest add = new ClientRequest("ADD");
		add.setType("Rect");
		add.setName("r1");
		manager.handle(add);

		ClientRequest move = new ClientRequest("MOVE");
		move.setTarget("space.r1");
		move.setDx(20);
		move.setDy(0);

		ServerResponse res = manager.handle(move);

		assertTrue(res.isSuccess());
		assertTrue(res.getMessage().toLowerCase().contains("moved"));
	}

	@Test
	void move_outOfBounds_shouldBeIgnored() {
		ClientRequest add = new ClientRequest("ADD");
		add.setType("Rect");
		add.setName("r1");
		manager.handle(add);

		ClientRequest move = new ClientRequest("MOVE");
		move.setTarget("space.r1");
		move.setDx(10000);
		move.setDy(10000);

		ServerResponse res = manager.handle(move);

		assertTrue(res.isSuccess());
		assertTrue(res.getMessage().toLowerCase().contains("ignored"));

		ElementData el = res.getScene().getElements().get(0);
		assertEquals(10, el.getX());
		assertEquals(10, el.getY());
	}

	@Test
	void setPosition_outOfBounds_shouldBeIgnored() {
		ClientRequest add = new ClientRequest("ADD");
		add.setType("Rect");
		add.setName("r1");
		manager.handle(add);

		ClientRequest setPos = new ClientRequest("SET_POSITION");
		setPos.setTarget("space.r1");
		setPos.setX(9999);
		setPos.setY(9999);

		ServerResponse res = manager.handle(setPos);

		assertTrue(res.isSuccess());
		assertTrue(res.getMessage().toLowerCase().contains("ignored"));
	}

	@Test
	void resizeSpace_tooSmall_shouldBeIgnored() {
		ClientRequest add = new ClientRequest("ADD");
		add.setType("Rect");
		add.setName("r1");
		manager.handle(add);

		ClientRequest resize = new ClientRequest("SET_SIZE");
		resize.setTarget("space");
		resize.setWidth(5);
		resize.setHeight(5);

		ServerResponse res = manager.handle(resize);

		assertTrue(res.isSuccess());
		assertTrue(res.getMessage().toLowerCase().contains("ignored"));
	}

	@Test
	void resizeElement_breakingChildren_shouldBeIgnored() {
		ClientRequest addParent = new ClientRequest("ADD");
		addParent.setType("Rect");
		addParent.setName("parent");
		manager.handle(addParent);

		ClientRequest addChild = new ClientRequest("ADD");
		addChild.setType("Rect");
		addChild.setName("child");
		addChild.setTarget("space.parent");
		manager.handle(addChild);

		ClientRequest resize = new ClientRequest("SET_SIZE");
		resize.setTarget("space.parent");
		resize.setWidth(1);
		resize.setHeight(1);

		ServerResponse res = manager.handle(resize);

		assertTrue(res.isSuccess());
		assertTrue(res.getMessage().toLowerCase().contains("ignored"));
	}
}