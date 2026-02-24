import { useState, useEffect } from 'react';
import { analystAPI } from '../services/api';
import {
    BarChart, Bar, XAxis, YAxis, CartesianGrid,
    Tooltip, ResponsiveContainer, Cell, RadialBarChart, RadialBar,
    PieChart, Pie
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

const AnalystRisk = () => {
    const [risk, setRisk] = useState(null);
    const [overduePayments, setOverduePayments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            const [riskRes, overdueRes] = await Promise.all([
                analystAPI.getRiskAssessment(),
                analystAPI.getOverduePayments(),
            ]);
            setRisk(riskRes.data.data);
            setOverduePayments(overdueRes.data.data || []);
        } catch (err) {
            setError('Failed to load risk data');
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
                <p>Loading risk assessment...</p>
            </div>
        );
    }

    if (error) {
        return <div className="alert alert-error">{error}</div>;
    }

    const riskScore = risk?.portfolioRiskScore || 0;
    const riskColor = riskScore > 50 ? '#ef4444' : riskScore > 20 ? '#f59e0b' : '#10b981';
    const riskLabel = riskScore > 50 ? 'Critical' : riskScore > 20 ? 'Moderate' : 'Low';

    // Risk gauge data
    const gaugeData = [
        { name: 'Risk Score', value: Math.min(riskScore, 100), fill: riskColor },
    ];

    // Health breakdown donut
    const healthData = [
        { name: 'Healthy', value: Math.max(100 - riskScore, 0), color: '#10b981' },
        { name: 'At Risk', value: Math.min(riskScore, 100), color: riskColor },
    ];

    // Risk summary bars
    const riskMetricsData = [
        { name: 'Overdue Payments', value: risk?.overduePaymentsCount || 0, fill: '#ef4444' },
        { name: 'Loans at Risk', value: risk?.loansAtRisk || 0, fill: '#f59e0b' },
        { name: 'Risk Score', value: riskScore, fill: riskColor },
    ];

    // Overdue payment amounts chart (top 10)
    const overdueChartData = overduePayments.slice(0, 10).map((payment, idx) => ({
        name: `#${payment.id || idx + 1}`,
        amount: payment.amount || 0,
        fill: '#ef4444',
    }));

    return (
        <div className="fade-in">
            <div className="page-header">
                <h1 className="page-title">⚠️ Risk Assessment</h1>
                <p className="page-subtitle">Portfolio risk analysis and overdue tracking</p>
            </div>

            {/* Risk Score Overview Cards */}
            <div className="stats-grid">
                <div className="stat-card" style={{ borderLeft: `4px solid ${riskColor}` }}>
                    <div className="stat-label">Portfolio Risk Score</div>
                    <div className="stat-value" style={{ color: riskColor }}>{riskScore.toFixed(1)}</div>
                    <div className={`stat-change ${riskScore > 20 ? 'negative' : 'positive'}`}>
                        {riskLabel} risk
                    </div>
                </div>
                <div className="stat-card" style={{ borderLeft: '4px solid #ef4444' }}>
                    <div className="stat-label">Overdue Payments</div>
                    <div className="stat-value">{risk?.overduePaymentsCount || 0}</div>
                    <div className="stat-change negative">Require attention</div>
                </div>
                <div className="stat-card" style={{ borderLeft: '4px solid #f59e0b' }}>
                    <div className="stat-label">Total Overdue Amount</div>
                    <div className="stat-value">{formatCurrency(risk?.totalOverdueAmount)}</div>
                </div>
                <div className="stat-card" style={{ borderLeft: '4px solid #8b5cf6' }}>
                    <div className="stat-label">Loans at Risk</div>
                    <div className="stat-value">{risk?.loansAtRisk || 0}</div>
                    <div className="stat-change negative">With missed payments</div>
                </div>
            </div>

            {/* Charts Row: Risk Gauge + Health Donut */}
            <div className="charts-grid">
                <div className="chart-card">
                    <div className="chart-header">
                        <div>
                            <div className="chart-title">🎯 Risk Score Gauge</div>
                            <div className="chart-subtitle">Portfolio risk level indicator</div>
                        </div>
                    </div>
                    <div className="chart-wrapper" style={{ flexDirection: 'column' }}>
                        <ResponsiveContainer width="100%" height={230}>
                            <RadialBarChart
                                cx="50%"
                                cy="50%"
                                innerRadius="55%"
                                outerRadius="90%"
                                barSize={22}
                                data={gaugeData}
                                startAngle={180}
                                endAngle={0}
                            >
                                <RadialBar
                                    background={{ fill: 'rgba(0,0,0,0.05)' }}
                                    dataKey="value"
                                    cornerRadius={12}
                                    animationDuration={1500}
                                />
                                <text x="50%" y="42%" textAnchor="middle" dominantBaseline="middle"
                                    style={{ fontSize: '40px', fontWeight: 700, fill: riskColor }}>
                                    {riskScore.toFixed(1)}
                                </text>
                                <text x="50%" y="58%" textAnchor="middle" dominantBaseline="middle"
                                    style={{ fontSize: '14px', fill: '#6b7280' }}>
                                    / 100
                                </text>
                            </RadialBarChart>
                        </ResponsiveContainer>
                        <div style={{
                            display: 'flex', gap: '20px', justifyContent: 'center',
                            marginTop: '4px', fontSize: '12px'
                        }}>
                            <span style={{ color: '#10b981', fontWeight: 600 }}>● Low (0-20)</span>
                            <span style={{ color: '#f59e0b', fontWeight: 600 }}>● Moderate (20-50)</span>
                            <span style={{ color: '#ef4444', fontWeight: 600 }}>● High (50+)</span>
                        </div>
                    </div>
                </div>

                <div className="chart-card">
                    <div className="chart-header">
                        <div>
                            <div className="chart-title">💚 Portfolio Health</div>
                            <div className="chart-subtitle">Healthy vs at-risk proportion</div>
                        </div>
                    </div>
                    <div className="chart-wrapper">
                        <ResponsiveContainer width="100%" height={280}>
                            <PieChart>
                                <Pie
                                    data={healthData}
                                    cx="50%"
                                    cy="50%"
                                    innerRadius={65}
                                    outerRadius={100}
                                    paddingAngle={4}
                                    dataKey="value"
                                    animationDuration={1200}
                                >
                                    {healthData.map((entry, index) => (
                                        <Cell key={index} fill={entry.color} stroke="none" />
                                    ))}
                                </Pie>
                                <Tooltip content={<CustomTooltip />} />
                                <text x="50%" y="46%" textAnchor="middle" dominantBaseline="middle"
                                    style={{ fontSize: '26px', fontWeight: 700, fill: '#10b981' }}>
                                    {(100 - riskScore).toFixed(0)}%
                                </text>
                                <text x="50%" y="56%" textAnchor="middle" dominantBaseline="middle"
                                    style={{ fontSize: '12px', fill: '#6b7280' }}>
                                    Healthy
                                </text>
                            </PieChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            </div>

            {/* Risk Metrics Horizontal Bar */}
            <div className="charts-grid">
                <div className="chart-card">
                    <div className="chart-header">
                        <div>
                            <div className="chart-title">📊 Risk Metrics Overview</div>
                            <div className="chart-subtitle">Key risk indicators at a glance</div>
                        </div>
                    </div>
                    <div className="chart-wrapper">
                        <ResponsiveContainer width="100%" height={260}>
                            <BarChart data={riskMetricsData} layout="vertical" barCategoryGap="25%">
                                <CartesianGrid strokeDasharray="3 3" stroke="rgba(0,0,0,0.06)" />
                                <XAxis
                                    type="number"
                                    tick={{ fill: '#6b7280', fontSize: 12 }}
                                    axisLine={{ stroke: 'rgba(0,0,0,0.08)' }}
                                />
                                <YAxis
                                    type="category"
                                    dataKey="name"
                                    tick={{ fill: '#6b7280', fontSize: 12 }}
                                    axisLine={{ stroke: 'rgba(0,0,0,0.08)' }}
                                    width={120}
                                />
                                <Tooltip content={<CustomTooltip />} />
                                <Bar dataKey="value" name="Value" radius={[0, 8, 8, 0]} animationDuration={1200}>
                                    {riskMetricsData.map((entry, index) => (
                                        <Cell key={index} fill={entry.fill} />
                                    ))}
                                </Bar>
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                {/* Overdue Amounts Bar */}
                <div className="chart-card">
                    <div className="chart-header">
                        <div>
                            <div className="chart-title">💸 Overdue Payment Amounts</div>
                            <div className="chart-subtitle">
                                {overdueChartData.length > 0
                                    ? `Top ${overdueChartData.length} overdue payments`
                                    : 'No overdue payments'}
                            </div>
                        </div>
                    </div>
                    <div className="chart-wrapper">
                        {overdueChartData.length > 0 ? (
                            <ResponsiveContainer width="100%" height={260}>
                                <BarChart data={overdueChartData} barCategoryGap="15%">
                                    <defs>
                                        <linearGradient id="riskGradRed" x1="0" y1="0" x2="0" y2="1">
                                            <stop offset="0%" stopColor="#ef4444" stopOpacity={1} />
                                            <stop offset="100%" stopColor="#ef4444" stopOpacity={0.4} />
                                        </linearGradient>
                                    </defs>
                                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(0,0,0,0.06)" />
                                    <XAxis
                                        dataKey="name"
                                        tick={{ fill: '#6b7280', fontSize: 11 }}
                                        axisLine={{ stroke: 'rgba(0,0,0,0.08)' }}
                                    />
                                    <YAxis
                                        tick={{ fill: '#6b7280', fontSize: 12 }}
                                        axisLine={{ stroke: 'rgba(0,0,0,0.08)' }}
                                        tickFormatter={(v) => v >= 1000 ? `₹${(v / 1000).toFixed(0)}k` : `₹${v}`}
                                    />
                                    <Tooltip content={<CustomTooltip />} />
                                    <Bar
                                        dataKey="amount"
                                        name="Amount"
                                        fill="url(#riskGradRed)"
                                        radius={[8, 8, 0, 0]}
                                        animationDuration={1200}
                                    />
                                </BarChart>
                            </ResponsiveContainer>
                        ) : (
                            <div style={{ textAlign: 'center', padding: '32px', color: '#6b7280' }}>
                                <div style={{ fontSize: '48px', marginBottom: '12px' }}>✅</div>
                                <p>No overdue payments found</p>
                                <p style={{ fontSize: '13px', marginTop: '4px' }}>Portfolio is in good standing</p>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* Overdue Payments Table */}
            <div className="card" style={{ marginTop: '24px' }}>
                <div className="card-header">
                    <h3 className="card-title">🔴 Overdue Payments ({overduePayments.length})</h3>
                </div>
                {overduePayments.length === 0 ? (
                    <div style={{ textAlign: 'center', padding: '32px', color: '#6b7280' }}>
                        ✅ No overdue payments found. Portfolio is in good standing.
                    </div>
                ) : (
                    <div style={{ overflowX: 'auto' }}>
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>Payment ID</th>
                                    <th>Amount</th>
                                    <th>Due Date</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                {overduePayments.slice(0, 20).map((payment, idx) => (
                                    <tr key={idx}>
                                        <td>#{payment.id || idx + 1}</td>
                                        <td>{formatCurrency(payment.amount)}</td>
                                        <td>{payment.dueDate || 'N/A'}</td>
                                        <td><span style={{ color: '#ef4444', fontWeight: '600' }}>OVERDUE</span></td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
};

export default AnalystRisk;
