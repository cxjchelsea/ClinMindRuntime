import { clinicianCaseView } from '../../../demo/runtimeDemoData';
import { ReportDraftEditor } from '../components/ReportDraftEditor';

export function ClinicianReportPage() {
  return (
    <section className="console-page">
      <div className="console-page__header">
        <h1>Clinician Report</h1>
        <p>报告草稿仅供医生本地复核，不代表已提交或已签发。</p>
      </div>
      <ReportDraftEditor draft={clinicianCaseView.report_draft} />
    </section>
  );
}
