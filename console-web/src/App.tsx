import { Navigate, Route, Routes } from 'react-router-dom';
import { AppShell } from './layout/AppShell';
import { AuditCenterPage } from './pages/AuditCenterPage';
import { CandidatePage } from './pages/CandidatePage';
import { EvaluationPage } from './pages/EvaluationPage';
import { ReviewQueuePage } from './pages/ReviewQueuePage';
import { RuntimePage } from './pages/RuntimePage';

export default function App() {
  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route index element={<Navigate to="/runtime" replace />} />
        <Route path="runtime" element={<RuntimePage />} />
        <Route path="evaluation" element={<EvaluationPage />} />
        <Route path="candidates" element={<CandidatePage />} />
        <Route path="review-queue" element={<ReviewQueuePage />} />
        <Route path="audit-center" element={<AuditCenterPage />} />
      </Route>
    </Routes>
  );
}
