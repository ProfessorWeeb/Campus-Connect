import axios from 'axios';
import React, { useContext, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import './Dashboard.css';

const Dashboard = () => {
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();
  const [stats, setStats] = useState({
    groups: 0,
    messages: 0,
    unreadMessages: 0,
  });
  const [recommendedGroups, setRecommendedGroups] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (user) {
      fetchStats();
      fetchRecommendedGroups();
    }
  }, [user]);

  const fetchStats = async () => {
    try {
      const [groupsRes, messagesRes, unreadRes] = await Promise.all([
        axios.get('http://localhost:8080/api/groups/my-groups'),
        axios.get('http://localhost:8080/api/messages/inbox'),
        axios.get('http://localhost:8080/api/messages/unread-count'),
      ]);
      setStats({
        groups: groupsRes.data.length,
        messages: messagesRes.data.length,
        unreadMessages: unreadRes.data,
      });
    } catch (error) {
      console.error('Error fetching stats:', error);
    }
  };

  const fetchRecommendedGroups = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/groups/recommended');
      setRecommendedGroups(response.data);
    } catch (error) {
      console.error('Error fetching recommended groups:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleQuickJoin = async (groupId, requiresInvite) => {
    if (!user) {
      alert('Please log in to join groups');
      navigate('/login');
      return;
    }

    // If group requires invite, show confirmation
    if (requiresInvite) {
      if (!window.confirm('This group requires approval. Send a join request?')) {
        return;
      }
    }

    try {
      const token = localStorage.getItem('token');
      const response = await axios.post(`http://localhost:8080/api/groups/${groupId}/join`, null, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      
      // Check if it was a direct join (returns GroupDTO) or a join request
      if (response.data.id && response.data.name) {
        // Direct join successful - automatic join
        if (!requiresInvite) {
          // Silent success for automatic joins
          console.log('Successfully joined the group automatically');
        } else {
          alert('Successfully joined the group!');
        }
      } else if (response.data.status) {
        // Join request created
        alert('Join request sent! The group admin will review your request.');
      }
      
      fetchRecommendedGroups();
      fetchStats();
    } catch (error) {
      console.error('Error joining group:', error);
      if (error.response?.data?.message) {
        alert(`Failed to join: ${error.response.data.message}`);
      } else {
        alert('Failed to join group. Please try again.');
      }
    }
  };

  return (
    <div className="dashboard-container">
      <div className="dashboard-sidebar">
        <div className="sidebar-section">
          <h2>My Groups</h2>
          <div className="sidebar-stat">
            <span className="stat-value">{stats.groups}</span>
            <span className="stat-label">Groups</span>
          </div>
          <Link to="/groups" className="sidebar-link">
            View All Groups →
          </Link>
        </div>

        <div className="sidebar-section">
          <h2>Messages</h2>
          <div className="sidebar-stat">
            <span className="stat-value">{stats.messages}</span>
            <span className="stat-label">Total Messages</span>
          </div>
          {stats.unreadMessages > 0 && (
            <div className="unread-indicator">
              <span className="unread-count">{stats.unreadMessages}</span>
              <span className="unread-text">unread</span>
            </div>
          )}
          <Link to="/messages" className="sidebar-link">
            View Messages →
          </Link>
        </div>

        <div className="sidebar-actions">
          <Link to="/groups" className="btn btn-primary btn-block">
            Browse Groups
          </Link>
          <Link to="/groups" className="btn btn-secondary btn-block">
            Create Group
          </Link>
        </div>
      </div>

      <div className="dashboard-main">
        <div className="dashboard-header">
          <h1>Welcome back, {user?.username}!</h1>
          <p className="dashboard-subtitle">Here are some groups we think you might like</p>
        </div>

        {loading ? (
          <div className="loading-message">Loading recommendations...</div>
        ) : recommendedGroups.length > 0 ? (
          <div className="recommended-groups">
            <h2 className="section-title">Recommended for You</h2>
            <div className="groups-grid">
              {recommendedGroups.map((group) => {
                const isMember = user && group.memberIds && group.memberIds.includes(user.id);
                const canJoinDirectly = !group.requiresInvite && !isMember;

                return (
                  <div key={group.id} className="recommended-group-card">
                    <div className="group-header">
                      <h3>{group.name}</h3>
                      <span className="privacy-badge-small">
                        {group.visibility === 'PUBLIC' 
                          ? (group.requiresInvite ? '🔒 Public - Invite Only' : '🌐 Public')
                          : (group.requiresInvite ? '🔒 Private - Invite Only' : '🔒 Private')}
                      </span>
                    </div>
                    <p className="group-course">{group.courseName} {group.courseCode && `(${group.courseCode})`}</p>
                    {group.topic && <p className="group-topic">Topic: {group.topic}</p>}
                    {group.description && (
                      <p className="group-description">{group.description}</p>
                    )}
                    <div className="group-meta">
                      <span>👥 {group.currentSize}/{group.maxSize} members</span>
                      <span>By {group.creatorName}</span>
                    </div>
                    <div className="group-actions">
                      {isMember ? (
                        <span className="member-badge">✓ Member</span>
                      ) : canJoinDirectly ? (
                        <button
                          onClick={() => handleQuickJoin(group.id, group.requiresInvite)}
                          className="btn btn-success btn-sm"
                        >
                          Join
                        </button>
                      ) : (
                        <button
                          onClick={() => handleQuickJoin(group.id, group.requiresInvite)}
                          className="btn btn-secondary btn-sm"
                        >
                          Request to Join
                        </button>
                      )}
                      <Link to={`/groups/${group.id}`} className="btn btn-primary btn-sm">
                        View Details
                      </Link>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        ) : (
          <div className="no-recommendations">
            <h2>No recommendations yet</h2>
            <p>Join some groups or add courses to your profile to get personalized recommendations!</p>
            <Link to="/groups" className="btn btn-primary">
              Browse All Groups
            </Link>
          </div>
        )}
      </div>
    </div>
  );
};

export default Dashboard;

