import type { PatientQuestion } from '../../../shared/types/patientViews';

export function PatientQuestionList({ questions }: { questions: PatientQuestion[] }) {
  return (
    <section className="portal-panel">
      <h2>Next questions</h2>
      <ol className="portal-ordered-list">
        {questions.map((question) => (
          <li key={question.id}>
            <strong>{question.prompt}</strong>
            <span>{question.reason_for_asking}</span>
          </li>
        ))}
      </ol>
    </section>
  );
}
