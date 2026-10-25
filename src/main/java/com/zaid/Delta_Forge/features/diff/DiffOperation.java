package com.zaid.Delta_Forge.features.diff;

import com.zaid.Delta_Forge.features.json.ValueSlot;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;

enum DiffOperationType{ADD,REMOVE,REPLACE}
public record DiffOperation(
    DiffOperationType diffOperationType,
    JsonPointer path,
    ValueSlot oldVal,
    ValueSlot newVal
) {}
