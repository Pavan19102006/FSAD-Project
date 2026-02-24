import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { adminAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { WelcomeModal, TutorialModal, useOnboarding } from '../components/Onboarding';
import {
    PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, CartesianGrid,
    Tooltip, ResponsiveContainer, Legend, AreaChart, Area, LineChart, Line
} from 'recharts';

const COLORS = {
    active: '#10b981',
    completed: '#3b82f6',
    defaulted: '#ef4444',
    pending: '#f59e0b',
    lenders: '#6366f1',
    borrowers: '#10b981',
    analysts: '#f59e0b',
    admins: '#8b5cf6',
};

const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
        return (
            <div style={{
                background: 'rgba(255,255,255,0.95)',
                border: '1px solid rgba(0,0,0,0.08)',
                borderRadius: '12px',
                padding: '12px 16px',
                boxShadow: '0 10px 25px rgba(0,0,0,0.1)',
            }}>
                <p style={{ fontWeight: 600, marginBottom: 4, color: '#1d1d1f' }}>{label}</p>
                {payload.map((entry, i) => (
                    <p key={i} style={{ color: entry.color, fontSize: 13, margin: '2px 0' }}>
                        {entry.name}: {typeof entry.value === 'number' && entry.value > 100
                            ? `₹${entry.value.toLocaleString()}`
                            : entry.value}
                    </p>
                ))}
            </div>
        );
    }
    return null;
};

