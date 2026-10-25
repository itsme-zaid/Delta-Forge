package com.zaid.Delta_Forge.features.diff;

import tools.jackson.databind.JsonNode;

import java.util.List;

public interface DiffEngine {
    List<DiffOperation> diff(JsonNode base, JsonNode target);
}
