import { useState } from 'react';
import { useAuth } from '../context/AuthContext';

const UserProfile = () => {
    const { user, logout } = useAuth();
    const [activeTab, setActiveTab] = useState('profile');
    const [showContactForm, setShowContactForm] = useState(false);
    const [contactMessage, setContactMessage] = useState('');
    const [submitted, setSubmitted] = useState(false);

    const handleContactSubmit = (e) => {
        e.preventDefault();
        setSubmitted(true);
        setTimeout(() => {
            setShowContactForm(false);
            setContactMessage('');
            setSubmitted(false);
        }, 2000);
    };

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
                    <div className="card-header">
                        <h3 className="card-title">Personal Information</h3>
                    </div>
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
                            <span className="detail-value">#{user?.id}</span>
                        </div>
                    </div>
                    <div style={{ marginTop: '24px' }}>
                        <button className="btn btn-danger" onClick={logout}>
                            🚪 Logout
                        </button>
                    </div>
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
                    background: rgba(255,255,255,0.05);
                    color: #9ca3af;
                    cursor: pointer;
                    transition: all 0.3s;
                    font-size: 14px;
                }
                .tab-btn:hover {
                    background: rgba(255,255,255,0.1);
                }
                .tab-btn.active {
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    color: white;
                }
                .profile-details {
                    display: grid;
                    gap: 16px;
                }
                .detail-row {
                    display: flex;
                    justify-content: space-between;
                    padding: 16px;
                    background: rgba(255,255,255,0.03);
                    border-radius: 8px;
                    border: 1px solid rgba(255,255,255,0.05);
                }
                .detail-label {
                    color: #9ca3af;
                    font-weight: 500;
                }
                .detail-value {
                    color: white;
                    font-weight: 600;
                }
                .help-section {
                    display: grid;
                    gap: 16px;
                }
                .faq-item {
                    padding: 20px;
                    background: rgba(255,255,255,0.03);
                    border-radius: 8px;
                    border-left: 3px solid #667eea;
                }
                .faq-item h4 {
                    margin: 0 0 8px 0;
                    color: white;
                }
                .faq-item p {
                    margin: 0;
                    color: #9ca3af;
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
                    background: rgba(255,255,255,0.03);
                    border-radius: 8px;
                }
                .contact-icon {
                    font-size: 24px;
                }
                .contact-item strong {
                    display: block;
                    color: white;
                    margin-bottom: 4px;
                }
                .contact-item p {
                    margin: 0;
                    color: #9ca3af;
                }
                .contact-form-section {
                    padding-top: 24px;
                    border-top: 1px solid rgba(255,255,255,0.1);
                }
                .contact-form-section h4 {
                    margin: 0 0 16px 0;
                    color: white;
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
                    color: #9ca3af;
                    font-weight: 500;
                }
                .form-control {
                    width: 100%;
                    padding: 12px;
                    border-radius: 8px;
                    border: 1px solid rgba(255,255,255,0.1);
                    background: rgba(255,255,255,0.05);
                    color: white;
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
