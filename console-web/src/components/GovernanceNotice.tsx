import './consoleComponents.css';

export function GovernanceNotice() {
  return (
    <aside className="governance-notice" role="note">
      Review 只更新 candidate <strong>review_status</strong>，不会自动修改 AssetPackage、
      CapabilityProfile、Runtime 或 TrainingDataset，也不会自动上线经验或进入训练集。
    </aside>
  );
}
