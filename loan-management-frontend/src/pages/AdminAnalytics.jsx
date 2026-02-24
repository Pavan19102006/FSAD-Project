import { useState, useEffect } from 'react';
import { adminAPI } from '../services/api';
import {
    PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, CartesianGrid,
    Tooltip, ResponsiveContainer, Legend, RadialBarChart, RadialBar,
    AreaChart, Area
} from 'recharts';

const COLORS_LOAN = ['#10b981', '#3b82f6', '#ef4444', '#f59e0b'];
const COLORS_USER = ['#6366f1', '#10b981', '#f59e0b'];

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

const AdminAnalytics = () => {
    const [dashboard, setDashboard] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            const response = await adminAPI.getDashboard();
            setDashboard(response.data.data);
        } catch (err) {
            setError('Failed to load analytics data');
        } finally {
            setLoading(false);
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

    const totalLoans = dashboard?.totalLoans || 0;
    const activeLoans = dashboard?.activeLoans || 0;
    const completedLoans = dashboard?.completedLoans || 0;
    const defaultedLoans = dashboard?.defaultedLoans || 0;
    const pendingApplications = dashboard?.pendingApplications || 0;

    const totalUsers = dashboard?.totalUsers || 0;
    const totalLenders = dashboard?.totalLenders || 0;
    const totalBorrowers = dashboard?.totalBorrowers || 0;
    const totalAnalysts = dashboard?.totalAnalysts || 0;

    // Pie chart data
    const loanDistData = [
        { name: 'Active', value: activeLoans, color: '#10b981' },
        { name: 'Completed', value: completedLoans, color: '#3b82f6' },
        { name: 'Defaulted', value: defaultedLoans, color: '#ef4444' },
        { name: 'Pending', value: pendingApplications, color: '#f59e0b' },
    ].filter(d => d.value > 0);

    const userPieData = [
        { name: 'Lenders', value: totalLenders, color: '#6366f1' },
        { name: 'Borrowers', value: totalBorrowers, color: '#10b981' },
        { name: 'Analysts', value: totalAnalysts, color: '#f59e0b' },
    ].filter(d => d.value > 0);

    // Bar chart data
    const financialBarData = [
        { name: 'Total Volume', amount: dashboard?.totalLoanAmount || 0, fill: '#6366f1' },
        { name: 'Active Value', amount: dashboard?.totalActiveAmount || 0, fill: '#10b981' },
        { name: 'Interest', amount: dashboard?.totalInterestEarned || 0, fill: '#f59e0b' },
    ];

    // Loan counts comparison
    const loanCountData = [
        { name: 'Active', count: activeLoans, fill: '#10b981' },
        { name: 'Completed', count: completedLoans, fill: '#3b82f6' },
        { name: 'Defaulted', count: defaultedLoans, fill: '#ef4444' },
        { name: 'Pending', count: pendingApplications, fill: '#f59e0b' },
    ];

    // Success rate gauge
    const successRate = totalLoans ? (((totalLoans - defaultedLoans) / totalLoans) * 100) : 100;
    const gaugeData = [
        { name: 'Success', value: successRate, fill: successRate > 80 ? '#10b981' : successRate > 50 ? '#f59e0b' : '#ef4444' },
    ];

    const defaultPercent = totalLoans ? ((defaultedLoans / totalLoans) * 100).toFixed(1) : 0;

    const renderCustomLabel = ({ cx, cy, midAngle, innerRadius, outerRadius, percent }) => {
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
        <div className="fade-in">
            <div className="page-header">
                <h1 className="page-title">📈 Platform Analytics</h1>
                <p className="page-subtitle">Comprehensive data insights and performance metrics</p>
            </div>

            {/* Financial Overview Stats */}
            <div className="card" style={{ marginBottom: '24px' }}>
                <div className="card-header">
                    <h3 className="card-title">💰 Financial Overview</h3>
                </div>
                <div className="stats-grid">
                    <div className="stat-card" style={{ borderLeft: '4px solid #3b82f6' }}>
                        <div className="stat-label">Total Loan Volume</div>
                        <div className="stat-value">{formatCurrency(dashboard?.totalLoanAmount)}</div>
                    </div>
                    <div className="stat-card" style={{ borderLeft: '4px solid #10b981' }}>
                        <div className="stat-label">Active Loan Value</div>
                        <div className="stat-value">{formatCurrency(dashboard?.totalActiveAmount)}</div>
                    </div>
                    <div className="stat-card" style={{ borderLeft: '4px solid #f59e0b' }}>
                        <div className="stat-label">Total Interest Earned</div>
                        <div className="stat-value">{formatCurrency(dashboard?.totalInterestEarned)}</div>
                    </div>
                    <div className="stat-card" style={{ borderLeft: '4px solid #8b5cf6' }}>
                        <div className="stat-label">Avg Loan Amount</div>
                        <div className="stat-value">{formatCurrency(totalLoans ? (dashboard?.totalLoanAmount || 0) / totalLoans : 0)}</div>
                    </div>
                </div>
            </div>

            {/* Charts Row 1: Loan Distribution Donut + Financial Bar */}
            <div className="charts-grid">
                <div className="chart-card">
                    <div className="chart-header">
                        <div>
                            <div className="chart-title">🍩 Loan Distribution</div>
                            <div className="chart-subtitle">Breakdown by loan status</div>
                        </div>
                    </div>
                    <div className="chart-wrapper">
                        {loanDistData.length > 0 ? (
                            <ResponsiveContainer width="100%" height={300}>
                                <PieChart>
                                    <Pie
                                        data={loanDistData}
                                        cx="50%"
                                        cy="50%"
                                        innerRadius={65}
                                        outerRadius={105}
                                        paddingAngle={4}
                                        dataKey="value"
                                        labelLine={false}
                                        label={renderCustomLabel}
                                        animationBegin={0}
                                        animationDuration={1200}
                                    >
                                        {loanDistData.map((entry, index) => (
                                            <Cell key={index} fill={entry.color} stroke="none" />
                                        ))}
                                    </Pie>
                                    <Tooltip content={<CustomTooltip />} />
                                    <Legend
                                        verticalAlign="bottom"
                                        height={36}
                                        formatter={(value) => <span style={{ color: '#6b7280', fontSize: 13 }}>{value}</span>}
                                    />
                                    <text x="50%" y="46%" className="chart-center-label" dominantBaseline="middle">
                                        {totalLoans}
                                    </text>
                                    <text x="50%" y="55%" className="chart-center-sublabel" dominantBaseline="middle">
                                        Total Loans
                                    </text>
                                </PieChart>
                            </ResponsiveContainer>
                        ) : (
                            <p style={{ color: 'var(--text-muted)' }}>No loan data available</p>
                        )}
                    </div>
                </div>

                <div className="chart-card">
                    <div className="chart-header">
                        <div>
                            <div className="chart-title">📊 Financial Comparison</div>
                            <div className="chart-subtitle">Total volume vs active vs interest earned</div>
                        </div>
                    </div>
                    <div className="chart-wrapper">
                        <ResponsiveContainer width="100%" height={300}>
                            <BarChart data={financialBarData} barCategoryGap="25%">
                                <defs>
                                    <linearGradient id="gradPurple" x1="0" y1="0" x2="0" y2="1">
                                        <stop offset="0%" stopColor="#6366f1" stopOpacity={1} />
                                        <stop offset="100%" stopColor="#6366f1" stopOpacity={0.5} />
                                    </linearGradient>
                                    <linearGradient id="gradGreen" x1="0" y1="0" x2="0" y2="1">
                                        <stop offset="0%" stopColor="#10b981" stopOpacity={1} />
                                        <stop offset="100%" stopColor="#10b981" stopOpacity={0.5} />
                                    </linearGradient>
                                    <linearGradient id="gradAmber" x1="0" y1="0" x2="0" y2="1">
                                        <stop offset="0%" stopColor="#f59e0b" stopOpacity={1} />
                                        <stop offset="100%" stopColor="#f59e0b" stopOpacity={0.5} />
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
                                    {financialBarData.map((entry, index) => (
                                        <Cell key={index} fill={
                                            index === 0 ? 'url(#gradPurple)' :
                                                index === 1 ? 'url(#gradGreen)' : 'url(#gradAmber)'
                                        } />
                                    ))}
                                </Bar>
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            </div>

            {/* Charts Row 2: User Pie + Loan Counts Bar */}
            <div className="charts-grid">
                <div className="chart-card">
                    <div className="chart-header">
                        <div>
                            <div className="chart-title">👥 User Roles</div>
                            <div className="chart-subtitle">Platform user composition</div>
                        </div>
                    </div>
                    <div className="chart-wrapper">
                        {userPieData.length > 0 ? (
                            <ResponsiveContainer width="100%" height={300}>
                                <PieChart>
                                    <Pie
                                        data={userPieData}
                                        cx="50%"
                                        cy="50%"
                                        outerRadius={100}
                                        paddingAngle={3}
                                        dataKey="value"
                                        labelLine={false}
                                        label={renderCustomLabel}
                                        animationDuration={1200}
                                    >
                                        {userPieData.map((entry, index) => (
                                            <Cell key={index} fill={entry.color} stroke="none" />
                                        ))}
                                    </Pie>
                                    <Tooltip content={<CustomTooltip />} />
                                    <Legend
                                        verticalAlign="bottom"
                                        height={36}
                                        formatter={(value) => <span style={{ color: '#6b7280', fontSize: 13 }}>{value}</span>}
                                    />
                                </PieChart>
                            </ResponsiveContainer>
                        ) : (
                            <p style={{ color: 'var(--text-muted)' }}>No user data</p>
                        )}
                    </div>
                </div>

                <div className="chart-card">
                    <div className="chart-header">
                        <div>
                            <div className="chart-title">📋 Loan Status Counts</div>
                            <div className="chart-subtitle">Number of loans by status</div>
                        </div>
                    </div>
                    <div className="chart-wrapper">
                        <ResponsiveContainer width="100%" height={300}>
                            <BarChart data={loanCountData} barCategoryGap="20%">
                                <CartesianGrid strokeDasharray="3 3" stroke="rgba(0,0,0,0.06)" />
                                <XAxis
                                    dataKey="name"
                                    tick={{ fill: '#6b7280', fontSize: 12 }}
                                    axisLine={{ stroke: 'rgba(0,0,0,0.08)' }}
                                />
                                <YAxis
                                    tick={{ fill: '#6b7280', fontSize: 12 }}
                                    axisLine={{ stroke: 'rgba(0,0,0,0.08)' }}
                                    allowDecimals={false}
                                />
                                <Tooltip content={<CustomTooltip />} />
                                <Bar dataKey="count" name="Loans" radius={[8, 8, 0, 0]} animationDuration={1200}>
                                    {loanCountData.map((entry, index) => (
                                        <Cell key={index} fill={entry.fill} />
                                    ))}
                                </Bar>
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            </div>

            {/* Success Rate Gauge */}
            <div className="charts-grid">
                <div className="chart-card full-width">
                    <div className="chart-header">
                        <div>
                            <div className="chart-title">🎯 Platform Health Score</div>
                            <div className="chart-subtitle">Loan success rate and key performance metrics</div>
                        </div>
                    </div>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px', alignItems: 'center' }}>
                        <div style={{ display: 'flex', justifyContent: 'center' }}>
                            <ResponsiveContainer width="100%" height={250}>
                                <RadialBarChart
                                    cx="50%"
                                    cy="50%"
                                    innerRadius="60%"
                                    outerRadius="90%"
                                    barSize={18}
                                    data={gaugeData}
                                    startAngle={180}
                                    endAngle={0}
                                >
                                    <RadialBar
                                        background={{ fill: 'rgba(0,0,0,0.05)' }}
                                        dataKey="value"
                                        cornerRadius={10}
                                        animationDuration={1500}
                                    />
                                    <text x="50%" y="45%" textAnchor="middle" dominantBaseline="middle"
                                        style={{ fontSize: '32px', fontWeight: 700, fill: gaugeData[0].fill }}>
                                        {successRate.toFixed(1)}%
                                    </text>
                                    <text x="50%" y="58%" textAnchor="middle" dominantBaseline="middle"
                                        style={{ fontSize: '13px', fill: '#6b7280' }}>
                                        Success Rate
                                    </text>
                                </RadialBarChart>
                            </ResponsiveContainer>
                        </div>

                        <div style={{ display: 'grid', gap: '16px' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', padding: '16px', background: 'var(--bg-tertiary)', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
                                <span style={{ color: 'var(--text-secondary)' }}>Loan Success Rate</span>
                                <span style={{ color: defaultedLoans === 0 ? '#10b981' : '#f59e0b', fontWeight: 600 }}>
                                    {successRate.toFixed(1)}%
                                </span>
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'space-between', padding: '16px', background: 'var(--bg-tertiary)', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
                                <span style={{ color: 'var(--text-secondary)' }}>Default Rate</span>
                                <span style={{ color: defaultPercent > 5 ? '#ef4444' : '#10b981', fontWeight: 600 }}>
                                    {defaultPercent}%
                                </span>
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'space-between', padding: '16px', background: 'var(--bg-tertiary)', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
                                <span style={{ color: 'var(--text-secondary)' }}>Borrower-to-Lender Ratio</span>
                                <span style={{ color: 'var(--text-primary)', fontWeight: 600 }}>
                                    {totalLenders ? (totalBorrowers / totalLenders).toFixed(2) : 'N/A'} : 1
                                </span>
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'space-between', padding: '16px', background: 'var(--bg-tertiary)', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
                                <span style={{ color: 'var(--text-secondary)' }}>Avg Loan Per Lender</span>
                                <span style={{ color: 'var(--text-primary)', fontWeight: 600 }}>
                                    {formatCurrency(totalLenders ? (dashboard?.totalLoanAmount || 0) / totalLenders : 0)}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminAnalytics;
