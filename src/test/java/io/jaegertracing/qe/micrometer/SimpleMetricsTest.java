package io.jaegertracing.qe.micrometer;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@Category(io.jaegertracing.qe.micrometer.IntegrationTest.class)
public class SimpleMetricsTest {
    private static final Map<String, String> envs = System.getenv();
    private static final String TARGET_URL = envs.getOrDefault("TARGET_URL", "http://localhost:8080/");

    private static final Logger logger = LoggerFactory.getLogger(SimpleMetricsTest.class);
    private static List<String> expectedMetricNames = new ArrayList<>();

    @BeforeClass
    public static void initial() {
        expectedMetricNames.add("jaeger:baggage_restrictions_updates_total{result=\"err\",}");
        expectedMetricNames.add("jaeger:baggage_restrictions_updates_total{result=\"ok\",}");
        expectedMetricNames.add("jaeger:baggage_truncations_total");
        expectedMetricNames.add("jaeger:baggage_updates_total{result=\"err\",}");
        expectedMetricNames.add("jaeger:baggage_updates_total{result=\"ok\",}");
        expectedMetricNames.add("jaeger:finished_spans_total");
        expectedMetricNames.add("jaeger:reporter_spans_total{result=\"dropped\",}");
        expectedMetricNames.add("jaeger:reporter_spans_total{result=\"err\",}");
        expectedMetricNames.add("jaeger:reporter_spans_total{result=\"ok\",}");
        expectedMetricNames.add("jaeger:sampler_queries_total{result=\"err\",}");
        expectedMetricNames.add("jaeger:sampler_queries_total{result=\"ok\",}");
        expectedMetricNames.add("jaeger:sampler_updates_total{result=\"err\",}");
        expectedMetricNames.add("jaeger:sampler_updates_total{result=\"ok\",}");
        expectedMetricNames.add("jaeger:span_context_decoding_errors_total");
        expectedMetricNames.add("jaeger:started_spans_total{sampled=\"n\",}");
        expectedMetricNames.add("jaeger:started_spans_total{sampled=\"y\",}");
        expectedMetricNames.add("jaeger:traces_total{sampled=\"n\",state=\"joined\",}");
        expectedMetricNames.add("jaeger:traces_total{sampled=\"n\",state=\"started\",}");
        expectedMetricNames.add("jaeger:traces_total{sampled=\"y\",state=\"joined\",}");
        expectedMetricNames.add("jaeger:traces_total{sampled=\"y\",state=\"started\",}");

        Collections.sort(expectedMetricNames);
    }


    @Test
    public void check() throws IOException, MalformedURLException {
        Map<String, Double> metricCounts = getMetrics();
        List<String> metricNames = new ArrayList<>(metricCounts.keySet());
        Collections.sort(metricNames);
        assertEquals(expectedMetricNames, metricNames);
    }


    private Map<String, Double> getMetrics() throws IOException, MalformedURLException {
        String urlString = TARGET_URL + "/actuator/prometheus";
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        logger.info("Got response " + connection.getResponseCode() + " from " + urlString);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        Map<String, Double> metricCounts = new HashMap<>();
        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("jaeger")) {
                String metricName = line.substring(0, line.indexOf(" "));
                Double count = Double.valueOf(line.substring(line.indexOf(" ")));
                metricCounts.put(metricName, count);
            }
        }
        bufferedReader.close();

        return metricCounts;
    }
}