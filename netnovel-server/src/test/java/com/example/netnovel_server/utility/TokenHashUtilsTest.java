package com.example.netnovel_server.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TokenHashUtilsTest {

    // Refresh tokens are stored as SHA-256 hashes, never raw token strings.

    @Test
    void sha256ReturnsExpectedHexDigest() {
        assertEquals(
            "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9",
            TokenHashUtils.sha256("hello world")
        );
    }

    @Test
    void sha256IsDeterministicAndInputSensitive() {
        assertEquals(TokenHashUtils.sha256("refresh-token"), TokenHashUtils.sha256("refresh-token"));
        assertNotEquals(TokenHashUtils.sha256("refresh-token"), TokenHashUtils.sha256("other-token"));
    }
}
