package com.datamint.twin;

import com.google.gson.Gson;
import org.junit.Test;
import static org.junit.Assert.*;

public class LoginUnitTest {

    @Test
    public void testNormalizeUrl() {
        assertEquals("http://10.0.2.2:8080/api/", Session.normalizeUrl(null));
        assertEquals("http://10.0.2.2:8080/api/", Session.normalizeUrl(""));
        assertEquals("http://10.0.2.2:8080/api/", Session.normalizeUrl("   "));
        assertEquals("http://10.0.2.2:8080/api/", Session.normalizeUrl("http://10.0.2.2:8080/api/"));
        assertEquals("http://10.0.2.2:8080/api/", Session.normalizeUrl("http://10.0.2.2:8080/api"));
        assertEquals("http://10.0.2.2:8080/api/", Session.normalizeUrl("10.0.2.2:8080"));
        assertEquals("http://192.168.1.15:8080/api/", Session.normalizeUrl("192.168.1.15:8080"));
        assertEquals("https://myfactory.com/api/", Session.normalizeUrl("https://myfactory.com"));
    }

    @Test
    public void testLoginRequestSerialization() {
        Models.LoginRequest req = new Models.LoginRequest("admin", "admin123");
        String json = new Gson().toJson(req);
        assertTrue(json.contains("\"username\":\"admin\""));
        assertTrue(json.contains("\"password\":\"admin123\""));
    }
}

