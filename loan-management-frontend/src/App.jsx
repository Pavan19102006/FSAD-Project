import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Layout from './components/Layout';
import Login from './pages/Login';
import Register from './pages/Register';
import OtpVerification from './pages/OtpVerification';
import LandingPage from './pages/LandingPage';
import AdminDashboard from './pages/AdminDashboard';
import AdminAnalytics from './pages/AdminAnalytics';
import AdminUserManagement from './pages/AdminUserManagement';
import LenderDashboard from './pages/LenderDashboard';
import LenderLoans from './pages/LenderLoans';
import LenderApplications from './pages/LenderApplications';
import LenderCreateLoan from './pages/LenderCreateLoan';
import LenderWithdraw from './pages/LenderWithdraw';
import LenderPayments from './pages/LenderPayments';
import BorrowerDashboard from './pages/BorrowerDashboard';
import AnalystDashboard from './pages/AnalystDashboard';
import AnalystRisk from './pages/AnalystRisk';
import AnalystPayments from './pages/AnalystPayments';
import AnalystLoans from './pages/AnalystLoans';
import UserProfile from './pages/UserProfile';

function App() {
    return (
        <AuthProvider>
            <BrowserRouter>
                <Routes>
                    {/* Public Routes */}
                    <Route path="/" element={<LandingPage />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    <Route path="/verify-otp" element={<OtpVerification />} />

                    {/* Admin Routes */}
                    <Route path="/admin" element={<Layout />}>
                        <Route index element={<AdminDashboard />} />
                        <Route path="dashboard" element={<AdminDashboard />} />
                        <Route path="users" element={<AdminUserManagement />} />
                        <Route path="analytics" element={<AdminAnalytics />} />
                        <Route path="profile" element={<UserProfile />} />
                    </Route>

                    {/* Lender Routes */}
                    <Route path="/lender" element={<Layout />}>
                        <Route index element={<LenderDashboard />} />
                        <Route path="dashboard" element={<LenderDashboard />} />
                        <Route path="loans" element={<LenderLoans />} />
                        <Route path="loans/:id" element={<LenderLoans />} />
                        <Route path="applications" element={<LenderApplications />} />
                        <Route path="payments" element={<LenderPayments />} />
                        <Route path="create" element={<LenderCreateLoan />} />
                        <Route path="withdraw" element={<LenderWithdraw />} />
                        <Route path="profile" element={<UserProfile />} />
                    </Route>

                    {/* Borrower Routes */}
                    <Route path="/borrower" element={<Layout />}>
                        <Route index element={<BorrowerDashboard />} />
                        <Route path="dashboard" element={<BorrowerDashboard />} />
                        <Route path="loans" element={<BorrowerDashboard />} />
                        <Route path="applications" element={<BorrowerDashboard />} />
                        <Route path="payments" element={<BorrowerDashboard />} />
                        <Route path="offers" element={<BorrowerDashboard />} />
                        <Route path="credit-score" element={<BorrowerDashboard />} />
                        <Route path="profile" element={<UserProfile />} />
                    </Route>

                    {/* Analyst Routes */}
                    <Route path="/analyst" element={<Layout />}>
                        <Route index element={<AnalystDashboard />} />
                        <Route path="dashboard" element={<AnalystDashboard />} />
                        <Route path="risk" element={<AnalystRisk />} />
                        <Route path="payments" element={<AnalystPayments />} />
                        <Route path="loans" element={<AnalystLoans />} />
                        <Route path="profile" element={<UserProfile />} />
                    </Route>

                    {/* Catch all */}
                    <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
            </BrowserRouter>
        </AuthProvider>
    );
}

export default App;
