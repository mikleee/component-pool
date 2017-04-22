package com.virtual1.componentpool;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * Created by misha on 18.04.17.
 */
class PoolConfig {
    private final static Logger LOGGER = Logger.getLogger(PoolConfig.class);

    private final static String DEFAULT_CONFIG_DESTINATION = "component-pool-default.properties";
    private final static String EXT_CONFIG_DESTINATION = "component-pool.properties";

    private static long timeToLive = 30 * 60 * 1000;
    private static long cleanSchedule = timeToLive / 2;

    static void init() {
        try {
            Properties properties = readConfig();
            applyProperties(properties);
        } catch (Exception e) {
            LOGGER.error("Config init failed", e);
        }
    }

    static long getTimeToLive() {
        return timeToLive;
    }

    static long getCleanSchedule() {
        return cleanSchedule;
    }

    private static Properties readConfig() throws IOException {
        InputStream ext = getPropertiesSource(EXT_CONFIG_DESTINATION);
        InputStream def = getPropertiesSource(DEFAULT_CONFIG_DESTINATION);

        Properties result = new Properties();
        result.load(def);

        if (ext != null) {
            LOGGER.info("Reading properties from " + EXT_CONFIG_DESTINATION);
            Properties extProperties = new Properties();
            extProperties.load(ext);
            result.putAll(extProperties);
        } else {
            LOGGER.info("External config " + EXT_CONFIG_DESTINATION + " hasn't been found");
        }

        return result;
    }

    private static InputStream getPropertiesSource(String path) {
        return PoolConfig.class.getClassLoader().getResourceAsStream(path);
    }

    private static void applyProperties(Properties properties) {
        Long timeToLiveInSeconds = parseLong(properties, "time-to-live");
        if (timeToLiveInSeconds != null) {
            timeToLive = secondsToMillis(timeToLiveInSeconds);
        }

        Long cleanScheduleInSeconds = parseLong(properties, "clean-schedule");
        if (cleanScheduleInSeconds != null) {
            cleanSchedule = secondsToMillis(cleanScheduleInSeconds);
        }

    }

    private static Long parseLong(Properties properties, String key) {
        String p = (String) properties.get(key);
        if (p == null) {
            return null;
        }

        try {
            return Long.valueOf(p);
        } catch (Exception e) {
            LOGGER.warn("Config key " + key + " has incorrect format");
            return null;
        }
    }

    private static long secondsToMillis(Long minutes) {
        return minutes * 1000;
    }

}
