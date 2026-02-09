import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Layout from './components/Layout';
import Login from './pages/Login';
import Register from './pages/Register';
import LandingPage from './pages/LandingPage';
import AdminDashboard from './pages/AdminDashboard';
import AdminUserManagement from './pages/AdminUserManagement';
import LenderDashboard from './pages/LenderDashboard';
import LenderLoans from './pages/LenderLoans';
import LenderApplications from './pages/LenderApplications';
import LenderCreateLoan from './pages/LenderCreateLoan';
import LenderWithdraw from './pages/LenderWithdraw';
import LenderPayments from './pages/LenderPayments';
import BorrowerDashboard from './pages/BorrowerDashboard';
import AnalystDashboard from './pages/AnalystDashboard';

function App() {
    return (
        <AuthProvider>
            <BrowserRouter>
                <Routes>
                    {/* Public Routes */}
                    <Route path="/" element={<LandingPage />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />

                    {/* Admin Routes */}
                    <Route path="/admin" element={<Layout />}>
                        <Route index element={<AdminDashboard />} />
                        <Route path="dashboard" element={<AdminDashboard />} />
                        <Route path="users" element={<AdminUserManagement />} />
                        <Route path="analytics" element={<AdminDashboard />} />
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
                    </Route>

                    {/* Analyst Routes */}
                    <Route path="/analyst" element={<Layout />}>
                        <Route index element={<AnalystDashboard />} />
                        <Route path="dashboard" element={<AnalystDashboard />} />
                        <Route path="risk" element={<AnalystDashboard />} />
                        <Route path="payments" element={<AnalystDashboard />} />
                        <Route path="loans" element={<AnalystDashboard />} />
                    </Route>

                    {/* Catch all */}
                    <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
            </BrowserRouter>
        </AuthProvider>
    );
}

export default App;
