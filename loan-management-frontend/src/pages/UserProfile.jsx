import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { profileAPI } from '../services/api';

const UserProfile = () => {
    const { user, logout } = useAuth();
    const [activeTab, setActiveTab] = useState('profile');
    const [isEditing, setIsEditing] = useState(false);
    const [saving, setSaving] = useState(false);
    const [saveMessage, setSaveMessage] = useState('');
    const [editForm, setEditForm] = useState({
        firstName: user?.firstName || '',
        lastName: user?.lastName || '',
        email: user?.email || '',
        phoneNumber: user?.phoneNumber || '',
    });
    const [showContactForm, setShowContactForm] = useState(false);
    const [contactMessage, setContactMessage] = useState('');
    const [submitted, setSubmitted] = useState(false);

    const handleEditToggle = () => {
        if (isEditing) {
            // Cancel editing, reset form
            setEditForm({
                firstName: user?.firstName || '',
                lastName: user?.lastName || '',
                email: user?.email || '',
                phoneNumber: user?.phoneNumber || '',
            });
        }
        setIsEditing(!isEditing);
        setSaveMessage('');
    };

    const handleInputChange = (e) => {
        setEditForm({ ...editForm, [e.target.name]: e.target.value });
    };

    const handleSaveProfile = async (e) => {
        e.preventDefault();
        setSaving(true);
        setSaveMessage('');
        try {
            const response = await profileAPI.updateProfile(editForm);
            const updatedData = response.data.data;
            // Update localStorage user data
            const storedUser = JSON.parse(localStorage.getItem('user') || '{}');
            const newUser = { ...storedUser, ...updatedData };
            localStorage.setItem('user', JSON.stringify(newUser));
            setSaveMessage('✅ Profile updated successfully!');
            setIsEditing(false);
            // Reload to reflect changes
            setTimeout(() => window.location.reload(), 1000);
        } catch (err) {
            setSaveMessage('❌ Failed to update profile. ' + (err.response?.data?.message || ''));
        } finally {
            setSaving(false);
        }
    };

    const handleContactSubmit = (e) => {
        e.preventDefault();
        setSubmitted(true);
        setTimeout(() => {
            setShowContactForm(false);
            setContactMessage('');
            setSubmitted(false);
        }, 2000);
    };

    // Generate a display user ID
    const displayUserId = (user?.userId || user?.id) ? `USR-${String(user?.userId || user?.id).padStart(6, '0')}` : 'N/A';

    return (
        <div className="fade-in">
            <div className="page-header">
                <h1 className="page-title">👤 My Profile</h1>
                <p className="page-subtitle">Manage your account settings</p>
            </div>

            {/* Profile Card */}
            <div className="profile-card">
                <div className="profile-avatar">
                    {user?.firstName?.charAt(0)}{user?.lastName?.charAt(0)}
                </div>
                <div className="profile-info">
                    <h2>{user?.firstName} {user?.lastName}</h2>
                    <span className="role-badge">{user?.role}</span>
                    <span className="user-id-badge">{displayUserId}</span>
                </div>
            </div>

            {/* Tabs */}
            <div className="profile-tabs">
                <button
                    className={`tab-btn ${activeTab === 'profile' ? 'active' : ''}`}
                    onClick={() => setActiveTab('profile')}
                >
                    📋 Profile Info
                </button>
                <button
                    className={`tab-btn ${activeTab === 'help' ? 'active' : ''}`}
                    onClick={() => setActiveTab('help')}
                >
                    ❓ Help
                </button>
                <button
                    className={`tab-btn ${activeTab === 'contact' ? 'active' : ''}`}
                    onClick={() => setActiveTab('contact')}
                >
                    📧 Contact Us
                </button>
            </div>

            {/* Profile Info Tab */}
            {activeTab === 'profile' && (
                <div className="card">
                    <div className="card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <h3 className="card-title">Personal Information</h3>
                        <button
                            className={`btn ${isEditing ? 'btn-secondary' : 'btn-primary'}`}
                            onClick={handleEditToggle}
                            style={{ fontSize: '13px', padding: '8px 16px' }}
                        >
                            {isEditing ? '✖ Cancel' : '✏️ Edit Profile'}
                        </button>
                    </div>

                    {saveMessage && (
                        <div style={{
                            padding: '12px 16px',
                            marginBottom: '16px',
                            borderRadius: '8px',
                            background: saveMessage.startsWith('✅') ? 'rgba(16,185,129,0.1)' : 'rgba(239,68,68,0.1)',
                            border: `1px solid ${saveMessage.startsWith('✅') ? '#10b981' : '#ef4444'}`,
                            color: saveMessage.startsWith('✅') ? '#10b981' : '#ef4444',
                            fontSize: '14px',
                        }}>
                            {saveMessage}
                        </div>
                    )}

                    {isEditing ? (
                        <form onSubmit={handleSaveProfile}>
                            <div className="profile-details">
                                <div className="detail-row edit-row">
                                    <span className="detail-label">📧 Email</span>
                                    <input
                                        type="email"
                                        name="email"
                                        value={editForm.email}
                                        onChange={handleInputChange}
                                        className="profile-edit-input"
                                        required
                                    />
                                </div>
                                <div className="detail-row edit-row">
                                    <span className="detail-label">👤 First Name</span>
                                    <input
                                        type="text"
                                        name="firstName"
                                        value={editForm.firstName}
                                        onChange={handleInputChange}
                                        className="profile-edit-input"
                                        required
                                    />
                                </div>
                                <div className="detail-row edit-row">
                                    <span className="detail-label">👤 Last Name</span>
                                    <input
                                        type="text"
                                        name="lastName"
                                        value={editForm.lastName}
                                        onChange={handleInputChange}
                                        className="profile-edit-input"
                                        required
                                    />
                                </div>
                                <div className="detail-row edit-row">
                                    <span className="detail-label">📱 Phone Number</span>
                                    <input
                                        type="tel"
                                        name="phoneNumber"
                                        value={editForm.phoneNumber}
                                        onChange={handleInputChange}
                                        className="profile-edit-input"
                                        placeholder="Enter phone number"
                                    />
                                </div>
                                <div className="detail-row">
                                    <span className="detail-label">🎭 Role</span>
                                    <span className="detail-value" style={{ opacity: 0.6 }}>{user?.role} (cannot be changed)</span>
                                </div>
                                <div className="detail-row">
                                    <span className="detail-label">🆔 User ID</span>
                                    <span className="detail-value">{displayUserId}</span>
                                </div>
                            </div>
                            <div style={{ marginTop: '24px', display: 'flex', gap: '12px' }}>
                                <button type="submit" className="btn btn-primary" disabled={saving}>
                                    {saving ? '💾 Saving...' : '💾 Save Changes'}
                                </button>
                                <button type="button" className="btn btn-secondary" onClick={handleEditToggle}>
                                    Cancel
                                </button>
                            </div>
                        </form>
                    ) : (
                        <>
                            <div className="profile-details">
                                <div className="detail-row">
                                    <span className="detail-label">📧 Email</span>
                                    <span className="detail-value">{user?.email}</span>
                                </div>
                                <div className="detail-row">
                                    <span className="detail-label">👤 First Name</span>
                                    <span className="detail-value">{user?.firstName || 'Not set'}</span>
                                </div>
                                <div className="detail-row">
                                    <span className="detail-label">👤 Last Name</span>
                                    <span className="detail-value">{user?.lastName || 'Not set'}</span>
                                </div>
                                <div className="detail-row">
                                    <span className="detail-label">📱 Phone Number</span>
                                    <span className="detail-value">{user?.phoneNumber || 'Not set'}</span>
                                </div>
                                <div className="detail-row">
                                    <span className="detail-label">🎭 Role</span>
                                    <span className="detail-value">{user?.role}</span>
                                </div>
                                <div className="detail-row">
                                    <span className="detail-label">🆔 User ID</span>
                                    <span className="detail-value">{displayUserId}</span>
                                </div>
                            </div>
                            <div style={{ marginTop: '24px' }}>
                                <button className="btn btn-danger" onClick={logout}>
                                    🚪 Logout
                                </button>
                            </div>
                        </>
                    )}
                </div>
            )}

            {/* Help Tab */}
            {activeTab === 'help' && (
                <div className="card">
                    <div className="card-header">
                        <h3 className="card-title">Help & FAQs</h3>
                    </div>
                    <div className="help-section">
                        <div className="faq-item">
                            <h4>🔐 How do I reset my password?</h4>
                            <p>Contact support at support@12club.com to reset your password.</p>
                        </div>
                        <div className="faq-item">
                            <h4>💰 How do I apply for a loan?</h4>
                            <p>Navigate to "Loan Offers" from the dashboard and select an available loan to apply.</p>
                        </div>
                        <div className="faq-item">
                            <h4>📅 How do I make a payment?</h4>
                            <p>Go to "My Payments" section and click on "Make Payment" for any pending EMI.</p>
                        </div>
                        <div className="faq-item">
                            <h4>📊 What affects my credit score?</h4>
                            <p>Your credit score is affected by payment history, loan amounts, and repayment patterns.</p>
                        </div>
                        <div className="faq-item">
                            <h4>🔔 How do I enable notifications?</h4>
                            <p>Notifications are enabled by default. Check your notification bell icon in the dashboard.</p>
                        </div>
                    </div>
                </div>
            )}

            {/* Contact Us Tab */}
            {activeTab === 'contact' && (
                <div className="card">
                    <div className="card-header">
                        <h3 className="card-title">Contact Us</h3>
                    </div>
                    <div className="contact-section">
                        <div className="contact-info">
                            <div className="contact-item">
                                <span className="contact-icon">📧</span>
                                <div>
                                    <strong>Email Support</strong>
                                    <p>support@12club.com</p>
                                </div>
                            </div>
                            <div className="contact-item">
                                <span className="contact-icon">📞</span>
                                <div>
                                    <strong>Phone Support</strong>
                                    <p>+91 1800-123-4567</p>
                                </div>
                            </div>
                            <div className="contact-item">
                                <span className="contact-icon">⏰</span>
                                <div>
                                    <strong>Working Hours</strong>
                                    <p>Mon - Fri: 9:00 AM - 6:00 PM</p>
                                </div>
                            </div>
                            <div className="contact-item">
                                <span className="contact-icon">📍</span>
                                <div>
                                    <strong>Address</strong>
                                    <p>123 Finance Street, Hyderabad, India</p>
                                </div>
                            </div>
                        </div>

                        <div className="contact-form-section">
                            <h4>Send us a message</h4>
                            {submitted ? (
                                <div className="success-message">
                                    ✅ Message sent successfully! We'll get back to you soon.
                                </div>
                            ) : (
                                <form onSubmit={handleContactSubmit}>
                                    <div className="form-group">
                                        <label>Subject</label>
                                        <select className="form-control">
                                            <option>General Inquiry</option>
                                            <option>Loan Application Help</option>
                                            <option>Payment Issue</option>
                                            <option>Technical Support</option>
                                            <option>Other</option>
                                        </select>
                                    </div>
                                    <div className="form-group">
                                        <label>Message</label>
                                        <textarea
                                            className="form-control"
                                            rows="4"
                                            value={contactMessage}
                                            onChange={(e) => setContactMessage(e.target.value)}
                                            placeholder="Describe your issue or question..."
                                            required
                                        />
                                    </div>
                                    <button type="submit" className="btn btn-primary">
                                        📤 Send Message
                                    </button>
                                </form>
                            )}
                        </div>
                    </div>
                </div>
            )}

            <style>{`
                .profile-card {
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    border-radius: 16px;
                    padding: 32px;
                    display: flex;
                    align-items: center;
                    gap: 24px;
                    margin-bottom: 24px;
                }
                .profile-avatar {
                    width: 80px;
                    height: 80px;
                    border-radius: 50%;
                    background: rgba(255,255,255,0.2);
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-size: 28px;
                    font-weight: bold;
                    color: white;
                    border: 3px solid rgba(255,255,255,0.3);
                }
                .profile-info h2 {
                    color: white;
                    margin: 0 0 8px 0;
                    font-size: 24px;
                }
                .role-badge {
                    background: rgba(255,255,255,0.2);
                    padding: 4px 12px;
                    border-radius: 20px;
                    font-size: 12px;
                    color: white;
                    text-transform: uppercase;
                }
                .user-id-badge {
                    background: rgba(255,255,255,0.15);
                    padding: 4px 12px;
                    border-radius: 20px;
                    font-size: 12px;
                    color: rgba(255,255,255,0.8);
                    margin-left: 8px;
                    font-family: monospace;
                    letter-spacing: 1px;
                }
                .profile-tabs {
                    display: flex;
                    gap: 12px;
                    margin-bottom: 24px;
                    flex-wrap: wrap;
                }
                .tab-btn {
                    padding: 12px 24px;
                    border: none;
                    border-radius: 8px;
                    background: var(--bg-tertiary);
                    color: #4b5563;
                    cursor: pointer;
                    transition: all 0.3s;
                    font-size: 14px;
                    border: 1px solid var(--border-color);
                }
                .tab-btn:hover {
                    background: var(--bg-glass);
                    border-color: var(--primary);
                }
                .tab-btn.active {
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    color: white;
                    border-color: transparent;
                }
                .profile-details {
                    display: grid;
                    gap: 16px;
                }
                .detail-row {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    padding: 16px;
                    background: var(--bg-tertiary);
                    border-radius: 8px;
                    border: 1px solid var(--border-color);
                }
                .edit-row {
                    border: 1px solid rgba(102,126,234,0.3);
                }
                .detail-label {
                    color: #6b7280;
                    font-weight: 500;
                    min-width: 140px;
                }
                .detail-value {
                    color: #1d1d1f;
                    font-weight: 600;
                }
                .profile-edit-input {
                    flex: 1;
                    max-width: 300px;
                    padding: 10px 14px;
                    border-radius: 8px;
                    border: 1px solid var(--border-color);
                    background: var(--bg-secondary);
                    color: #1d1d1f;
                    font-size: 14px;
                    transition: border-color 0.2s;
                }
                .profile-edit-input:focus {
                    outline: none;
                    border-color: #667eea;
                    box-shadow: 0 0 0 3px rgba(102,126,234,0.15);
                }
                .help-section {
                    display: grid;
                    gap: 16px;
                }
                .faq-item {
                    padding: 20px;
                    background: var(--bg-tertiary);
                    border-radius: 8px;
                    border-left: 3px solid #667eea;
                }
                .faq-item h4 {
                    margin: 0 0 8px 0;
                    color: #1d1d1f;
                }
                .faq-item p {
                    margin: 0;
                    color: #6b7280;
                }
                .contact-section {
                    display: grid;
                    gap: 32px;
                }
                .contact-info {
                    display: grid;
                    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
                    gap: 16px;
                }
                .contact-item {
                    display: flex;
                    align-items: flex-start;
                    gap: 16px;
                    padding: 20px;
                    background: var(--bg-tertiary);
                    border-radius: 8px;
                }
                .contact-icon {
                    font-size: 24px;
                }
                .contact-item strong {
                    display: block;
                    color: #1d1d1f;
                    margin-bottom: 4px;
                }
                .contact-item p {
                    margin: 0;
                    color: #6b7280;
                }
                .contact-form-section {
                    padding-top: 24px;
                    border-top: 1px solid var(--border-color);
                }
                .contact-form-section h4 {
                    margin: 0 0 16px 0;
                    color: #1d1d1f;
                }
                .success-message {
                    padding: 20px;
                    background: rgba(16, 185, 129, 0.1);
                    border: 1px solid #10b981;
                    border-radius: 8px;
                    color: #10b981;
                    text-align: center;
                }
                .form-group {
                    margin-bottom: 16px;
                }
                .form-group label {
                    display: block;
                    margin-bottom: 6px;
                    color: #4b5563;
                    font-weight: 500;
                }
                .form-control {
                    width: 100%;
                    padding: 12px;
                    border-radius: 8px;
                    border: 1px solid var(--border-color);
                    background: var(--bg-secondary);
                    color: #1d1d1f;
                    font-size: 14px;
                }
                .form-control:focus {
                    outline: none;
                    border-color: #667eea;
                }
                textarea.form-control {
                    resize: vertical;
                    min-height: 100px;
                }
                .btn-danger {
                    background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
                    color: white;
                }
                .btn-danger:hover {
                    opacity: 0.9;
                }
            `}</style>
        </div>
    );
};

export default UserProfile;
