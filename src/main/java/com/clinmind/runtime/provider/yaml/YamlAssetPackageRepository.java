package com.clinmind.runtime.provider.yaml;

import com.clinmind.runtime.asset.AssetLoadErrorCode;
import com.clinmind.runtime.asset.AssetLoadException;
import com.clinmind.runtime.asset.AssetPackageManifest;
import com.clinmind.runtime.asset.AssetPackageRepository;
import com.clinmind.runtime.asset.AssetResource;
import com.clinmind.runtime.asset.AssetStatus;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

@Component
public class YamlAssetPackageRepository implements AssetPackageRepository {

    public static final String DEFAULT_PACKAGE_ID = "phase2-default";
    private static final String MANIFEST_FILE = "manifest.yml";
    private static final List<String> SECURITY_CRITICAL_RESOURCES = List.of(
            "red-flag-rules.yml",
            "capability-profiles.yml");

    private final String packagesPrefix;
    private final Yaml yaml = new Yaml();
    private final ResourcePatternResolver resourcePatternResolver;

    public YamlAssetPackageRepository() {
        this("assets/packages/", new PathMatchingResourcePatternResolver());
    }

    YamlAssetPackageRepository(String packagesPrefix, ResourcePatternResolver resourcePatternResolver) {
        this.packagesPrefix = packagesPrefix.endsWith("/") ? packagesPrefix : packagesPrefix + "/";
        this.resourcePatternResolver = resourcePatternResolver;
    }

    @Override
    public String getDefaultPackageId() {
        return listPackages().stream()
                .filter(AssetPackageManifest::defaultPackage)
                .map(AssetPackageManifest::packageId)
                .findFirst()
                .orElse(DEFAULT_PACKAGE_ID);
    }

    @Override
    public AssetPackageManifest loadRuntimeManifest(String packageId) {
        AssetPackageManifest manifest = loadManifest(packageId);
        assertRuntimeUsable(manifest);
        validateSecurityCriticalAssets(packageId);
        return manifest;
    }

    @Override
    public AssetPackageManifest loadManifest(String packageId) {
        Map<String, Object> root = loadYamlMap(packageId, MANIFEST_FILE, true);
        return parseManifest(root);
    }

    @Override
    public List<AssetPackageManifest> listPackages() {
        List<AssetPackageManifest> manifests = new ArrayList<>();
        try {
            Resource[] resources = resourcePatternResolver.getResources(
                    "classpath*:" + packagesPrefix + "*/" + MANIFEST_FILE);
            for (Resource resource : resources) {
                String packageId = extractPackageId(resource);
                if (packageId != null) {
                    manifests.add(loadManifest(packageId));
                }
            }
        } catch (IOException error) {
            throw new AssetLoadException(
                    AssetLoadErrorCode.ASSET_LOAD_FAILED,
                    "Failed to list asset packages",
                    true,
                    null,
                    null,
                    error);
        }
        return manifests;
    }

    @Override
    public AssetResource loadResource(String packageId, String relativePath) {
        Map<String, Object> content = loadYamlMap(packageId, relativePath, false);
        return new AssetResource(packageId, normalizeRelativePath(relativePath), content);
    }

    public void assertRuntimeUsable(AssetPackageManifest manifest) {
        if (manifest == null || !manifest.isRuntimeUsable()) {
            throw new AssetLoadException(
                    AssetLoadErrorCode.ASSET_STATUS_DISABLED,
                    "Asset package is not runtime usable: "
                            + (manifest == null ? "null" : manifest.packageId()),
                    true,
                    manifest == null ? null : manifest.packageId(),
                    null);
        }
    }

    public void validateSecurityCriticalAssets(String packageId) {
        for (String relativePath : SECURITY_CRITICAL_RESOURCES) {
            if (!resourceExists(packageId, relativePath)) {
                throw new AssetLoadException(
                        AssetLoadErrorCode.ASSET_NOT_FOUND,
                        "Security-critical asset missing: " + relativePath,
                        true,
                        packageId,
                        relativePath);
            }
        }
    }

    private boolean resourceExists(String packageId, String relativePath) {
        try {
            Resource resource = resourcePatternResolver.getResource(
                    buildClasspathLocation(packageId, relativePath));
            return resource.exists();
        } catch (Exception error) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadYamlMap(String packageId, String relativePath, boolean riskCritical) {
        String normalizedPath = normalizeRelativePath(relativePath);
        Resource resource = resourcePatternResolver.getResource(
                buildClasspathLocation(packageId, normalizedPath));
        if (!resource.exists()) {
            throw new AssetLoadException(
                    AssetLoadErrorCode.ASSET_NOT_FOUND,
                    "Asset resource not found: " + packagesPrefix + packageId + "/" + normalizedPath,
                    riskCritical,
                    packageId,
                    normalizedPath);
        }
        try (InputStream inputStream = resource.getInputStream()) {
            Object loaded = yaml.load(inputStream);
            if (loaded instanceof Map<?, ?> map) {
                return new LinkedHashMap<>((Map<String, Object>) map);
            }
            throw new AssetLoadException(
                    AssetLoadErrorCode.ASSET_FORMAT_INVALID,
                    "Invalid YAML format: " + normalizedPath,
                    riskCritical,
                    packageId,
                    normalizedPath);
        } catch (AssetLoadException error) {
            throw error;
        } catch (Exception error) {
            throw new AssetLoadException(
                    AssetLoadErrorCode.ASSET_LOAD_FAILED,
                    "Failed to load asset resource: " + normalizedPath,
                    riskCritical,
                    packageId,
                    normalizedPath,
                    error);
        }
    }

    private AssetPackageManifest parseManifest(Map<String, Object> root) {
        return new AssetPackageManifest(
                stringValue(root.get("package_id")),
                stringValue(root.get("version")),
                AssetStatus.fromValue(stringValue(root.get("status"))),
                stringValue(root.get("display_name")),
                stringValue(root.get("description")),
                parseInstant(root.get("created_at")),
                parseInstant(root.get("updated_at")),
                stringValue(root.get("source")),
                stringValue(root.get("owner")),
                stringList(root.get("supported_symptom_groups")),
                booleanValue(root.get("default_package")));
    }

    private String buildClasspathLocation(String packageId, String relativePath) {
        return "classpath:" + packagesPrefix + packageId + "/" + relativePath;
    }

    private String extractPackageId(Resource resource) {
        try {
            String uri = resource.getURI().toString();
            String marker = "/" + packagesPrefix.replace("\\", "/");
            int packageStart = uri.lastIndexOf(marker);
            if (packageStart < 0) {
                marker = packagesPrefix.replace("\\", "/");
                packageStart = uri.lastIndexOf(marker);
            }
            if (packageStart < 0) {
                return null;
            }
            int start = packageStart + marker.length();
            int end = uri.indexOf('/', start);
            if (end < 0) {
                end = uri.indexOf('\\', start);
            }
            if (end < 0) {
                end = uri.length();
            }
            return uri.substring(start, end);
        } catch (IOException error) {
            return null;
        }
    }

    private String normalizeRelativePath(String relativePath) {
        return relativePath.replace("\\", "/");
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private boolean booleanValue(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        return value != null && Boolean.parseBoolean(String.valueOf(value));
    }

    private Instant parseInstant(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof Date date) {
            return date.toInstant();
        }
        return Instant.parse(String.valueOf(value));
    }

    @SuppressWarnings("unchecked")
    private List<String> stringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (Object item : list) {
            if (item != null) {
                result.add(String.valueOf(item));
            }
        }
        return List.copyOf(result);
    }
}
