package com.kramar.rest_endpoint_power_test;

import org.apache.commons.io.IOUtils;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RestEndPointPowerTest {

    private static final Logger LOGGER = Logger.getLogger(RestEndPointPowerTest.class.getSimpleName());

    private static final String URL = "url";
    private static final String THREAD_POOL_SIZE = "thread_pool_size";
    private static final String CALL_INTERVAL = "call_interval";
    private static final String COUNT_PER_CALL_INTERVAL = "count_per_call_interval";
    private static final String REQUEST_FILE = "request.json";
    private static final String PROPERTIES_FILE = "config.properties";

    private Properties properties;
    private HttpEntity<String> httpEntity;

    public static void main(String[] args) {
        final RestEndPointPowerTest restEndPointPowerTest = new RestEndPointPowerTest();
        restEndPointPowerTest.runTest();
    }

    private void runTest() {
        properties = new Properties();
        String json = "";
        try {
            final ClassLoader classLoader = getClass().getClassLoader();
            properties.load(classLoader.getResourceAsStream(PROPERTIES_FILE));
            json = IOUtils.toString(classLoader.getResourceAsStream(REQUEST_FILE), "UTF-8");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpEntity = new HttpEntity<>(json, httpHeaders);
        new Timer().schedule(new Task(), 1, Long.parseLong(properties.getProperty(CALL_INTERVAL)) * 1000);
    }

    private class Task extends TimerTask {
        @Override
        public void run() {
            final ExecutorService executorService =
                    Executors.newFixedThreadPool(Integer.parseInt(properties.getProperty(THREAD_POOL_SIZE)));
            for (int i = 0; i < Integer.parseInt(properties.getProperty(COUNT_PER_CALL_INTERVAL)); i++) {
                executorService.submit(() -> {
                    try {
                        final RestTemplate restTemplate = new RestTemplate();
                        final ResponseEntity<String> result =
                                restTemplate.exchange(properties.getProperty(URL), HttpMethod.POST, httpEntity, String.class);
                        LOGGER.log(Level.INFO, Thread.currentThread().getName());
                        LOGGER.log(Level.INFO, result.toString());
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                    return null;
                });
            }
        }
    }
}
