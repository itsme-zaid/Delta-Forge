package com.zaid.Delta_Forge.features.diff;

import com.zaid.Delta_Forge.features.json.JsonDocumentService;
import com.zaid.Delta_Forge.features.json.ValueSlot;
import org.jspecify.annotations.NonNull;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

public final class DiffFormatter {

    private final JsonDocumentService jsonDocumentService;

    public DiffFormatter(JsonDocumentService jsonDocumentService) {
        this.jsonDocumentService = jsonDocumentService;
    }

    public String formatText(List<DiffOperation> ops) {
        if (ops.isEmpty()) {
            return "(no changes)";
        }
        StringBuilder sb = new StringBuilder();
        for (DiffOperation op : ops) {
            switch (op.diffOperationType()) {
                case ADD -> sb.append("+ ").append(displayPath(op.path()))
                        .append(" = ").append(renderValue(op.newVal())).append('\n');
                case REMOVE -> sb.append("- ").append(displayPath(op.path()))
                        .append(" = ").append(renderValue(op.oldVal())).append('\n');
                case REPLACE -> sb.append("~ ").append(displayPath(op.path()))
                        .append(": ").append(renderValue(op.oldVal()))
                        .append(" -> ").append(renderValue(op.newVal())).append('\n');
            }
        }
        return sb.toString().stripTrailing();
    }

    private String displayPath(@NonNull JsonPointer path) {
        return path.matches() ? "(root)" : path.toString();
    }

    private String renderValue(ValueSlot slot) {
        return switch (slot) {
            case ValueSlot.Missing() -> "(missing)";
            case ValueSlot.Present(JsonNode n) -> renderNode(n);
        };
    }

    private String renderNode(JsonNode node) {
        if (node.isString() || node.isString()) {
            return "\"" + node.asString() + "\"";
        }
        return node.toString();   // numbers, booleans, null, objects, arrays
    }

    public String formatJson(List<DiffOperation> ops) {
        ArrayNode arr = JsonNodeFactory.instance.arrayNode();
        for (DiffOperation op : ops) {
            ObjectNode o = arr.addObject();
            o.put("type", op.diffOperationType().name());
            o.put("path", op.path().toString());
            if (op.oldVal() instanceof ValueSlot.Present(JsonNode n)) {
                o.set("oldValue", n);
            }
            if (op.newVal() instanceof ValueSlot.Present(JsonNode n)) {
                o.set("newValue", n);
            }
        }
        return jsonDocumentService.serialize(arr);
    }
    private String highlightStringChange(String oldStr, String newStr) {
        int[][] lcs = lcs(oldStr, newStr);
        StringBuilder sb = new StringBuilder();
        // walk the LCS table, emitting KEEP/REMOVE/ADD segments
        int i = 0, j = 0;
        while (i < oldStr.length() && j < newStr.length()) {
            if (oldStr.charAt(i) == newStr.charAt(j)) {
                sb.append(oldStr.charAt(i));
                i++; j++;
            } else if (lcs[i + 1][j] >= lcs[i][j + 1]) {
                sb.append("[-").append(oldStr.charAt(i)).append("-]");
                i++;
            } else {
                sb.append("[+").append(newStr.charAt(j)).append("+]");
                j++;
            }
        }
        while (i < oldStr.length()) sb.append("[-").append(oldStr.charAt(i++)).append("-]");
        while (j < newStr.length()) sb.append("[+").append(newStr.charAt(j++)).append("+]");
        return sb.toString();
    }
    private int[][] lcs(String oldString, String newString) {
        int m = oldString.length();
        int n = newString.length();

        int[][] dp = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (oldString.charAt(i - 1) == newString.charAt(j - 1)) {
                    dp[i][j] = 1 + dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.max(
                            dp[i - 1][j],
                            dp[i][j - 1]
                    );
                }
            }
        }

        return dp;
    }
}