package com.clinmind.runtime.asset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class AssetPackageManifestTest {

    @Test
    void exposesPackageVersionAndSupportedGroups() {
        AssetPackageManifest manifest = sampleManifest();

        assertThat(manifest.packageVersion().value()).isEqualTo("0.2.0");
        assertThat(manifest.supportedSymptomGroups()).containsExactly("chest_pain", "fever");
        assertThat(manifest.isRuntimeUsable()).isTrue();
        assertThat(manifest.defaultPackage()).isTrue();
    }

    @Test
    void rejectsBlankPackageId() {
        assertThatThrownBy(() -> new AssetPackageManifest(
                " ",
                "0.2.0",
                AssetStatus.ACTIVE,
                "name",
                "desc",
                Instant.now(),
                Instant.now(),
                "internal",
                "owner",
                List.of(),
                true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    static AssetPackageManifest sampleManifest() {
        return AssetTestFixtures.sampleManifest();
    }
}
