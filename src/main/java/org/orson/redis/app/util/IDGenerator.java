package org.orson.redis.app.util;

import java.util.UUID;

public final class IDGenerator {


    /**
     * @return UUID
     */
    public static String newId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
