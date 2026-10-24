package com.zaid.Delta_Forge.features.pointer;


import org.springframework.stereotype.Service;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

@Service
public class PointerService {
    public JsonNode getAt(JsonNode root, JsonPointer pointer){
        return root.at(pointer);
    }
    public boolean existsAt(JsonNode root, JsonPointer pointer) {
        return !root.at(pointer).isMissingNode();
    }

    // explain -:> you check the node -> you are directly on that node;, if the node to be removed is an array element you do getMatchingIndex() to get that index and then remove it ,else you can remove it by getting the property;

    public JsonNode removeAt(JsonNode root, JsonPointer pointer){
        if(Pointers.isRoot(pointer)){
            throw new IllegalArgumentException("Cannot remove root from the document");
        }
        JsonPointer parentPtr = pointer.head();
        JsonNode parent = root.at(parentPtr);
        if (parent.isMissingNode()) {
            return root;  // nothing to remove; idempotent
        }
        if (parent.isObject()) {
            ((ObjectNode) parent).remove(pointer.getMatchingProperty());
        } else if (parent.isArray()) {
            ((ArrayNode) parent).remove(pointer.getMatchingIndex());
        } else {
            throw new IllegalStateException(
                    "Parent at " + parentPtr + " is neither object nor array");
        }
        return root;
    }
    public JsonNode setAt(JsonNode root, JsonPointer pointer, JsonNode value) {
        if (Pointers.isRoot(pointer)) {
            return value;  // root replacement — returns a new root
        }
        JsonPointer parentPointer = pointer.head();
        JsonNode parent = root.at(parentPointer);

        // Create missing object parents recursively
        if (parent.isMissingNode()) {
            ObjectNode newParent = JsonNodeFactory.instance.objectNode();
            setAt(root, parentPointer, newParent);
            parent = newParent;
        }

        if (parent.isObject()) {
            ((ObjectNode) parent).set(pointer.getMatchingProperty(), value);
        } else if (parent.isArray()) {
            int index = pointer.getMatchingIndex();
            ArrayNode arr = (ArrayNode) parent;
            if (index < 0 || index >= arr.size()) {
                throw new IllegalArgumentException(
                        "Array index " + index + " out of bounds (size " + arr.size() + ")");
            }
            arr.set(index, value);
        } else {
            throw new IllegalStateException(
                    "Cannot set at " + pointer + ": parent is " + parent.getNodeType());
        }
        return root;
    }
}
