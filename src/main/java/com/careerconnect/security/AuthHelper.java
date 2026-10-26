package com.careerconnect.security;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Very small session-based auth: the role and id of the logged-in person live in the HTTP session. */
public final class AuthHelper {

    public static final String SEEKER = "JOB_SEEKER";
    public static final String RECRUITER = "RECRUITER";
    public static final String ADMIN = "ADMIN";

    private AuthHelper() {}

    public static void login(HttpSession session, String role, Long id, String name) {
        session.setAttribute("role", role);
        session.setAttribute("id", id);
        session.setAttribute("name", name);
    }

    /** Returns the logged-in id, or throws 401/403 if nobody (or the wrong role) is logged in. */
    public static Long require(HttpSession session, String role) {
        Object r = session.getAttribute("role");
        Object id = session.getAttribute("id");
        if (r == null || id == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in first");
        }
        if (!r.toString().equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this page");
        }
        return (Long) id;
    }

    public static void need(String value, String label) {
        if (value == null || value.isBlank()) throw bad(label + " is required");
    }

    public static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
    public static ResponseStatusException notFound(String m) { return new ResponseStatusException(HttpStatus.NOT_FOUND, m); }
    public static ResponseStatusException forbidden(String m) { return new ResponseStatusException(HttpStatus.FORBIDDEN, m); }
    public static ResponseStatusException conflict(String m) { return new ResponseStatusException(HttpStatus.CONFLICT, m); }
}
