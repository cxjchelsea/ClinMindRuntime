import { Link } from 'react-router-dom';
import { DEMO_RUNTIME_ID, patientRuntimeView } from '../../../demo/runtimeDemoData';
import { PatientQuestionList } from '../components/PatientQuestionList';

export function GuidedInquiryPage() {
  return (
    <section className="console-page">
      <div className="console-page__header">
        <h1>Guided Inquiry</h1>
        <p>受控问询帮助补齐医生可能需要的信息，答案不会在 P0 提交到真实系统。</p>
      </div>
      <PatientQuestionList questions={patientRuntimeView.next_questions} />
      <div className="portal-action-row">
        <Link className="portal-link-button" to={`/patient/sessions/${DEMO_RUNTIME_ID}/summary`}>
          Review safe summary
        </Link>
      </div>
    </section>
  );
}
