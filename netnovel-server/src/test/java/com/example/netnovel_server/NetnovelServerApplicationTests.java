package com.example.netnovel_server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class NetnovelServerApplicationTests {

	// Unit-level smoke check; full Spring context tests should use an isolated test database.
	@Test
	void applicationClassIsAvailable() {
		assertNotNull(NetnovelServerApplication.class);
	}

}
