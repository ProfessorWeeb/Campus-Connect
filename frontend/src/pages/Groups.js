import axios from 'axios';
import React, { useContext, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import './Groups.css';

const Groups = () => {
  const { user } = useContext(AuthContext);
  const [groups, setGroups] = useState([]);
  const [myCreatedGroups, setMyCreatedGroups] = useState([]);
  const [myJoinedGroups, setMyJoinedGroups] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [joiningGroups, setJoiningGroups] = useState(new Set());
  const [createForm, setCreateForm] = useState({
    name: '',
    description: '',
    courseName: '',
    courseCode: '',
    topic: '',
    maxSize: 10,
    visibility: 'PUBLIC',
    requiresInvite: false,
  });
  const [userSearchQuery, setUserSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [invitedUsers, setInvitedUsers] = useState([]);
  const [showUserSearch, setShowUserSearch] = useState(false);
  const [courses, setCourses] = useState([]);
  const [courseSearchQuery, setCourseSearchQuery] = useState('');
  const [showCourseDropdown, setShowCourseDropdown] = useState(false);

  useEffect(() => {
    fetchGroups();
    fetchCourses();
    if (user) {
      fetchMyCreatedGroups();
    }
  }, [user]);

  // Close course dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (showCourseDropdown && !event.target.closest('.course-selector')) {
        setShowCourseDropdown(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showCourseDropdown]);

  const fetchGroups = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/groups');
      setGroups(response.data);
    } catch (error) {
      console.error('Error fetching groups:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchMyCreatedGroups = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/groups/my-created-groups');
      const createdGroups = response.data;
      setMyCreatedGroups(createdGroups);
      // Fetch joined groups after created groups are loaded, passing the created groups to filter
      if (user) {
        await fetchMyJoinedGroups(createdGroups);
      }
    } catch (error) {
      console.error('Error fetching my created groups:', error);
    }
  };

  const fetchMyJoinedGroups = async (createdGroups = null) => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get('http://localhost:8080/api/groups/my-groups', {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      // Filter out groups the user created (those are shown in My Created Groups)
      const groupsToFilter = createdGroups || myCreatedGroups;
      const createdGroupIds = new Set(groupsToFilter.map(g => g.id));
      const joinedGroups = response.data.filter(group => !createdGroupIds.has(group.id));
      setMyJoinedGroups(joinedGroups);
    } catch (error) {
      console.error('Error fetching my joined groups:', error);
    }
  };

  const fetchCourses = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/courses');
      setCourses(response.data);
      console.log('Courses loaded:', response.data.length);
    } catch (error) {
      console.error('Error fetching courses:', error);
      alert('Failed to load courses. Make sure the backend is running and courses are initialized.');
    }
  };

  const handleCourseSelect = (course) => {
    setCreateForm({
      ...createForm,
      courseName: course.name,
      courseCode: course.code,
    });
    setCourseSearchQuery(course.code);
    setShowCourseDropdown(false);
  };

  const filteredCourses = courses.filter(course =>
    course.code.toLowerCase().includes(courseSearchQuery.toLowerCase()) ||
    course.name.toLowerCase().includes(courseSearchQuery.toLowerCase())
  );

  const handleSearch = async () => {
    try {
      if (!searchQuery.trim()) {
        fetchGroups();
        return;
      }
      const response = await axios.get('http://localhost:8080/api/groups/search', {
        params: { query: searchQuery },
      });
      setGroups(response.data);
    } catch (error) {
      console.error('Error searching groups:', error);
    }
  };

  const handleSearchUsers = async () => {
    if (!userSearchQuery.trim()) {
      setSearchResults([]);
      return;
    }

    try {
      const response = await axios.get('http://localhost:8080/api/users/search', {
        params: { query: userSearchQuery },
      });
      // Filter out already invited users and current user
      const filtered = response.data.filter(user => 
        !invitedUsers.some(invited => invited.id === user.id)
      );
      setSearchResults(filtered);
    } catch (error) {
      console.error('Error searching users:', error);
      setSearchResults([]);
    }
  };

  const handleAddInvite = (user) => {
    if (!invitedUsers.some(u => u.id === user.id)) {
      setInvitedUsers([...invitedUsers, user]);
      setUserSearchQuery('');
      setSearchResults([]);
    }
  };

  const handleRemoveInvite = (userId) => {
    setInvitedUsers(invitedUsers.filter(u => u.id !== userId));
  };

  const handleCreateGroup = async (e) => {
    e.preventDefault();
    
    // Validate required fields
    if (!createForm.name || !createForm.name.trim()) {
      alert('Please enter a Group Name.');
      return;
    }

    if (!createForm.courseName || !createForm.courseCode) {
      alert('Please select a course from the dropdown list. Type to search and click on a course to select it.');
      return;
    }

    if (!user) {
      alert('Please log in to create a group.');
      return;
    }

    try {
      const params = new URLSearchParams();
      Object.keys(createForm).forEach(key => {
        if (createForm[key] !== '' && createForm[key] !== null && createForm[key] !== undefined) {
          if (key === 'requiresInvite') {
            params.append(key, createForm[key].toString());
          } else {
            params.append(key, createForm[key]);
          }
        }
      });

      // Add invited user IDs
      if (invitedUsers.length > 0) {
        invitedUsers.forEach(user => {
          params.append('invitedUserIds', user.id);
        });
      }

      const response = await axios.post(`http://localhost:8080/api/groups?${params.toString()}`);
      setShowCreateForm(false);
      setCreateForm({
        name: '',
        description: '',
        courseName: '',
        courseCode: '',
        topic: '',
        maxSize: 10,
        visibility: 'PUBLIC',
        requiresInvite: false,
      });
      setInvitedUsers([]);
      setUserSearchQuery('');
      setSearchResults([]);
      fetchGroups();
      fetchMyCreatedGroups();
      fetchMyJoinedGroups();
      alert('Group created successfully! Invitations have been sent to selected users.');
    } catch (error) {
      console.error('Error creating group:', error);
      console.error('Error response:', error.response);
      
      let errorMessage = 'Failed to create group. ';
      
      if (error.response) {
        // Server responded with error
        if (error.response.status === 401 || error.response.status === 403) {
          errorMessage += 'You are not authenticated. Please log in again.';
        } else if (error.response.status === 400) {
          errorMessage += error.response.data?.message || 'Invalid data provided.';
        } else if (error.response.data?.message) {
          errorMessage += error.response.data.message;
        } else {
          errorMessage += `Server error (${error.response.status}).`;
        }
      } else if (error.request) {
        // Request made but no response
        errorMessage += 'Cannot connect to server. Make sure the backend is running.';
      } else {
        // Something else happened
        errorMessage += error.message || 'Unknown error occurred.';
      }
      
      alert(errorMessage);
    }
  };

  const handleQuickJoin = async (groupId, requiresInvite) => {
    if (!user) {
      alert('Please log in to join groups');
      return;
    }

    // If group requires invite, show confirmation
    if (requiresInvite) {
      if (!window.confirm('This group requires approval. Send a join request?')) {
        return;
      }
    }

    setJoiningGroups(prev => new Set(prev).add(groupId));
    try {
      const token = localStorage.getItem('token');
      const response = await axios.post(`http://localhost:8080/api/groups/${groupId}/join`, null, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      
      // Check if it was a direct join (returns GroupDTO with id and name) or a join request
      if (response.data.id && response.data.name) {
        // Direct join successful - automatic join
        // Only show alert if it was not automatic (but for open join groups, it should be automatic)
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
      
      // Refresh groups and created groups to update UI
      await fetchGroups();
      if (user) {
        await fetchMyCreatedGroups(); // This will also fetch joined groups
      }
    } catch (error) {
      console.error('Error joining group:', error);
      if (error.response?.data?.message) {
        alert(`Failed to join: ${error.response.data.message}`);
      } else {
        alert('Failed to join group. Please try again.');
      }
    } finally {
      setJoiningGroups(prev => {
        const next = new Set(prev);
        next.delete(groupId);
        return next;
      });
    }
  };

  if (loading) {
    return <div className="container">Loading...</div>;
  }

  return (
    <div className="container">
      <div className="groups-header">
        <h1>Study Groups</h1>
        <button
          onClick={() => setShowCreateForm(!showCreateForm)}
          className="btn btn-primary"
        >
          {showCreateForm ? 'Cancel' : 'Create Group'}
        </button>
      </div>

      {showCreateForm && (
        <div className="card">
          <h2>Create New Group</h2>
          <form onSubmit={handleCreateGroup}>
            <div className="form-group">
              <label>Group Name *</label>
              <input
                type="text"
                value={createForm.name}
                onChange={(e) => setCreateForm({ ...createForm, name: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label>Course *</label>
              <div className="course-selector">
                <input
                  type="text"
                  placeholder={courses.length === 0 ? "Loading courses..." : "Type to search for a course (e.g., CSCI 1301)..."}
                  value={courseSearchQuery}
                  onChange={(e) => {
                    const value = e.target.value;
                    setCourseSearchQuery(value);
                    if (value && courses.length > 0) {
                      setShowCourseDropdown(true);
                    }
                    if (!value) {
                      setCreateForm({ ...createForm, courseName: '', courseCode: '' });
                      setShowCourseDropdown(false);
                    }
                  }}
                  onFocus={() => {
                    if (courseSearchQuery && courses.length > 0) {
                      setShowCourseDropdown(true);
                    }
                  }}
                  disabled={courses.length === 0}
                />
                {showCourseDropdown && courseSearchQuery && courses.length > 0 && (
                  <div className="course-dropdown">
                    {filteredCourses.length > 0 ? (
                      filteredCourses.slice(0, 10).map((course) => (
                        <div
                          key={course.id}
                          className="course-option"
                          onClick={() => handleCourseSelect(course)}
                        >
                          <strong>{course.code}</strong> - {course.name}
                        </div>
                      ))
                    ) : (
                      <div className="course-option no-results" style={{ color: '#999', fontStyle: 'italic' }}>
                        No courses found. Try a different search.
                      </div>
                    )}
                  </div>
                )}
              </div>
              {courses.length === 0 && (
                <div style={{ marginTop: '8px', fontSize: '12px', color: '#dc3545' }}>
                  ⚠ Courses not loaded. Check console for errors or initialize courses in the backend.
                </div>
              )}
              {courses.length > 0 && createForm.courseCode && createForm.courseName ? (
                <div className="selected-course" style={{ marginTop: '8px', padding: '8px', backgroundColor: '#e8f5e9', borderRadius: '4px' }}>
                  ✓ Selected: <strong>{createForm.courseCode}</strong> - {createForm.courseName}
                </div>
              ) : courses.length > 0 ? (
                <div style={{ marginTop: '8px', fontSize: '12px', color: '#666' }}>
                  ⚠ Type to search and click a course from the dropdown above
                </div>
              ) : null}
            </div>
            <div className="form-group">
              <label>Topic</label>
              <input
                type="text"
                value={createForm.topic}
                onChange={(e) => setCreateForm({ ...createForm, topic: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label>Description</label>
              <textarea
                value={createForm.description}
                onChange={(e) => setCreateForm({ ...createForm, description: e.target.value })}
                rows="4"
              />
            </div>
            <div className="form-group">
              <label>Max Size</label>
              <input
                type="number"
                value={createForm.maxSize}
                onChange={(e) => setCreateForm({ ...createForm, maxSize: parseInt(e.target.value) })}
                min="2"
                max="50"
              />
            </div>
            <div className="form-group">
              <label>Invite Users (Optional)</label>
              <button
                type="button"
                onClick={() => setShowUserSearch(!showUserSearch)}
                className="btn btn-secondary btn-sm"
                style={{ marginBottom: '10px' }}
              >
                {showUserSearch ? 'Hide' : 'Add Users to Invite'}
              </button>

              {showUserSearch && (
                <div className="user-invite-section">
                  <div className="user-search">
                    <input
                      type="text"
                      placeholder="Search users by name, email, or username..."
                      value={userSearchQuery}
                      onChange={(e) => {
                        setUserSearchQuery(e.target.value);
                        if (e.target.value.trim()) {
                          handleSearchUsers();
                        } else {
                          setSearchResults([]);
                        }
                      }}
                      onKeyPress={(e) => e.key === 'Enter' && handleSearchUsers()}
                    />
                    <button
                      type="button"
                      onClick={handleSearchUsers}
                      className="btn btn-primary btn-sm"
                    >
                      Search
                    </button>
                  </div>

                  {searchResults.length > 0 && (
                    <div className="search-results">
                      {searchResults.map((user) => (
                        <div key={user.id} className="search-result-item">
                          <div className="user-info">
                            <strong>{user.username}</strong>
                            {user.firstName && user.lastName && (
                              <span> - {user.firstName} {user.lastName}</span>
                            )}
                            {user.email && <span className="user-email"> ({user.email})</span>}
                          </div>
                          <button
                            type="button"
                            onClick={() => handleAddInvite(user)}
                            className="btn btn-success btn-sm"
                          >
                            Add
                          </button>
                        </div>
                      ))}
                    </div>
                  )}

                  {invitedUsers.length > 0 && (
                    <div className="invited-users-list">
                      <h4>Users to Invite ({invitedUsers.length})</h4>
                      {invitedUsers.map((user) => (
                        <div key={user.id} className="invited-user-item">
                          <span>
                            <strong>{user.username}</strong>
                            {user.firstName && user.lastName && (
                              <span> - {user.firstName} {user.lastName}</span>
                            )}
                          </span>
                          <button
                            type="button"
                            onClick={() => handleRemoveInvite(user.id)}
                            className="btn btn-danger btn-sm"
                          >
                            Remove
                          </button>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>
            <div className="form-group privacy-settings">
              <label>Privacy Settings</label>
              <div className="privacy-options">
                <div className="privacy-section">
                  <label className="privacy-section-label">Visibility</label>
                  <div className="radio-group">
                    <label className="radio-option">
                      <input
                        type="radio"
                        name="visibility"
                        value="PUBLIC"
                        checked={createForm.visibility === 'PUBLIC'}
                        onChange={(e) => setCreateForm({ ...createForm, visibility: e.target.value })}
                      />
                      <div className="radio-content">
                        <span className="radio-label">Public</span>
                        <span className="radio-description">Visible in search and browse</span>
                      </div>
                    </label>
                    <label className="radio-option">
                      <input
                        type="radio"
                        name="visibility"
                        value="PRIVATE"
                        checked={createForm.visibility === 'PRIVATE'}
                        onChange={(e) => setCreateForm({ ...createForm, visibility: e.target.value })}
                      />
                      <div className="radio-content">
                        <span className="radio-label">Private</span>
                        <span className="radio-description">Hidden from search</span>
                      </div>
                    </label>
                  </div>
                </div>
                <div className="privacy-section">
                  <label className="checkbox-option">
                    <input
                      type="checkbox"
                      checked={createForm.requiresInvite}
                      onChange={(e) => setCreateForm({ ...createForm, requiresInvite: e.target.checked })}
                    />
                    <div className="checkbox-content">
                      <span className="checkbox-label">Require Approval to Join</span>
                      <span className="checkbox-description">Users must request to join (invite only)</span>
                    </div>
                  </label>
                </div>
                <div className="privacy-preview">
                  <strong>Current Setting:</strong>
                  <span className="privacy-preview-text">
                    {createForm.visibility === 'PUBLIC'
                      ? (createForm.requiresInvite ? '🌐 Public - Invite Only' : '🌐 Public - Open Join')
                      : (createForm.requiresInvite ? '🔒 Private - Invite Only' : '🔒 Private - Direct Join')}
                  </span>
                </div>
              </div>
            </div>
            <button type="submit" className="btn btn-primary">
              Create Group
            </button>
          </form>
        </div>
      )}

      <div className="search-bar">
        <input
          type="text"
          placeholder="Search groups by course, topic, or name..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
        />
        <button onClick={handleSearch} className="btn btn-primary">
          Search
        </button>
      </div>

      {myCreatedGroups.length > 0 && (
        <div className="my-created-groups-section">
          <h2 className="section-header">My Created Groups</h2>
          <div className="groups-list">
            {myCreatedGroups.map((group) => {
              const isMember = user && group.memberIds && group.memberIds.includes(user.id);
              const isCreator = user && group.creatorId === user.id;
              const canJoinDirectly = !group.requiresInvite && !isMember && !isCreator;
              const isJoining = joiningGroups.has(group.id);

              return (
                <div key={group.id} className="card group-card created-group-card">
                  <div className="creator-badge-header">
                    <span className="creator-badge-large">👑 You Created This</span>
                  </div>
                  <h3>{group.name}</h3>
                  <p className="group-course">{group.courseName} {group.courseCode && `(${group.courseCode})`}</p>
                  {group.topic && <p className="group-topic">Topic: {group.topic}</p>}
                  {group.description && <p className="group-description">{group.description}</p>}
                  <div className="group-meta">
                    <span>Members: {group.currentSize}/{group.maxSize}</span>
                    <span>Created by: {group.creatorName}</span>
                    <span className="privacy-badge">
                      {group.visibility === 'PUBLIC' 
                        ? (group.requiresInvite ? '🔒 Public - Invite Only' : '🌐 Public')
                        : (group.requiresInvite ? '🔒 Private - Invite Only' : '🔒 Private')}
                    </span>
                  </div>
                  <div className="group-actions">
                    {isMember && (
                      <span className="member-badge">✓ Member</span>
                    )}
                    {isCreator && (
                      <span className="creator-badge">👑 Creator</span>
                    )}
                    <Link to={`/groups/${group.id}`} className="btn btn-primary">
                      View Details
                    </Link>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {myJoinedGroups.length > 0 && (
        <div className="my-joined-groups-section">
          <h2 className="section-header">My Joined Groups</h2>
          <div className="groups-list">
            {myJoinedGroups.map((group) => {
              const isMember = user && group.memberIds && group.memberIds.includes(user.id);
              const isCreator = user && group.creatorId === user.id;
              const canJoinDirectly = !group.requiresInvite && !isMember && !isCreator;
              const isJoining = joiningGroups.has(group.id);

              return (
                <div key={group.id} className="card group-card joined-group-card">
                  <h3>{group.name}</h3>
                  <p className="group-course">{group.courseName} {group.courseCode && `(${group.courseCode})`}</p>
                  {group.topic && <p className="group-topic">Topic: {group.topic}</p>}
                  {group.description && <p className="group-description">{group.description}</p>}
                  <div className="group-meta">
                    <span>Members: {group.currentSize}/{group.maxSize}</span>
                    <span>Created by: {group.creatorName}</span>
                    <span className="privacy-badge">
                      {group.visibility === 'PUBLIC' 
                        ? (group.requiresInvite ? '🔒 Public - Invite Only' : '🌐 Public')
                        : (group.requiresInvite ? '🔒 Private - Invite Only' : '🔒 Private')}
                    </span>
                  </div>
                  <div className="group-actions">
                    {isMember && (
                      <span className="member-badge">✓ Member</span>
                    )}
                    {isCreator && (
                      <span className="creator-badge">👑 Creator</span>
                    )}
                    <Link to={`/groups/${group.id}`} className="btn btn-primary">
                      View Details
                    </Link>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      <div className="all-groups-section">
        <h2 className="section-header">
          {(myCreatedGroups.length > 0 || myJoinedGroups.length > 0) ? 'All Groups' : 'Groups'}
        </h2>
        <div className="groups-list">
          {groups
            .filter(group => 
              !myCreatedGroups.some(created => created.id === group.id) &&
              !myJoinedGroups.some(joined => joined.id === group.id)
            )
            .map((group) => {
              const isMember = user && group.memberIds && group.memberIds.includes(user.id);
              const isCreator = user && group.creatorId === user.id;
              const canJoinDirectly = !group.requiresInvite && !isMember && !isCreator;
              const isJoining = joiningGroups.has(group.id);

              return (
                <div key={group.id} className="card group-card">
                  <h3>{group.name}</h3>
                  <p className="group-course">{group.courseName} {group.courseCode && `(${group.courseCode})`}</p>
                  {group.topic && <p className="group-topic">Topic: {group.topic}</p>}
                  {group.description && <p className="group-description">{group.description}</p>}
                  <div className="group-meta">
                    <span>Members: {group.currentSize}/{group.maxSize}</span>
                    <span>Created by: {group.creatorName}</span>
                    <span className="privacy-badge">
                      {group.visibility === 'PUBLIC' 
                        ? (group.requiresInvite ? '🔒 Public - Invite Only' : '🌐 Public')
                        : (group.requiresInvite ? '🔒 Private - Invite Only' : '🔒 Private')}
                    </span>
                  </div>
                  <div className="group-actions">
                    {canJoinDirectly && (
                      <button 
                        onClick={() => handleQuickJoin(group.id, group.requiresInvite)}
                        className="btn btn-success"
                        disabled={isJoining}
                      >
                        {isJoining ? 'Joining...' : 'Join'}
                      </button>
                    )}
                    {isMember && (
                      <span className="member-badge">✓ Member</span>
                    )}
                    {isCreator && (
                      <span className="creator-badge">👑 Creator</span>
                    )}
                    <Link to={`/groups/${group.id}`} className="btn btn-primary">
                      View Details
                    </Link>
                  </div>
                </div>
              );
            })}
        </div>
      </div>
    </div>
  );
};

export default Groups;

