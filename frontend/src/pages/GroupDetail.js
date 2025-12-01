import axios from 'axios';
import React, { useContext, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import './GroupDetail.css';

const GroupDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);
  const [group, setGroup] = useState(null);
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [joinRequestMessage, setJoinRequestMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [joining, setJoining] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [editForm, setEditForm] = useState({
    name: '',
    description: '',
    courseName: '',
    courseCode: '',
    topic: '',
    maxSize: 10,
    visibility: 'PUBLIC',
    requiresInvite: false,
  });
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    fetchGroup();
    fetchMessages();
  }, [id]);

  useEffect(() => {
    if (group) {
      setEditForm({
        name: group.name || '',
        description: group.description || '',
        courseName: group.courseName || '',
        courseCode: group.courseCode || '',
        topic: group.topic || '',
        maxSize: group.maxSize || 10,
        visibility: group.visibility || 'PUBLIC',
        requiresInvite: group.requiresInvite || false,
      });
    }
  }, [group]);

  const fetchGroup = async () => {
    try {
      const response = await axios.get(`http://localhost:8080/api/groups/${id}`);
      setGroup(response.data);
    } catch (error) {
      console.error('Error fetching group:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchMessages = async () => {
    try {
      const response = await axios.get(`http://localhost:8080/api/messages/group/${id}`);
      setMessages(response.data);
    } catch (error) {
      console.error('Error fetching messages:', error);
    }
  };

  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!newMessage.trim()) return;

    try {
      await axios.post('http://localhost:8080/api/messages/group', null, {
        params: {
          groupId: id,
          content: newMessage,
        },
      });
      setNewMessage('');
      fetchMessages();
    } catch (error) {
      console.error('Error sending message:', error);
      alert('Failed to send message. You may need to join the group first.');
    }
  };

  const handleJoin = async () => {
    if (!user) {
      alert('Please log in to join groups');
      navigate('/login');
      return;
    }

    // If group requires invite, show confirmation
    if (requiresInvite && !joinRequestMessage.trim()) {
      // For invite-only groups, user should provide a message
      // But we'll still allow joining without message
    }

    setJoining(true);
    try {
      const token = localStorage.getItem('token');
      const params = {};
      if (joinRequestMessage.trim()) {
        params.message = joinRequestMessage;
      }

      const response = await axios.post(`http://localhost:8080/api/groups/${id}/join`, null, {
        params: params,
        headers: {
          Authorization: `Bearer ${token}`
        }
      });

      // Check if it was a direct join (returns GroupDTO) or a join request (returns GroupJoinRequest)
      if (response.data.id && response.data.name) {
        // Direct join successful - automatic join
        if (!requiresInvite) {
          // Silent success for automatic joins, just refresh
          console.log('Successfully joined the group automatically');
        } else {
          alert('Successfully joined the group!');
        }
        setJoinRequestMessage('');
      } else if (response.data.status) {
        // Join request created
        alert('Join request sent! The group admin will review your request.');
        setJoinRequestMessage('');
      }

      // Refresh group data to update membership status
      await fetchGroup();
      await fetchMessages(); // Refresh messages in case user can now see them
    } catch (error) {
      console.error('Error joining group:', error);
      if (error.response?.data?.message) {
        alert(`Failed to join: ${error.response.data.message}`);
      } else if (error.response?.status === 403) {
        alert('You do not have permission to join this group.');
      } else {
        alert('Failed to join group. Please try again.');
      }
    } finally {
      setJoining(false);
    }
  };

  const handleLeave = async () => {
    if (!user) {
      return;
    }

    if (!window.confirm('Are you sure you want to leave this group?')) {
      return;
    }

    try {
      await axios.post(`http://localhost:8080/api/groups/${id}/leave`);
      alert('You have left the group.');
      await fetchGroup();
    } catch (error) {
      console.error('Error leaving group:', error);
      if (error.response?.data?.message) {
        alert(`Failed to leave: ${error.response.data.message}`);
      } else {
        alert('Failed to leave group. Please try again.');
      }
    }
  };

  const handleEdit = () => {
    setEditMode(true);
  };

  const handleCancelEdit = () => {
    setEditMode(false);
    if (group) {
      setEditForm({
        name: group.name || '',
        description: group.description || '',
        courseName: group.courseName || '',
        courseCode: group.courseCode || '',
        topic: group.topic || '',
        maxSize: group.maxSize || 10,
        visibility: group.visibility || 'PUBLIC',
        requiresInvite: group.requiresInvite || false,
      });
    }
  };

  const handleSaveEdit = async (e) => {
    e.preventDefault();
    if (!user) {
      return;
    }

    try {
      const params = new URLSearchParams();
      Object.keys(editForm).forEach(key => {
        if (editForm[key] !== '' && editForm[key] !== null && editForm[key] !== undefined) {
          if (key === 'requiresInvite') {
            params.append(key, editForm[key].toString());
          } else {
            params.append(key, editForm[key]);
          }
        }
      });

      await axios.post(`http://localhost:8080/api/groups/${id}?${params.toString()}`);
      setEditMode(false);
      await fetchGroup();
      alert('Group updated successfully!');
    } catch (error) {
      console.error('Error updating group:', error);
      if (error.response?.data?.message) {
        alert(`Failed to update: ${error.response.data.message}`);
      } else {
        alert('Failed to update group. Please try again.');
      }
    }
  };

  const handleDelete = async () => {
    if (!user) {
      return;
    }

    if (!window.confirm('Are you sure you want to delete this group? This action cannot be undone. All members will be removed and all messages will be deleted.')) {
      return;
    }

    setDeleting(true);
    try {
      await axios.delete(`http://localhost:8080/api/groups/${id}`);
      alert('Group deleted successfully.');
      navigate('/groups');
    } catch (error) {
      console.error('Error deleting group:', error);
      if (error.response?.data?.message) {
        alert(`Failed to delete: ${error.response.data.message}`);
      } else {
        alert('Failed to delete group. Please try again.');
      }
      setDeleting(false);
    }
  };

  if (loading) {
    return <div className="container">Loading...</div>;
  }

  if (!group) {
    return <div className="container">Group not found</div>;
  }

  // Check if current user is a member
  const isMember = user && group.memberIds && group.memberIds.includes(user.id);
  const isCreator = user && group.creatorId === user.id;
  const requiresInvite = group.requiresInvite === true;
  const isPublic = group.visibility === 'PUBLIC';
  
  // Determine join button text and behavior
  const getJoinButtonText = () => {
    if (requiresInvite) {
      return 'Request to Join';
    } else if (isPublic) {
      return 'Join Group';
    } else {
      return 'Join Group (Private)';
    }
  };

  return (
    <div className="container">
      <button onClick={() => navigate('/groups')} className="btn btn-secondary">
        ← Back to Groups
      </button>
      <div className="group-detail-header">
        <h1>{group.name}</h1>
        <div className="group-actions">
          {!isMember && !isCreator && (
            <div className="join-section">
              {requiresInvite && (
                <textarea
                  placeholder="Optional message to group admin..."
                  value={joinRequestMessage}
                  onChange={(e) => setJoinRequestMessage(e.target.value)}
                  rows="3"
                />
              )}
              <button 
                onClick={handleJoin} 
                className="btn btn-primary"
                disabled={joining}
              >
                {joining ? 'Joining...' : getJoinButtonText()}
              </button>
            </div>
          )}
          {isMember && !isCreator && (
            <button onClick={handleLeave} className="btn btn-secondary">
              Leave Group
            </button>
          )}
          {isCreator && (
            <div className="creator-actions">
              <button onClick={handleEdit} className="btn btn-primary">
                Edit Group
              </button>
              <button onClick={handleDelete} className="btn btn-danger" disabled={deleting}>
                {deleting ? 'Deleting...' : 'Delete Group'}
              </button>
            </div>
          )}
        </div>
      </div>

      {!editMode ? (
        <div className="group-info card">
          <p><strong>Course:</strong> {group.courseName} {group.courseCode && `(${group.courseCode})`}</p>
          {group.topic && <p><strong>Topic:</strong> {group.topic}</p>}
          {group.description && <p><strong>Description:</strong> {group.description}</p>}
          <p><strong>Members:</strong> {group.currentSize}/{group.maxSize}</p>
          <p><strong>Created by:</strong> {group.creatorName}</p>
          <p><strong>Privacy:</strong> {
            isPublic 
              ? (requiresInvite ? 'Public - Invite Only' : 'Public - Open Join')
              : (requiresInvite ? 'Private - Invite Only' : 'Private - Direct Join')
          }</p>
        </div>
      ) : (
        <div className="group-edit-form card">
          <h2>Edit Group</h2>
          <form onSubmit={handleSaveEdit}>
            <div className="form-group">
              <label>Group Name *</label>
              <input
                type="text"
                value={editForm.name}
                onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label>Course Name *</label>
              <input
                type="text"
                value={editForm.courseName}
                onChange={(e) => setEditForm({ ...editForm, courseName: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label>Course Code</label>
              <input
                type="text"
                value={editForm.courseCode}
                onChange={(e) => setEditForm({ ...editForm, courseCode: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label>Topic</label>
              <input
                type="text"
                value={editForm.topic}
                onChange={(e) => setEditForm({ ...editForm, topic: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label>Description</label>
              <textarea
                value={editForm.description}
                onChange={(e) => setEditForm({ ...editForm, description: e.target.value })}
                rows="4"
              />
            </div>
            <div className="form-group">
              <label>Max Size</label>
              <input
                type="number"
                value={editForm.maxSize}
                onChange={(e) => setEditForm({ ...editForm, maxSize: parseInt(e.target.value) })}
                min={group.currentSize}
                max="50"
                required
              />
              <small>Minimum: {group.currentSize} (current members)</small>
            </div>
            <div className="form-group">
              <label>Visibility</label>
              <select
                value={editForm.visibility}
                onChange={(e) => setEditForm({ ...editForm, visibility: e.target.value })}
              >
                <option value="PUBLIC">Public</option>
                <option value="PRIVATE">Private</option>
              </select>
            </div>
            <div className="form-group">
              <label>
                <input
                  type="checkbox"
                  checked={editForm.requiresInvite}
                  onChange={(e) => setEditForm({ ...editForm, requiresInvite: e.target.checked })}
                />
                Require Approval to Join
              </label>
            </div>
            <div className="form-actions">
              <button type="submit" className="btn btn-primary">
                Save Changes
              </button>
              <button type="button" onClick={handleCancelEdit} className="btn btn-secondary">
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      {isMember && (
        <div className="group-chat card">
          <h2>Group Chat</h2>
          <div className="messages-container">
            {messages.map((message) => (
              <div key={message.id} className="message">
                <strong>{message.senderName}:</strong> {message.content}
                <span className="message-time">
                  {new Date(message.createdAt).toLocaleString()}
                </span>
              </div>
            ))}
          </div>
          <form onSubmit={handleSendMessage} className="message-form">
            <input
              type="text"
              value={newMessage}
              onChange={(e) => setNewMessage(e.target.value)}
              placeholder="Type a message..."
            />
            <button type="submit" className="btn btn-primary">
              Send
            </button>
          </form>
        </div>
      )}
    </div>
  );
};

export default GroupDetail;

