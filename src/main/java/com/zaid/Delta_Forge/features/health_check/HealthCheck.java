package com.zaid.Delta_Forge.features.health_check;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Iterator;
@RestController
@RequestMapping("health-check")
public class HealthCheck {

    @Autowired
    private ObjectMapper objectMapper;
    @PostMapping
    public void getDoc(@RequestBody JsonNode node){
        walk(node,"");
//        System.out.println(doc.at("/service/node"));
    }
    private void walk(JsonNode node, String path) {
        // Process the current nodeS
        System.out.println(path + " -> " + node.getNodeType() + node.at(path));

        if (node.isObject()) {
            // Traverse every key-value pair in the objectS
            node.properties().forEach(entry -> {
                String childPath =
                        path + "/" + escapePointerToken(entry.getKey());
                System.out.println(entry.getValue());
                walk(entry.getValue(), childPath);
            });

        } else if (node.isArray()) {
            // Traverse every element in the array
            for (int i = 0; i < node.size(); i++) {
                String childPath = path + "/" + i;
                walk(node.get(i), childPath);
            }
        }
    }

    private String escapePointerToken(String token) {
        return token.replace("~", "~0")
                .replace("/", "~1");
    }

}
