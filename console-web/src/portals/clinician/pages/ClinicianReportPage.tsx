import { useParams } from 'react-router-dom';
import { ReportDraftEditor } from '../components/ReportDraftEditor';
import { useClinicianCaseView } from '../hooks/useClinicianCaseView';

export function ClinicianReportPage() {
  const { caseId = 'runtime-demo-001' } = useParams();
  const { data: clinicianCaseView, loading, source, error } = useClinicianCaseView(caseId);

  return (
    <section className="console-page">
      <div className="console-page__header">
        <h1>Clinician Report</h1>
        <p>报告草稿仅供医生本地复核，不代表已提交或已签发。</p>
      </div>
      {source === 'demo-fallback' ? (
        <p className="audit-access-hint">
          Demo fallback：Clinician Report API 暂不可用，当前使用本地演示投影。{error ? `原因：${error}` : ''}
        </p>
      ) : null}
      {loading ? <p className="portal-muted">Loading Clinician View API...</p> : null}
      <ReportDraftEditor draft={clinicianCaseView.report_draft} />
    </section>
  );
}