const AdminDashboard = () => {
    const { user } = useAuth();
    const [dashboard, setDashboard] = useState(null);
    const [trends, setTrends] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const {
        showWelcome,
        showTutorial,
        tutorialStep,
        totalSteps,
        checkFirstTimeUser,
        startTutorial,
        closeWelcome,
        closeTutorial,
        nextStep,
        prevStep,
        resetTutorial
    } = useOnboarding(user);

    useEffect(() => {
        checkFirstTimeUser();
    }, [user]);

    useEffect(() => {
        fetchDashboard();
        fetchTrends();
        // Auto-refresh every 30 seconds
        const interval = setInterval(() => {
            fetchDashboard();
            fetchTrends();
        }, 30000);
        return () => clearInterval(interval);
    }, []);

    const fetchDashboard = async () => {
        try {
            const response = await adminAPI.getDashboard();
            setDashboard(response.data.data);
        } catch (err) {
            setError('Failed to load dashboard data');
        } finally {
            setLoading(false);
        }
    };

    const fetchTrends = async () => {
        try {
            const response = await adminAPI.getTrends();
            setTrends(response.data.data || []);
        } catch (err) {
            console.log('Trends data not available');
        }
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'INR',
        }).format(amount || 0);
    };

    if (loading) {
        return (
            <div className="loading-container">
                <div className="loading-spinner"></div>
                <p>Loading dashboard...</p>
            </div>
        );
    }

    if (error) {
        return <div className="alert alert-error">{error}</div>;
    }

    // Prepare chart data
    const loanStatusData = [
        { name: 'Active', value: dashboard?.activeLoans || 0, color: COLORS.active },
        { name: 'Completed', value: dashboard?.completedLoans || 0, color: COLORS.completed },
        { name: 'Defaulted', value: dashboard?.defaultedLoans || 0, color: COLORS.defaulted },
    ].filter(d => d.value > 0);

    const userDistributionData = [
        { name: 'Lenders', value: dashboard?.totalLenders || 0, color: COLORS.lenders },
        { name: 'Borrowers', value: dashboard?.totalBorrowers || 0, color: COLORS.borrowers },
        { name: 'Analysts', value: dashboard?.totalAnalysts || 0, color: COLORS.analysts },
    ];

    const financialData = [
        { name: 'Total Loans', amount: dashboard?.totalLoanAmount || 0 },
        { name: 'Active Value', amount: dashboard?.totalActiveAmount || 0 },
        { name: 'Interest', amount: dashboard?.totalInterestEarned || 0 },
    ];

    const totalLoans = dashboard?.totalLoans || 0;

    const renderCustomLabel = ({ cx, cy, midAngle, innerRadius, outerRadius, percent, name }) => {
        const RADIAN = Math.PI / 180;
        const radius = innerRadius + (outerRadius - innerRadius) * 0.5;
        const x = cx + radius * Math.cos(-midAngle * RADIAN);
        const y = cy + radius * Math.sin(-midAngle * RADIAN);
        if (percent < 0.05) return null;
        return (
            <text x={x} y={y} fill="white" textAnchor="middle" dominantBaseline="central" fontSize={12} fontWeight={600}>
                {`${(percent * 100).toFixed(0)}%`}
            </text>
        );
    };

    return (
        <>
            {showWelcome && (
                <WelcomeModal
                    user={user}
                    onClose={closeWelcome}
                    onStartTutorial={startTutorial}
                />
            )}
            {showTutorial && (
                <TutorialModal
                    user={user}
                    currentStep={tutorialStep}
                    totalSteps={totalSteps}
                    onNext={nextStep}
                    onPrev={prevStep}
                    onClose={closeTutorial}
                />
            )}
            <div className="fade-in">
                <div className="page-header">
                    <h1 className="page-title">Admin Dashboard</h1>
                    <p className="page-subtitle">Platform overview and management</p>
                </div>

                {/* Stats Row 1 - Users */}
                <div className="stats-grid">
                    <div className="stat-card">
                        <div className="stat-label">Total Users</div>
                        <div className="stat-value">{dashboard?.totalUsers || 0}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Lenders</div>
                        <div className="stat-value">{dashboard?.totalLenders || 0}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Borrowers</div>
                        <div className="stat-value">{dashboard?.totalBorrowers || 0}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Analysts</div>
                        <div className="stat-value">{dashboard?.totalAnalysts || 0}</div>
                    </div>
                </div>

                {/* Stats Row 2 - Loans */}
                <div className="stats-grid">
                    <div className="stat-card">
                        <div className="stat-label">Total Loans</div>
                        <div className="stat-value">{totalLoans}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Active Loans</div>
                        <div className="stat-value">{dashboard?.activeLoans || 0}</div>
                        <div className="stat-change positive">Currently active</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Total Loan Amount</div>
                        <div className="stat-value">{formatCurrency(dashboard?.totalLoanAmount)}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Pending Applications</div>
                        <div className="stat-value">{dashboard?.pendingApplications || 0}</div>
                        <div className="stat-change">Awaiting review</div>
                    </div>
                </div>

                {/* Charts Row */}
                <div className="charts-grid">
                    {/* Loan Status Pie Chart */}
                    <div className="chart-card">
                        <div className="chart-header">
                            <div>
                                <div className="chart-title">🍩 Loan Status Distribution</div>
                                <div className="chart-subtitle">Active vs Completed vs Defaulted</div>
                            </div>
                        </div>
                        <div className="chart-wrapper">
                            {loanStatusData.length > 0 ? (
                                <ResponsiveContainer width="100%" height={280}>
                                    <PieChart>
                                        <Pie
                                            data={loanStatusData}
                                            cx="50%"
                                            cy="50%"
                                            innerRadius={60}
                                            outerRadius={100}
                                            paddingAngle={4}
                                            dataKey="value"
                                            labelLine={false}
                                            label={renderCustomLabel}
                                            animationBegin={0}
                                            animationDuration={1200}
                                        >
                                            {loanStatusData.map((entry, index) => (
                                                <Cell key={index} fill={entry.color} stroke="none" />
                                            ))}
                                        </Pie>
                                        <Tooltip content={<CustomTooltip />} />
                                        <Legend
                                            verticalAlign="bottom"
                                            height={36}
                                            formatter={(value) => <span style={{ color: '#6b7280', fontSize: 13 }}>{value}</span>}
                                        />
                                        {/* Center label */}
                                        <text x="50%" y="47%" className="chart-center-label" dominantBaseline="middle">
                                            {totalLoans}
                                        </text>
                                        <text x="50%" y="55%" className="chart-center-sublabel" dominantBaseline="middle">
                                            Total
                                        </text>
                                    </PieChart>
                                </ResponsiveContainer>
                            ) : (
                                <p style={{ color: 'var(--text-muted)' }}>No loan data available</p>
                            )}
                        </div>
                    </div>

                    {/* User Distribution Bar Chart */}
                    <div className="chart-card">
                        <div className="chart-header">
                            <div>
                                <div className="chart-title">👥 User Distribution</div>
                                <div className="chart-subtitle">Users by role</div>
                            </div>
                        </div>
                        <div className="chart-wrapper">
                            <ResponsiveContainer width="100%" height={280}>
                                <BarChart data={userDistributionData} barCategoryGap="30%">
                                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(0,0,0,0.06)" />
                                    <XAxis
                                        dataKey="name"
                                        tick={{ fill: '#6b7280', fontSize: 13 }}
                                        axisLine={{ stroke: 'rgba(0,0,0,0.08)' }}
                                    />
                                    <YAxis
                                        tick={{ fill: '#6b7280', fontSize: 12 }}
                                        axisLine={{ stroke: 'rgba(0,0,0,0.08)' }}
                                        allowDecimals={false}
                                    />
                                    <Tooltip content={<CustomTooltip />} />
                                    <Bar
                                        dataKey="value"
                                        name="Users"
                                        radius={[8, 8, 0, 0]}
                                        animationDuration={1200}
                                    >
                                        {userDistributionData.map((entry, index) => (
                                            <Cell key={index} fill={entry.color} />
                                        ))}
                                    </Bar>
                                </BarChart>
                            </ResponsiveContainer>
                        </div>
                    </div>
                </div>

                {/* Financial Overview - Full Width */}
                <div className="charts-grid">
                    <div className="chart-card full-width">
                        <div className="chart-header">
                            <div>
                                <div className="chart-title">💰 Financial Overview</div>
                                <div className="chart-subtitle">Loan volume, active portfolio value, and interest earned</div>
                            </div>
                        </div>
                        <div className="chart-wrapper">
                            <ResponsiveContainer width="100%" height={300}>
                                <BarChart data={financialData} barCategoryGap="25%">
                                    <defs>
                                        <linearGradient id="gradBlue" x1="0" y1="0" x2="0" y2="1">
                                            <stop offset="0%" stopColor="#6366f1" stopOpacity={1} />
                                            <stop offset="100%" stopColor="#6366f1" stopOpacity={0.6} />
                                        </linearGradient>
                                    </defs>
                                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(0,0,0,0.06)" />
                                    <XAxis
                                        dataKey="name"
                                        tick={{ fill: '#6b7280', fontSize: 13 }}
                                        axisLine={{ stroke: 'rgba(0,0,0,0.08)' }}
                                    />
                                    <YAxis
                                        tick={{ fill: '#6b7280', fontSize: 12 }}
                                        axisLine={{ stroke: 'rgba(0,0,0,0.08)' }}
                                        tickFormatter={(v) => `₹${(v / 1000).toFixed(0)}k`}
                                    />
                                    <Tooltip content={<CustomTooltip />} />
                                    <Bar
                                        dataKey="amount"
                                        name="Amount"
                                        fill="url(#gradBlue)"
                                        radius={[8, 8, 0, 0]}
                                        animationDuration={1200}
                                    />
                                </BarChart>
                            </ResponsiveContainer>
                        </div>
                    </div>
                </div>

                {/* Monthly Trends - Area Chart */}
                {trends.length > 0 && (
                    <div className="charts-grid">
                        <div className="chart-card full-width">
                            <div className="chart-header">
                                <div>
                                    <div className="chart-title">📈 Monthly Trends</div>
                                    <div className="chart-subtitle">Loan volume, collections & interest over 12 months • Auto-refreshes every 30s</div>
                                </div>
                                <div style={{ display: 'flex', gap: '12px', fontSize: '11px' }}>
                                    <span style={{ color: '#6366f1' }}>● Loan Volume</span>
                                    <span style={{ color: '#10b981' }}>● Collections</span>
                                    <span style={{ color: '#f59e0b' }}>● Interest</span>
                                </div>
                            </div>
                            <div className="chart-wrapper">
                                <ResponsiveContainer width="100%" height={320}>
                                    <AreaChart data={trends}>
                                        <defs>
                                            <linearGradient id="trendGradVolume" x1="0" y1="0" x2="0" y2="1">
                                                <stop offset="0%" stopColor="#6366f1" stopOpacity={0.3} />
                                                <stop offset="100%" stopColor="#6366f1" stopOpacity={0.02} />
                                            </linearGradient>
                                            <linearGradient id="trendGradCollect" x1="0" y1="0" x2="0" y2="1">
                                                <stop offset="0%" stopColor="#10b981" stopOpacity={0.3} />
                                                <stop offset="100%" stopColor="#10b981" stopOpacity={0.02} />
                                            </linearGradient>
                                            <linearGradient id="trendGradInterest" x1="0" y1="0" x2="0" y2="1">
                                                <stop offset="0%" stopColor="#f59e0b" stopOpacity={0.25} />
                                                <stop offset="100%" stopColor="#f59e0b" stopOpacity={0.02} />
                                            </linearGradient>
                                        </defs>
                                        <CartesianGrid strokeDasharray="3 3" stroke="rgba(0,0,0,0.06)" />
                                        <XAxis
                                            dataKey="month"
                                            tick={{ fill: '#6b7280', fontSize: 11 }}
                                            axisLine={{ stroke: 'rgba(0,0,0,0.08)' }}
                                        />
                                        <YAxis
                                            tick={{ fill: '#6b7280', fontSize: 11 }}
                                            axisLine={{ stroke: 'rgba(0,0,0,0.08)' }}
                                            tickFormatter={(v) => v >= 1000 ? `₹${(v / 1000).toFixed(0)}k` : `₹${v}`}
                                        />
                                        <Tooltip content={<CustomTooltip />} />
                                        <Area
                                            type="monotone"
                                            dataKey="loanVolume"
                                            name="Loan Volume"
                                            stroke="#6366f1"
                                            strokeWidth={2.5}
                                            fill="url(#trendGradVolume)"
                                            dot={{ r: 4, fill: '#6366f1', strokeWidth: 2, stroke: '#fff' }}
                                            activeDot={{ r: 6, strokeWidth: 2 }}
                                            animationDuration={2000}
                                            animationEasing="ease-in-out"
                                        />
                                        <Area
                                            type="monotone"
                                            dataKey="paymentsCollected"
                                            name="Collections"
                                            stroke="#10b981"
                                            strokeWidth={2.5}
                                            fill="url(#trendGradCollect)"
                                            dot={{ r: 4, fill: '#10b981', strokeWidth: 2, stroke: '#fff' }}
                                            activeDot={{ r: 6, strokeWidth: 2 }}
                                            animationDuration={2200}
                                            animationEasing="ease-in-out"
                                        />
                                        <Area
                                            type="monotone"
                                            dataKey="interestEarned"
                                            name="Interest"
                                            stroke="#f59e0b"
                                            strokeWidth={2}
                                            fill="url(#trendGradInterest)"
                                            dot={{ r: 3, fill: '#f59e0b', strokeWidth: 2, stroke: '#fff' }}
                                            activeDot={{ r: 5, strokeWidth: 2 }}
                                            animationDuration={2400}
                                            animationEasing="ease-in-out"
                                        />
                                    </AreaChart>
                                </ResponsiveContainer>
                            </div>
                        </div>

                        {/* Active Loans Trend - Line Chart */}
                        <div className="chart-card full-width">
                            <div className="chart-header">
                                <div>
                                    <div className="chart-title">🏦 Active Loans & Portfolio Value</div>
                                    <div className="chart-subtitle">Number of active loans and total portfolio value over time</div>
                                </div>
                            </div>
                            <div className="chart-wrapper">
                                <ResponsiveContainer width="100%" height={280}>
                                    <LineChart data={trends}>
                                        <CartesianGrid strokeDasharray="3 3" stroke="rgba(0,0,0,0.06)" />
                                        <XAxis
                                            dataKey="month"
                                            tick={{ fill: '#6b7280', fontSize: 11 }}
                                            axisLine={{ stroke: 'rgba(0,0,0,0.08)' }}
                                        />
                                        <YAxis
                                            yAxisId="left"
                                            tick={{ fill: '#6b7280', fontSize: 11 }}
                                            axisLine={{ stroke: 'rgba(0,0,0,0.08)' }}
                                        />
                                        <YAxis
                                            yAxisId="right"
                                            orientation="right"
                                            tick={{ fill: '#6b7280', fontSize: 11 }}
                                            axisLine={{ stroke: 'rgba(0,0,0,0.08)' }}
                                            tickFormatter={(v) => v >= 1000 ? `₹${(v / 1000).toFixed(0)}k` : `₹${v}`}
                                        />
                                        <Tooltip content={<CustomTooltip />} />
                                        <Legend />
                                        <Line
                                            yAxisId="left"
                                            type="monotone"
                                            dataKey="activeLoans"
                                            name="Active Loans"
                                            stroke="#3b82f6"
                                            strokeWidth={3}
                                            dot={{ r: 5, fill: '#3b82f6', strokeWidth: 2, stroke: '#fff' }}
                                            activeDot={{ r: 7, strokeWidth: 2 }}
                                            animationDuration={1800}
                                            animationEasing="ease-in-out"
                                        />
                                        <Line
                                            yAxisId="right"
                                            type="monotone"
                                            dataKey="portfolioValue"
                                            name="Portfolio Value"
                                            stroke="#f97316"
                                            strokeWidth={2.5}
                                            strokeDasharray="5 5"
                                            dot={{ r: 4, fill: '#f97316', strokeWidth: 2, stroke: '#fff' }}
                                            activeDot={{ r: 6, strokeWidth: 2 }}
                                            animationDuration={2000}
                                            animationEasing="ease-in-out"
                                        />
                                    </LineChart>
                                </ResponsiveContainer>
                            </div>
                        </div>
                    </div>
                )}

                {/* Quick Actions */}
                <div className="card" style={{ marginTop: '24px' }}>
                    <div className="card-header">
                        <h3 className="card-title">Quick Actions</h3>
                    </div>
                    <div style={{ display: 'flex', gap: '16px', flexWrap: 'wrap' }}>
                        <Link to="/admin/users" className="btn btn-primary">👥 Manage Users</Link>
                        <Link to="/admin/analytics" className="btn btn-secondary">📈 View Analytics</Link>
                    </div>
                </div>
            </div>
            <button className="help-btn" onClick={resetTutorial} title="Show Tutorial">
                ❓
            </button>
        </>
    );
};

export default AdminDashboard;
