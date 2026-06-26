package com.clinmind.runtime.provider.yaml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.asset.AssetLoadErrorCode;
import com.clinmind.runtime.asset.AssetLoadException;
import com.clinmind.runtime.asset.AssetPackageManifest;
import com.clinmind.runtime.asset.AssetResource;
import com.clinmind.runtime.asset.AssetStatus;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

class YamlAssetPackageRepositoryTest {

    private final YamlAssetPackageRepository repository = new YamlAssetPackageRepository();

    @Test
    void loadsDefaultPackageManifest() {
        AssetPackageManifest manifest = repository.loadManifest(YamlAssetPackageRepository.DEFAULT_PACKAGE_ID);

        assertThat(manifest.packageId()).isEqualTo("phase2-default");
        assertThat(manifest.version()).isEqualTo("0.2.0");
        assertThat(manifest.status()).isEqualTo(AssetStatus.ACTIVE);
        assertThat(manifest.supportedSymptomGroups()).contains("chest_pain", "fever");
        assertThat(manifest.defaultPackage()).isTrue();
    }

    @Test
    void listsPackagesIncludingTestPackages() {
        assertThat(repository.listPackages())
                .extracting(AssetPackageManifest::packageId)
                .contains("phase2-default", "phase2-alt", "broken-package", "disabled-package");
    }

    @Test
    void loadsResourceFromDefaultPackage() {
        AssetResource resource = repository.loadResource(
                YamlAssetPackageRepository.DEFAULT_PACKAGE_ID,
                "symptom-groups/chest-pain.yml");

        assertThat(resource.relativePath()).isEqualTo("symptom-groups/chest-pain.yml");
        assertThat(resource.content()).containsKey("symptom_group");
        assertThat(resource.content().get("symptom_group")).isEqualTo("chest_pain");
    }

    @Test
    void loadsAlternatePackageFromTestResources() {
        AssetPackageManifest manifest = repository.loadManifest("phase2-alt");

        assertThat(manifest.status()).isEqualTo(AssetStatus.ACTIVE);
        assertThat(manifest.version()).isEqualTo("0.2.1-alt");
    }

    @Test
    void loadRuntimeManifestRejectsDisabledPackage() {
        AssetPackageManifest disabled = repository.loadManifest("disabled-package");

        assertThat(disabled.status()).isEqualTo(AssetStatus.DISABLED);

        assertThatThrownBy(() -> repository.loadRuntimeManifest("disabled-package"))
                .isInstanceOf(AssetLoadException.class)
                .satisfies(error -> {
                    AssetLoadException assetError = (AssetLoadException) error;
                    assertThat(assetError.getErrorCode()).isEqualTo(AssetLoadErrorCode.ASSET_STATUS_DISABLED);
                    assertThat(assetError.isRiskCritical()).isTrue();
                });
    }

    @Test
    void brokenPackageMissingSecurityCriticalAssets() {
        assertThatThrownBy(() -> repository.loadRuntimeManifest("broken-package"))
                .isInstanceOf(AssetLoadException.class)
                .satisfies(error -> {
                    AssetLoadException assetError = (AssetLoadException) error;
                    assertThat(assetError.getErrorCode()).isEqualTo(AssetLoadErrorCode.ASSET_NOT_FOUND);
                    assertThat(assetError.getMessage()).contains("red-flag-rules.yml");
                    assertThat(assetError.isRiskCritical()).isTrue();
                });
    }

    @Test
    void missingResourceThrowsAssetLoadException() {
        assertThatThrownBy(() -> repository.loadResource(
                YamlAssetPackageRepository.DEFAULT_PACKAGE_ID,
                "missing-file.yml"))
                .isInstanceOf(AssetLoadException.class)
                .satisfies(error -> {
                    AssetLoadException assetError = (AssetLoadException) error;
                    assertThat(assetError.getErrorCode()).isEqualTo(AssetLoadErrorCode.ASSET_NOT_FOUND);
                });
    }

    @Test
    void resolvesDefaultPackageId() {
        assertThat(repository.getDefaultPackageId()).isEqualTo("phase2-default");
    }

    @Test
    void loadRuntimeManifestValidatesDefaultPackage() {
        AssetPackageManifest manifest = repository.loadRuntimeManifest(
                YamlAssetPackageRepository.DEFAULT_PACKAGE_ID);

        assertThat(manifest.isRuntimeUsable()).isTrue();
    }

    @Test
    void customPrefixSupportsIsolatedRepository() {
        YamlAssetPackageRepository isolated = new YamlAssetPackageRepository(
                "assets/packages/",
                new PathMatchingResourcePatternResolver());

        assertThat(isolated.loadManifest("phase2-default").packageId()).isEqualTo("phase2-default");
    }
}
