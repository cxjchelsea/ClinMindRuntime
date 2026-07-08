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
import { ForbiddenPage } from './pages/ForbiddenPage';
import { CaseWorkspacePage } from './portals/clinician/pages/CaseWorkspacePage';
import { ClinicianCaseInboxPage } from './portals/clinician/pages/ClinicianCaseInboxPage';
import { ClinicianDashboardPage } from './portals/clinician/pages/ClinicianDashboardPage';
import { ClinicianReportPage } from './portals/clinician/pages/ClinicianReportPage';
import { GuidedInquiryPage } from './portals/patient/pages/GuidedInquiryPage';
import { PatientHomePage } from './portals/patient/pages/PatientHomePage';
import { PatientSafeSummaryPage } from './portals/patient/pages/PatientSafeSummaryPage';
import { SymptomIntakePage } from './portals/patient/pages/SymptomIntakePage';
import { useDemoRole } from './rbac/DemoRoleProvider';
import { RoleGuard } from './rbac/RoleGuard';
import { getDefaultRouteForRole } from './rbac/rbac';

function DefaultRoute() {
  const { role } = useDemoRole();
  return <Navigate to={getDefaultRouteForRole(role)} replace />;
}

export default function App() {
  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route index element={<DefaultRoute />} />
        <Route path="forbidden" element={<ForbiddenPage />} />

        <Route
          path="patient"
          element={
            <RoleGuard portal="patient">
              <PatientHomePage />
            </RoleGuard>
          }
        />
        <Route
          path="patient/intake"
          element={
            <RoleGuard portal="patient">
              <SymptomIntakePage />
            </RoleGuard>
          }
        />
        <Route
          path="patient/inquiry"
          element={
            <RoleGuard portal="patient">
              <GuidedInquiryPage />
            </RoleGuard>
          }
        />
        <Route
          path="patient/sessions/:sessionId/summary"
          element={
            <RoleGuard portal="patient">
              <PatientSafeSummaryPage />
            </RoleGuard>
          }
        />

        <Route
          path="clinician"
          element={
            <RoleGuard portal="clinician">
              <ClinicianDashboardPage />
            </RoleGuard>
          }
        />
        <Route
          path="clinician/cases"
          element={
            <RoleGuard portal="clinician">
              <ClinicianCaseInboxPage />
            </RoleGuard>
          }
        />
        <Route
          path="clinician/cases/:caseId"
          element={
            <RoleGuard portal="clinician">
              <CaseWorkspacePage />
            </RoleGuard>
          }
        />
        <Route
          path="clinician/cases/:caseId/report"
          element={
            <RoleGuard portal="clinician">
              <ClinicianReportPage />
            </RoleGuard>
          }
        />

        <Route
          path="governance/overview"
          element={
            <RoleGuard portal="governance" permission="governance:read_overview">
              <ConsoleOverviewPage />
            </RoleGuard>
          }
        />
        <Route
          path="governance/runtime-timeline"
          element={
            <RoleGuard portal="governance" permission="governance:read_runtime_timeline">
              <RuntimeTimelinePage />
            </RoleGuard>
          }
        />
        <Route
          path="governance/runtimes/:runtimeId"
          element={
            <RoleGuard portal="governance" permission="governance:read_runtime_timeline">
              <RuntimeTimelinePage />
            </RoleGuard>
          }
        />
        <Route
          path="governance/domains"
          element={
            <RoleGuard portal="governance" permission="governance:read_overview">
              <GovernanceDomainsPage />
            </RoleGuard>
          }
        />
        <Route
          path="governance/candidate-inbox"
          element={
            <RoleGuard portal="governance" permission="governance:read_candidate_inbox">
              <CandidateInboxPage />
            </RoleGuard>
          }
        />
        <Route
          path="governance/audits"
          element={
            <RoleGuard portal="governance" permission="governance:read_audit">
              <AuditBrowserPage />
            </RoleGuard>
          }
        />
        <Route
          path="governance/runtime"
          element={
            <RoleGuard portal="governance" permission="governance:read_runtime_timeline">
              <RuntimePage />
            </RoleGuard>
          }
        />
        <Route
          path="governance/evaluations"
          element={
            <RoleGuard portal="governance" permission="governance:read_overview">
              <EvaluationPage />
            </RoleGuard>
          }
        />
        <Route
          path="governance/candidates"
          element={
            <RoleGuard portal="governance" permission="governance:read_candidate_inbox">
              <CandidatePage />
            </RoleGuard>
          }
        />
        <Route
          path="governance/review-queue"
          element={
            <RoleGuard portal="governance" permission="governance:read_candidate_inbox">
              <ReviewQueuePage />
            </RoleGuard>
          }
        />
        <Route
          path="governance/audit-center"
          element={
            <RoleGuard portal="governance" permission="governance:read_audit">
              <AuditCenterPage />
            </RoleGuard>
          }
        />

        <Route path="overview" element={<Navigate to="/governance/overview" replace />} />
        <Route path="runtime-timeline" element={<Navigate to="/governance/runtime-timeline" replace />} />
        <Route path="governance-domains" element={<Navigate to="/governance/domains" replace />} />
        <Route path="candidate-inbox" element={<Navigate to="/governance/candidate-inbox" replace />} />
        <Route path="audit-browser" element={<Navigate to="/governance/audits" replace />} />
        <Route path="runtime" element={<Navigate to="/governance/runtime" replace />} />
        <Route path="evaluation" element={<Navigate to="/governance/evaluations" replace />} />
        <Route path="candidates" element={<Navigate to="/governance/candidates" replace />} />
        <Route path="review-queue" element={<Navigate to="/governance/review-queue" replace />} />
        <Route path="audit-center" element={<Navigate to="/governance/audit-center" replace />} />
      </Route>
    </Routes>
  );
}
