package com.ctrip.framework.cs.netty.http;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.ctrip.framework.cs.netty.http.CookieUtil.*;
import static com.ctrip.framework.cs.util.Preconditions.checkNotNull;
/**
 * Created by jiang.j on 2017/2/14.
 */
public final class ServerCookieEncoder extends CookieEncoder {

    /**
     * Strict encoder that validates that name and value chars are in the valid scope
     * defined in RFC6265, and (for methods that accept multiple cookies) that only
     * one cookie is encoded with any given name. (If multiple cookies have the same
     * name, the last one is the one that is encoded.)
     */
    public static final ServerCookieEncoder STRICT = new ServerCookieEncoder(true);

    /**
     * Lax instance that doesn't validate name and value, and that allows multiple
     * cookies with the same name.
     */
    public static final ServerCookieEncoder LAX = new ServerCookieEncoder(false);

    private ServerCookieEncoder(boolean strict) {
        super(strict);
    }

    /**
     * Encodes the specified cookie name-value pair into a Set-Cookie header value.
     *
     * @param name the cookie name
     * @param value the cookie value
     * @return a single Set-Cookie header value
     */
    public String encode(String name, String value) {
        return encode(new DefaultCookie(name, value));
    }

    /**
     * Encodes the specified cookie into a Set-Cookie header value.
     *
     * @param cookie the cookie
     * @return a single Set-Cookie header value
     */
    public String encode(Cookie cookie) {
        final String name = checkNotNull(cookie, "cookie").name();
        final String value = cookie.value() != null ? cookie.value() : "";

        validateCookie(name, value);

        StringBuilder buf = new StringBuilder();

        if (cookie.wrap()) {
            addQuoted(buf, name, value);
        } else {
            add(buf, name, value);
        }

        if (cookie.maxAge() != Long.MIN_VALUE) {
            add(buf, CookieHeaderNames.MAX_AGE, cookie.maxAge());
            Date expires = new Date(cookie.maxAge() * 1000 + System.currentTimeMillis());
            buf.append(CookieHeaderNames.EXPIRES);
            buf.append((char) HttpConstants.EQUALS);
            SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
            dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            buf.append(dateFormatGmt.format(expires));
            buf.append((char) HttpConstants.SEMICOLON);
            buf.append((char) HttpConstants.SP);
        }

        if (cookie.path() != null) {
            add(buf, CookieHeaderNames.PATH, cookie.path());
        }

        if (cookie.domain() != null) {
            add(buf, CookieHeaderNames.DOMAIN, cookie.domain());
        }
        if (cookie.isSecure()) {
            add(buf, CookieHeaderNames.SECURE);
        }
        if (cookie.isHttpOnly()) {
            add(buf, CookieHeaderNames.HTTPONLY);
        }

        return stripTrailingSeparator(buf);
    }

    /** Deduplicate a list of encoded cookies by keeping only the last instance with a given name.
     *
     * @param encoded The list of encoded cookies.
     * @param nameToLastIndex A map from cookie name to index of last cookie instance.
     * @return The encoded list with all but the last instance of a named cookie.
     */
    private List<String> dedup(List<String> encoded, Map<String, Integer> nameToLastIndex) {
        boolean[] isLastInstance = new boolean[encoded.size()];
        for (int idx : nameToLastIndex.values()) {
            isLastInstance[idx] = true;
        }
        List<String> dedupd = new ArrayList<String>(nameToLastIndex.size());
        for (int i = 0, n = encoded.size(); i < n; i++) {
            if (isLastInstance[i]) {
                dedupd.add(encoded.get(i));
            }
        }
        return dedupd;
    }

    /**
     * Batch encodes cookies into Set-Cookie header values.
     *
     * @param cookies a bunch of cookies
     * @return the corresponding bunch of Set-Cookie headers
     */
    public List<String> encode(Cookie... cookies) {
        if (checkNotNull(cookies, "cookies").length == 0) {
            return Collections.emptyList();
        }

        List<String> encoded = new ArrayList<String>(cookies.length);
        Map<String, Integer> nameToIndex = strict && cookies.length > 1 ? new HashMap<String, Integer>() : null;
        boolean hasDupdName = false;
        for (int i = 0; i < cookies.length; i++) {
            Cookie c = cookies[i];
            encoded.add(encode(c));
            if (nameToIndex != null) {
                hasDupdName |= nameToIndex.put(c.name(), i) != null;
            }
        }
        return hasDupdName ? dedup(encoded, nameToIndex) : encoded;
    }

    /**
     * Batch encodes cookies into Set-Cookie header values.
     *
     * @param cookies a bunch of cookies
     * @return the corresponding bunch of Set-Cookie headers
     */
    public List<String> encode(Collection<? extends Cookie> cookies) {
        if (checkNotNull(cookies, "cookies").isEmpty()) {
            return Collections.emptyList();
        }

        List<String> encoded = new ArrayList<String>(cookies.size());
        Map<String, Integer> nameToIndex = strict && cookies.size() > 1 ? new HashMap<String, Integer>() : null;
        int i = 0;
        boolean hasDupdName = false;
        for (Cookie c : cookies) {
            encoded.add(encode(c));
            if (nameToIndex != null) {
                hasDupdName |= nameToIndex.put(c.name(), i++) != null;
            }
        }
        return hasDupdName ? dedup(encoded, nameToIndex) : encoded;
    }

    /**
     * Batch encodes cookies into Set-Cookie header values.
     *
     * @param cookies a bunch of cookies
     * @return the corresponding bunch of Set-Cookie headers
     */
    public List<String> encode(Iterable<? extends Cookie> cookies) {
        Iterator<? extends Cookie> cookiesIt = checkNotNull(cookies, "cookies").iterator();
        if (!cookiesIt.hasNext()) {
            return Collections.emptyList();
        }

        List<String> encoded = new ArrayList<String>();
        Cookie firstCookie = cookiesIt.next();
        Map<String, Integer> nameToIndex = strict && cookiesIt.hasNext() ? new HashMap<String, Integer>() : null;
        int i = 0;
        encoded.add(encode(firstCookie));
        boolean hasDupdName = nameToIndex != null && nameToIndex.put(firstCookie.name(), i++) != null;
        while (cookiesIt.hasNext()) {
            Cookie c = cookiesIt.next();
            encoded.add(encode(c));
            if (nameToIndex != null) {
                hasDupdName |= nameToIndex.put(c.name(), i++) != null;
            }
        }
        return hasDupdName ? dedup(encoded, nameToIndex) : encoded;
    }
}
