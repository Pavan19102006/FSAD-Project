import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { analystAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { WelcomeModal, TutorialModal, useOnboarding } from '../components/Onboarding';
import {
    PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, CartesianGrid,
    Tooltip, ResponsiveContainer, Legend, RadialBarChart, RadialBar,
    AreaChart, Area, LineChart, Line
} from 'recharts';

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
                <p style={{ fontWeight: 600, marginBottom: 4, color: '#1d1d1f' }}>{label || payload[0]?.name}</p>
                {payload.map((entry, i) => (
                    <p key={i} style={{ color: entry.color || entry.payload?.fill, fontSize: 13, margin: '2px 0' }}>
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

const STATUS_COLORS = {
    ACTIVE: '#10b981',
    COMPLETED: '#3b82f6',
    DEFAULTED: '#ef4444',
    PENDING: '#f59e0b',
    APPROVED: '#6366f1',
    FUNDED: '#8b5cf6',
    CLOSED: '#64748b',
};

const AnalystDashboard = () => {
    const { user } = useAuth();
    const [analytics, setAnalytics] = useState(null);
    const [risk, setRisk] = useState(null);
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
        fetchData();
        fetchTrends();
        const interval = setInterval(() => {
            fetchData();
            fetchTrends();
        }, 30000);
        return () => clearInterval(interval);
    }, []);

    const fetchData = async () => {
        try {
            const [analyticsRes, riskRes] = await Promise.all([
                analystAPI.getLoanAnalytics(),
                analystAPI.getRiskAssessment(),
            ]);
            setAnalytics(analyticsRes.data.data);
            setRisk(riskRes.data.data);
        } catch (err) {
            setError('Failed to load analytics data');
        } finally {
            setLoading(false);
        }
    };

    const fetchTrends = async () => {
        try {
            const response = await analystAPI.getTrends();
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
                <p>Loading analytics...</p>
            </div>
        );
    }

    if (error) {
        return <div className="alert alert-error">{error}</div>;
    }

    // Prepare chart data
    const statusDistribution = analytics?.statusDistribution || {};
    const statusPieData = Object.entries(statusDistribution).map(([status, count]) => ({
        name: status.replace('_', ' '),
        value: count,
        color: STATUS_COLORS[status] || '#6b7280',
    })).filter(d => d.value > 0);

    const riskScore = risk?.portfolioRiskScore || 0;
    const riskColor = riskScore > 50 ? '#ef4444' : riskScore > 20 ? '#f59e0b' : '#10b981';

    const riskBarData = [
        { name: 'Overdue Count', value: risk?.overduePaymentsCount || 0, fill: '#ef4444' },
        { name: 'Loans at Risk', value: risk?.loansAtRisk || 0, fill: '#f59e0b' },
    ];

    const riskGaugeData = [
        { name: 'Risk', value: Math.min(riskScore, 100), fill: riskColor },
    ];

    const financialMetrics = [
        { name: 'Avg Loan', amount: analytics?.averageLoanAmount || 0, fill: '#6366f1' },
        { name: 'Overdue Amt', amount: risk?.totalOverdueAmount || 0, fill: '#ef4444' },
    ];

    const renderCustomLabel = ({ cx, cy, midAngle, innerRadius, outerRadius, percent }) => {
        const RADIAN = Math.PI / 180;
        const radius = innerRadius + (outerRadius - innerRadius) * 0.5;
        const x = cx + radius * Math.cos(-midAngle * RADIAN);
        const y = cy + radius * Math.sin(-midAngle * RADIAN);
        if (percent < 0.05) return null;
        return (
            <text x={x} y={y} fill="white" textAnchor="middle" dominantBaseline="central" fontSize={11} fontWeight={600}>
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
                    <h1 className="page-title">Financial Analytics</h1>
                    <p className="page-subtitle">Analyze loan data and assess risks</p>
                </div>

                {/* Stats Row */}
                <h2 style={{ marginBottom: '16px', fontSize: '18px' }}>Loan Portfolio Overview</h2>
                <div className="stats-grid">
                    <div className="stat-card">
                        <div className="stat-label">Average Loan Amount</div>
                        <div className="stat-value">{formatCurrency(analytics?.averageLoanAmount)}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Average Interest Rate</div>
                        <div className="stat-value">{analytics?.averageInterestRate?.toFixed(2) || 0}%</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Default Rate</div>
                        <div className="stat-value">{analytics?.defaultRate?.toFixed(2) || 0}%</div>
                        <div className={`stat-change ${analytics?.defaultRate > 5 ? 'negative' : 'positive'}`}>
                            {analytics?.defaultRate > 5 ? 'High risk' : 'Low risk'}
                        </div>
                    </div>
                </div>

                {/* Charts Row 1: Status Pie + Risk Gauge */}
                <div className="charts-grid">
                    <div className="chart-card">
                        <div className="chart-header">
                            <div>
                                <div className="chart-title">🍩 Loan Status Distribution</div>
                                <div className="chart-subtitle">Portfolio composition by status</div>
                            </div>
                        </div>
                        <div className="chart-wrapper">
                            {statusPieData.length > 0 ? (
                                <ResponsiveContainer width="100%" height={300}>
                                    <PieChart>
                                        <Pie
                                            data={statusPieData}
                                            cx="50%"
                                            cy="50%"
                                            innerRadius={60}
                                            outerRadius={100}
                                            paddingAngle={3}
                                            dataKey="value"
                                            labelLine={false}
                                            label={renderCustomLabel}
                                            animationDuration={1200}
                                        >
                                            {statusPieData.map((entry, index) => (
                                                <Cell key={index} fill={entry.color} stroke="none" />
                                            ))}
                                        </Pie>
                                        <Tooltip content={<CustomTooltip />} />
                                        <Legend
                                            verticalAlign="bottom"
                                            height={36}
                                            formatter={(value) => <span style={{ color: '#6b7280', fontSize: 12 }}>{value}</span>}
                                        />
                                    </PieChart>
                                </ResponsiveContainer>
                            ) : (
                                <p style={{ color: 'var(--text-muted)' }}>No distribution data</p>
                            )}
                        </div>
                    </div>

                    <div className="chart-card">
                        <div className="chart-header">
                            <div>
                                <div className="chart-title">🎯 Portfolio Risk Score</div>
                                <div className="chart-subtitle">Overall portfolio health gauge</div>
                            </div>
                        </div>
                        <div className="chart-wrapper" style={{ flexDirection: 'column' }}>
                            <ResponsiveContainer width="100%" height={220}>
                                <RadialBarChart
                                    cx="50%"
                                    cy="50%"
                                    innerRadius="60%"
                                    outerRadius="90%"
                                    barSize={20}
                                    data={riskGaugeData}
                                    startAngle={180}
                                    endAngle={0}
                                >
                                    <RadialBar
                                        background={{ fill: 'rgba(0,0,0,0.05)' }}
                                        dataKey="value"
                                        cornerRadius={10}
                                        animationDuration={1500}
                                    />
                                    <text x="50%" y="42%" textAnchor="middle" dominantBaseline="middle"
                                        style={{ fontSize: '36px', fontWeight: 700, fill: riskColor }}>
                                        {riskScore.toFixed(1)}
                                    </text>
                                    <text x="50%" y="56%" textAnchor="middle" dominantBaseline="middle"
                                        style={{ fontSize: '13px', fill: '#6b7280' }}>
                                        out of 100
                                    </text>
                                </RadialBarChart>
                            </ResponsiveContainer>
                            <div style={{
                                display: 'flex', gap: '16px', justifyContent: 'center',
                                marginTop: '8px', fontSize: '12px', color: '#6b7280'
                            }}>
                                <span style={{ color: '#10b981' }}>● Low (0-20)</span>
                                <span style={{ color: '#f59e0b' }}>● Moderate (20-50)</span>
                                <span style={{ color: '#ef4444' }}>● High (50+)</span>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Charts Row 2: Risk Metrics Bar + Financial Bar */}
                <div className="charts-grid">
                    <div className="chart-card">
                        <div className="chart-header">
                            <div>
                                <div className="chart-title">⚠️ Risk Indicators</div>
                                <div className="chart-subtitle">Overdue payments and at-risk loans</div>
                            </div>
                        </div>
                        <div className="chart-wrapper">
                            <ResponsiveContainer width="100%" height={280}>
                                <BarChart data={riskBarData} barCategoryGap="30%" layout="vertical">
                                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(0,0,0,0.06)" />
                                    <XAxis
                                        type="number"
                                        tick={{ fill: '#6b7280', fontSize: 12 }}
                                        axisLine={{ stroke: 'rgba(0,0,0,0.08)' }}
                                        allowDecimals={false}
                                    />
                                    <YAxis
                                        type="category"
                                        dataKey="name"
                                        tick={{ fill: '#6b7280', fontSize: 12 }}
                                        axisLine={{ stroke: 'rgba(0,0,0,0.08)' }}
                                        width={100}
                                    />
                                    <Tooltip content={<CustomTooltip />} />
                                    <Bar dataKey="value" name="Count" radius={[0, 8, 8, 0]} animationDuration={1200}>
                                        {riskBarData.map((entry, index) => (
                                            <Cell key={index} fill={entry.fill} />
                                        ))}
                                    </Bar>
                                </BarChart>
                            </ResponsiveContainer>
                        </div>
                    </div>

                    <div className="chart-card">
                        <div className="chart-header">
                            <div>
                                <div className="chart-title">💰 Financial Metrics</div>
                                <div className="chart-subtitle">Average loan vs overdue amounts</div>
                            </div>
                        </div>
                        <div className="chart-wrapper">
                            <ResponsiveContainer width="100%" height={280}>
                                <BarChart data={financialMetrics} barCategoryGap="30%">
                                    <defs>
                                        <linearGradient id="analystGradPurple" x1="0" y1="0" x2="0" y2="1">
                                            <stop offset="0%" stopColor="#6366f1" stopOpacity={1} />
                                            <stop offset="100%" stopColor="#6366f1" stopOpacity={0.5} />
                                        </linearGradient>
                                        <linearGradient id="analystGradRed" x1="0" y1="0" x2="0" y2="1">
                                            <stop offset="0%" stopColor="#ef4444" stopOpacity={1} />
                                            <stop offset="100%" stopColor="#ef4444" stopOpacity={0.5} />
                                        </linearGradient>
                                    </defs>
                                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(0,0,0,0.06)" />
                                    <XAxis
                                        dataKey="name"
                                        tick={{ fill: '#6b7280', fontSize: 12 }}
                                        axisLine={{ stroke: 'rgba(0,0,0,0.08)' }}
                                    />
                                    <YAxis
                                        tick={{ fill: '#6b7280', fontSize: 12 }}
                                        axisLine={{ stroke: 'rgba(0,0,0,0.08)' }}
                                        tickFormatter={(v) => v >= 1000 ? `₹${(v / 1000).toFixed(0)}k` : `₹${v}`}
                                    />
                                    <Tooltip content={<CustomTooltip />} />
                                    <Bar dataKey="amount" name="Amount" radius={[8, 8, 0, 0]} animationDuration={1200}>
                                        {financialMetrics.map((entry, index) => (
                                            <Cell key={index} fill={
                                                index === 0 ? 'url(#analystGradPurple)' : 'url(#analystGradRed)'
                                            } />
                                        ))}
                                    </Bar>
                                </BarChart>
                            </ResponsiveContainer>
                        </div>
                    </div>
                </div>

                {/* Risk Assessment Stats */}
                <h2 style={{ marginBottom: '16px', marginTop: '8px', fontSize: '18px' }}>Risk Assessment Summary</h2>
                <div className="stats-grid">
                    <div className="stat-card">
                        <div className="stat-label">Overdue Payments</div>
                        <div className="stat-value">{risk?.overduePaymentsCount || 0}</div>
                        <div className="stat-change negative">Require attention</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Total Overdue Amount</div>
                        <div className="stat-value">{formatCurrency(risk?.totalOverdueAmount)}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Loans at Risk</div>
                        <div className="stat-value">{risk?.loansAtRisk || 0}</div>
                        <div className="stat-change negative">With missed payments</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Portfolio Risk Score</div>
                        <div className="stat-value">{risk?.portfolioRiskScore?.toFixed(1) || 0}</div>
                        <div className={`stat-change ${risk?.portfolioRiskScore > 20 ? 'negative' : 'positive'}`}>
                            out of 100
                        </div>
                    </div>
                </div>

                {/* Portfolio & Collections Trend Charts */}
                {trends.length > 0 && (
                    <div className="charts-grid">
                        <div className="chart-card full-width">
                            <div className="chart-header">
                                <div>
                                    <div className="chart-title">📉 Portfolio Performance Trend</div>
                                    <div className="chart-subtitle">Portfolio value and active loans over 12 months • Auto-updates</div>
                                </div>
                            </div>
                            <div className="chart-wrapper">
                                <ResponsiveContainer width="100%" height={300}>
                                    <AreaChart data={trends}>
                                        <defs>
                                            <linearGradient id="analystGradPortfolio" x1="0" y1="0" x2="0" y2="1">
                                                <stop offset="0%" stopColor="#8b5cf6" stopOpacity={0.35} />
                                                <stop offset="100%" stopColor="#8b5cf6" stopOpacity={0.02} />
                                            </linearGradient>
                                            <linearGradient id="analystGradLoans" x1="0" y1="0" x2="0" y2="1">
                                                <stop offset="0%" stopColor="#3b82f6" stopOpacity={0.25} />
                                                <stop offset="100%" stopColor="#3b82f6" stopOpacity={0.02} />
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
                                            tickFormatter={(v) => v >= 1000 ? `₹${(v / 1000).toFixed(0)}k` : v}
                                        />
                                        <Tooltip content={<CustomTooltip />} />
                                        <Legend />
                                        <Area
                                            type="monotone"
                                            dataKey="portfolioValue"
                                            name="Portfolio Value"
                                            stroke="#8b5cf6"
                                            strokeWidth={2.5}
                                            fill="url(#analystGradPortfolio)"
                                            dot={{ r: 4, fill: '#8b5cf6', strokeWidth: 2, stroke: '#fff' }}
                                            activeDot={{ r: 6, strokeWidth: 2 }}
                                            animationDuration={2000}
                                            animationEasing="ease-in-out"
                                        />
                                        <Area
                                            type="monotone"
                                            dataKey="loanVolume"
                                            name="Loan Volume"
                                            stroke="#3b82f6"
                                            strokeWidth={2}
                                            fill="url(#analystGradLoans)"
                                            dot={{ r: 3, fill: '#3b82f6', strokeWidth: 2, stroke: '#fff' }}
                                            activeDot={{ r: 5, strokeWidth: 2 }}
                                            animationDuration={2200}
                                            animationEasing="ease-in-out"
                                        />
                                    </AreaChart>
                                </ResponsiveContainer>
                            </div>
                        </div>

                        {/* Collections vs Interest Line Chart */}
                        <div className="chart-card full-width">
                            <div className="chart-header">
                                <div>
                                    <div className="chart-title">💰 Collections & Interest Earning</div>
                                    <div className="chart-subtitle">Monthly payment collections vs interest earned</div>
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
                                            tick={{ fill: '#6b7280', fontSize: 11 }}
                                            axisLine={{ stroke: 'rgba(0,0,0,0.08)' }}
                                            tickFormatter={(v) => v >= 1000 ? `₹${(v / 1000).toFixed(0)}k` : `₹${v}`}
                                        />
                                        <Tooltip content={<CustomTooltip />} />
                                        <Legend />
                                        <Line
                                            type="monotone"
                                            dataKey="paymentsCollected"
                                            name="Collections"
                                            stroke="#10b981"
                                            strokeWidth={3}
                                            dot={{ r: 5, fill: '#10b981', strokeWidth: 2, stroke: '#fff' }}
                                            activeDot={{ r: 7, strokeWidth: 2 }}
                                            animationDuration={1800}
                                            animationEasing="ease-in-out"
                                        />
                                        <Line
                                            type="monotone"
                                            dataKey="interestEarned"
                                            name="Interest Earned"
                                            stroke="#f59e0b"
                                            strokeWidth={2.5}
                                            strokeDasharray="5 5"
                                            dot={{ r: 4, fill: '#f59e0b', strokeWidth: 2, stroke: '#fff' }}
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
                        <Link to="/analyst/loans" className="btn btn-primary">📋 View All Loans</Link>
                        <Link to="/analyst/risk" className="btn btn-secondary">⚠️ Risk Report</Link>
                        <Link to="/analyst/payments" className="btn btn-secondary">💸 Payment Analysis</Link>
                    </div>
                </div>
            </div>
            <button className="help-btn" onClick={resetTutorial} title="Show Tutorial">
                ❓
            </button>
        </>
    );
};

export default AnalystDashboard;
