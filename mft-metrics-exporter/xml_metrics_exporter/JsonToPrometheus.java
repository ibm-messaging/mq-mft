/*
* (c) Copyright IBM Corporation 2025
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package xml_metrics_exporter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.PushGateway;

import java.util.*;

// Push the data into Prometheus
public class JsonToPrometheus {
    private static final Map<String, Gauge> gaugeMap = new HashMap<>();

    public static void exportJson(String jsonContent) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature());

        // // Parse and clean input JSON string into a valid JSON array format
        jsonContent = jsonContent.replaceAll("]\\s*\\[", ",");
        if (!jsonContent.trim().startsWith("[")) jsonContent = "[" + jsonContent;
        if (!jsonContent.trim().endsWith("]")) jsonContent = jsonContent + "]";
        jsonContent = jsonContent.replace("\\", "\\\\");
        jsonContent = jsonContent.replaceAll(",\\s*}", "}")
                                 .replaceAll(",\\s*]", "]");

        // Read the cleaned JSON content into a JsonNode tree
        JsonNode root = mapper.readTree(jsonContent);
        int counter = 0;
        for (JsonNode entry : root) {
            if (!entry.isObject()) continue;
            Iterator<Map.Entry<String, JsonNode>> fields = entry.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String listType = field.getKey();
                JsonNode inner = field.getValue();
                if (inner.isObject()) {
                    CollectorRegistry registry = new CollectorRegistry();
                    pushAsMetric(listType, inner, "entry_" + counter++, registry);
                    PushGateway pg = new PushGateway("localhost:9091");
                    Map<String, String> groupingKey = Map.of("instance", UUID.randomUUID().toString());
                    pg.pushAdd(registry, listType, groupingKey);
                    System.out.println(" Pushed job: " + listType);
                }
            }
        }
    }

    /**
    * Converts a single JSON object into a Prometheus Gauge metric and registers it.
    *
    * This method extracts fields from the given JSON node. String fields are treated
    * as metric labels, and the first numeric field encountered is used as the metric value.
    * The metric is registered with the provided Prometheus CollectorRegistry and labeled
    * using the JSON fields.
    */
    private static void pushAsMetric(String listName, JsonNode node, String instanceId, CollectorRegistry registry) {
        Map<String, String> labelsMap = new LinkedHashMap<>();
        double valueToPush = 1.0;
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = sanitizeLabel(field.getKey());
            JsonNode val = field.getValue();
            if (val.isNumber()) valueToPush = val.asDouble();
            labelsMap.put(key, val.isTextual() ? val.asText() : val.toString());
        }
        try {
            Gauge gauge = Gauge.build()
                    .name(sanitizeLabel(listName) + "_info")
                    .help("Metrics for " + listName)
                    .labelNames(labelsMap.keySet().toArray(new String[0]))
                    .register(registry);
            gauge.labels(labelsMap.values().toArray(new String[0])).set(valueToPush);
        } catch (IllegalArgumentException e) {
            System.err.println("✘ Failed to push metric for " + listName + ": " + e.getMessage());
        }
    }

    private static String sanitizeLabel(String label) {
        return label.replaceAll("[^a-zA-Z0-9_]", "_").toLowerCase();
    }
}
