package com.clinmind.runtime.asset;

public class AssetLoadException extends RuntimeException {

    private final AssetLoadErrorCode errorCode;
    private final boolean riskCritical;
    private final String packageId;
    private final String assetId;

    public AssetLoadException(
            AssetLoadErrorCode errorCode,
            String message,
            boolean riskCritical) {
        this(errorCode, message, riskCritical, null, null, null);
    }

    public AssetLoadException(
            AssetLoadErrorCode errorCode,
            String message,
            boolean riskCritical,
            String packageId,
            String assetId) {
        this(errorCode, message, riskCritical, packageId, assetId, null);
    }

    public AssetLoadException(
            AssetLoadErrorCode errorCode,
            String message,
            boolean riskCritical,
            String packageId,
            String assetId,
            Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.riskCritical = riskCritical;
        this.packageId = packageId;
        this.assetId = assetId;
    }

    public AssetLoadErrorCode getErrorCode() {
        return errorCode;
    }

    public boolean isRiskCritical() {
        return riskCritical;
    }

    public String getPackageId() {
        return packageId;
    }

    public String getAssetId() {
        return assetId;
    }
}
