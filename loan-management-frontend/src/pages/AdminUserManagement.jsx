import { useState, useEffect } from 'react';
import { adminAPI } from '../services/api';

const AdminUserManagement = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [selectedUser, setSelectedUser] = useState(null);
    const [filter, setFilter] = useState('ALL');
    const [showCreditModal, setShowCreditModal] = useState(false);
    const [creditForm, setCreditForm] = useState({
        creditScore: '',
        annualIncome: '',
        employmentStatus: ''
    });

    useEffect(() => {
        fetchUsers();
    }, [filter]);

    const fetchUsers = async () => {
        try {
            setLoading(true);
            let response;
            if (filter === 'ALL') {
                response = await adminAPI.getUsers();
            } else {
                response = await adminAPI.getUsersByRole(filter);
            }
            setUsers(response.data.data || []);
        } catch (err) {
            setError('Failed to load users');
        } finally {
            setLoading(false);
        }
    };

    const handleViewCreditScore = async (user) => {
        try {
            const response = await adminAPI.getCreditScore(user.id);
            setSelectedUser({ ...user, creditDetails: response.data.data });
            setCreditForm({
                creditScore: user.creditScore || '',
                annualIncome: user.annualIncome || '',
                employmentStatus: user.employmentStatus || ''
            });
            setShowCreditModal(true);
        } catch (err) {
            alert('Failed to fetch credit details');
        }
    };

    const handleUpdateCreditScore = async () => {
        try {
            await adminAPI.updateCreditScore(selectedUser.id, {
                creditScore: parseInt(creditForm.creditScore) || null,
                annualIncome: parseFloat(creditForm.annualIncome) || null,
                employmentStatus: creditForm.employmentStatus || null
            });
            setShowCreditModal(false);
            fetchUsers();
            alert('Credit score updated successfully!');
        } catch (err) {
            alert('Failed to update credit score');
        }
    };

    const handleCalculateRisk = async (userId) => {
        try {
            const response = await adminAPI.calculateRisk(userId);
            const data = response.data.data;
            alert(`Risk Score: ${data.riskScore}\nRisk Level: ${data.riskLevel}\n${data.recommendation}`);
            fetchUsers();
        } catch (err) {
            alert('Failed to calculate risk: ' + (err.response?.data?.message || err.message));
        }
    };

    const getRiskBadgeColor = (level) => {
        switch (level) {
            case 'LOW': return '#10b981';
            case 'MEDIUM': return '#f59e0b';
            case 'HIGH': return '#ef4444';
            case 'CRITICAL': return '#7f1d1d';
            default: return '#6b7280';
        }
    };

    const getCreditScoreColor = (score) => {
        if (!score) return '#6b7280';
        if (score >= 750) return '#10b981';
        if (score >= 650) return '#f59e0b';
        if (score >= 550) return '#f97316';
        return '#ef4444';
    };

    if (loading) {
        return (
            <div className="loading-container">
                <div className="loading-spinner"></div>
                <p>Loading users...</p>
            </div>
        );
    }

    return (
        <div className="fade-in">
            <div className="page-header">
                <h1 className="page-title">👥 User Management</h1>
                <p className="page-subtitle">Manage all users and credit scores</p>
            </div>

            {error && <div className="alert alert-error">{error}</div>}

            {/* Filters */}
            <div className="card" style={{ marginBottom: '24px' }}>
                <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
                    {['ALL', 'ADMIN', 'LENDER', 'BORROWER', 'ANALYST'].map(role => (
                        <button
                            key={role}
                            className={`btn ${filter === role ? 'btn-primary' : 'btn-secondary'}`}
                            onClick={() => setFilter(role)}
                        >
                            {role}
                        </button>
                    ))}
                </div>
            </div>

            {/* Users Table */}
            <div className="card">
                <div className="card-header">
                    <h3 className="card-title">Users ({users.length})</h3>
                </div>
                <div style={{ overflowX: 'auto' }}>
                    <table className="table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Name</th>
                                <th>Email</th>
                                <th>Phone</th>
                                <th>Role</th>
                                <th>Credit Score</th>
                                <th>Risk Level</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {users.map(user => (
                                <tr key={user.id}>
                                    <td>{user.id}</td>
                                    <td>
                                        <strong>{user.fullName || `${user.firstName} ${user.lastName}`}</strong>
                                    </td>
                                    <td>{user.email}</td>
                                    <td>{user.phoneNumber || '-'}</td>
                                    <td>
                                        <span className={`badge badge-${user.role?.toLowerCase()}`}>
                                            {user.role}
                                        </span>
                                    </td>
                                    <td>
                                        {user.creditScore ? (
                                            <span style={{
                                                fontWeight: 'bold',
                                                color: getCreditScoreColor(user.creditScore)
                                            }}>
                                                {user.creditScore}
                                            </span>
                                        ) : (
                                            <span style={{ color: '#6b7280' }}>Not set</span>
                                        )}
                                    </td>
                                    <td>
                                        {user.riskLevel ? (
                                            <span style={{
                                                padding: '4px 8px',
                                                borderRadius: '4px',
                                                fontSize: '12px',
                                                fontWeight: 'bold',
                                                backgroundColor: getRiskBadgeColor(user.riskLevel),
                                                color: 'white'
                                            }}>
                                                {user.riskLevel}
                                            </span>
                                        ) : '-'}
                                    </td>
                                    <td>
                                        <span className={`badge ${user.enabled ? 'badge-success' : 'badge-error'}`}>
                                            {user.enabled ? 'Active' : 'Disabled'}
                                        </span>
                                    </td>
                                    <td>
                                        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                                            <button
                                                className="btn btn-small btn-primary"
                                                onClick={() => handleViewCreditScore(user)}
                                                title="View/Edit Credit Score"
                                            >
                                                💳 Credit
                                            </button>
                                            {user.role === 'BORROWER' && (
                                                <button
                                                    className="btn btn-small btn-secondary"
                                                    onClick={() => handleCalculateRisk(user.id)}
                                                    title="Calculate Risk Score"
                                                >
                                                    📊 Risk
                                                </button>
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Credit Score Modal */}
            {showCreditModal && selectedUser && (
                <div className="modal-overlay" onClick={() => setShowCreditModal(false)}>
                    <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '500px' }}>
                        <div className="modal-header">
                            <h2>💳 Credit Score Management</h2>
                            <button className="modal-close" onClick={() => setShowCreditModal(false)}>×</button>
                        </div>
                        <div className="modal-body">
                            <div style={{ marginBottom: '20px' }}>
                                <strong>User:</strong> {selectedUser.fullName || `${selectedUser.firstName} ${selectedUser.lastName}`}<br />
                                <strong>Email:</strong> {selectedUser.email}<br />
                                <strong>Role:</strong> {selectedUser.role}
                            </div>

                            {selectedUser.creditDetails?.calculatedRiskScore && (
                                <div className="alert" style={{
                                    padding: '12px',
                                    marginBottom: '16px',
                                    backgroundColor: 'rgba(59, 130, 246, 0.1)',
                                    border: '1px solid #3b82f6',
                                    borderRadius: '8px'
                                }}>
                                    <strong>System Calculated Risk:</strong><br />
                                    Score: {selectedUser.creditDetails.calculatedRiskScore}<br />
                                    Level: {selectedUser.creditDetails.calculatedRiskLevel}<br />
                                    <em>{selectedUser.creditDetails.riskRecommendation}</em>
                                </div>
                            )}

                            <div className="form-group">
                                <label>Credit Score (300-850)</label>
                                <input
                                    type="number"
                                    className="form-control"
                                    value={creditForm.creditScore}
                                    onChange={e => setCreditForm({ ...creditForm, creditScore: e.target.value })}
                                    min="300"
                                    max="850"
                                    placeholder="Enter credit score (300-850)"
                                />
                            </div>

                            <div className="form-group">
                                <label>Annual Income ($)</label>
                                <input
                                    type="number"
                                    className="form-control"
                                    value={creditForm.annualIncome}
                                    onChange={e => setCreditForm({ ...creditForm, annualIncome: e.target.value })}
                                    placeholder="Enter annual income"
                                />
                            </div>

                            <div className="form-group">
                                <label>Employment Status</label>
                                <select
                                    className="form-control"
                                    value={creditForm.employmentStatus}
                                    onChange={e => setCreditForm({ ...creditForm, employmentStatus: e.target.value })}
                                >
                                    <option value="">Select status</option>
                                    <option value="EMPLOYED">Employed</option>
                                    <option value="SELF_EMPLOYED">Self Employed</option>
                                    <option value="UNEMPLOYED">Unemployed</option>
                                    <option value="RETIRED">Retired</option>
                                    <option value="STUDENT">Student</option>
                                </select>
                            </div>

                            <div style={{ display: 'flex', gap: '12px', marginTop: '20px' }}>
                                <button className="btn btn-primary" onClick={handleUpdateCreditScore}>
                                    Save Changes
                                </button>
                                <button className="btn btn-secondary" onClick={() => setShowCreditModal(false)}>
                                    Cancel
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            <style>{`
                .table {
                    width: 100%;
                    border-collapse: collapse;
                }
                .table th, .table td {
                    padding: 12px;
                    text-align: left;
                    border-bottom: 1px solid rgba(255,255,255,0.1);
                }
                .table th {
                    font-weight: 600;
                    color: #9ca3af;
                    font-size: 12px;
                    text-transform: uppercase;
                }
                .btn-small {
                    padding: 6px 12px !important;
                    font-size: 12px !important;
                }
                .badge {
                    padding: 4px 8px;
                    border-radius: 4px;
                    font-size: 12px;
                    font-weight: 600;
                }
                .badge-admin { background: #8b5cf6; color: white; }
                .badge-lender { background: #10b981; color: white; }
                .badge-borrower { background: #3b82f6; color: white; }
                .badge-analyst { background: #f59e0b; color: white; }
                .badge-success { background: #10b981; color: white; }
                .badge-error { background: #ef4444; color: white; }
                .modal-overlay {
                    position: fixed;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0;
                    background: rgba(0,0,0,0.7);
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    z-index: 1000;
                }
                .modal {
                    background: #1f2937;
                    border-radius: 12px;
                    width: 90%;
                    max-height: 90vh;
                    overflow-y: auto;
                }
                .modal-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    padding: 20px;
                    border-bottom: 1px solid rgba(255,255,255,0.1);
                }
                .modal-header h2 {
                    margin: 0;
                    font-size: 18px;
                }
                .modal-close {
                    background: none;
                    border: none;
                    color: white;
                    font-size: 24px;
                    cursor: pointer;
                }
                .modal-body {
                    padding: 20px;
                }
                .form-group {
                    margin-bottom: 16px;
                }
                .form-group label {
                    display: block;
                    margin-bottom: 6px;
                    font-weight: 500;
                }
                .form-control {
                    width: 100%;
                    padding: 10px;
                    border-radius: 8px;
                    border: 1px solid rgba(255,255,255,0.2);
                    background: rgba(255,255,255,0.05);
                    color: white;
                    font-size: 14px;
                }
            `}</style>
        </div>
    );
};

export default AdminUserManagement;
