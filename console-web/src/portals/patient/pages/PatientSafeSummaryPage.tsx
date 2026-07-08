import { patientRuntimeView } from '../../../demo/runtimeDemoData';
import { CollectedFactsCard } from '../components/CollectedFactsCard';
import { PatientQuestionList } from '../components/PatientQuestionList';
import { PatientSafetyNotice } from '../components/PatientSafetyNotice';

export function PatientSafeSummaryPage() {
  return (
    <section className="console-page">
      <div className="console-page__header">
        <h1>Patient Safe Summary</h1>
        <p>同一 Runtime 的患者端投影：仅展示安全摘要、已收集事实和下一步问询。</p>
      </div>
      <section className="portal-panel">
        <h2>Safe summary</h2>
        <p>{patientRuntimeView.safe_summary}</p>
        <p className="portal-muted">{patientRuntimeView.disclaimer}</p>
      </section>
      <PatientSafetyNotice notices={patientRuntimeView.safety_notices} />
      <CollectedFactsCard facts={patientRuntimeView.collected_facts} />
      <PatientQuestionList questions={patientRuntimeView.next_questions} />
      <section className="portal-panel">
        <h2>Care navigation</h2>
        <div className="portal-card-grid">
          {patientRuntimeView.care_navigation.map((item) => (
            <article className="portal-card" key={item.label}>
              <h3>{item.label}</h3>
              <p>{item.description}</p>
            </article>
          ))}
        </div>
      </section>
    </section>
  );
}
