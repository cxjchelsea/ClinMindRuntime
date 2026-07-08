import { Navigate, Route, Routes } from 'react-router-dom';
import { AuditBrowserPage } from './pages/AuditBrowserPage';
import { AppShell } from './layout/AppShell';
import { AuditCenterPage } from './pages/AuditCenterPage';
import { CandidatePage } from './pages/CandidatePage';
import { CandidateInboxPage } from './pages/CandidateInboxPage';
import { ConsoleOverviewPage } from './pages/ConsoleOverviewPage';
import { EvaluationPage } from './pages/EvaluationPage';
import { GovernanceDomainsPage } from './pages/GovernanceDomainsPage';
import { ReviewQueuePage } from './pages/ReviewQueuePage';
import { RuntimePage } from './pages/RuntimePage';
import { RuntimeTimelinePage } from './pages/RuntimeTimelinePage';

export default function App() {
  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route index element={<Navigate to="/overview" replace />} />
        <Route path="overview" element={<ConsoleOverviewPage />} />
        <Route path="runtime-timeline" element={<RuntimeTimelinePage />} />
        <Route path="governance-domains" element={<GovernanceDomainsPage />} />
        <Route path="candidate-inbox" element={<CandidateInboxPage />} />
        <Route path="audit-browser" element={<AuditBrowserPage />} />
        <Route path="runtime" element={<RuntimePage />} />
        <Route path="evaluation" element={<EvaluationPage />} />
        <Route path="candidates" element={<CandidatePage />} />
        <Route path="review-queue" element={<ReviewQueuePage />} />
        <Route path="audit-center" element={<AuditCenterPage />} />
      </Route>
    </Routes>
  );
}
