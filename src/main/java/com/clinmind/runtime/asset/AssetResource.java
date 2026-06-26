package com.clinmind.runtime.asset;

import java.util.Map;

public record AssetResource(
        String packageId,
        String relativePath,
        Map<String, Object> content
) {
    public AssetResource {
        content = content == null ? Map.of() : Map.copyOf(content);
    }
}
