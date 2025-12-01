import React, { useState, useContext, useEffect } from 'react';
import { AuthContext } from '../context/AuthContext';
import axios from 'axios';
import './Profile.css';

const Profile = () => {
  const { user: contextUser } = useContext(AuthContext);
  const [user, setUser] = useState(contextUser);
  const [editMode, setEditMode] = useState(false);
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    major: '',
    bio: '',
    visibility: 'PUBLIC',
    birthday: '',
    schoolYear: '',
    phoneNumber: '',
    location: '',
    linkedin: '',
    github: '',
  });

  useEffect(() => {
    if (contextUser) {
      setUser(contextUser);
      setFormData({
        firstName: contextUser.firstName || '',
        lastName: contextUser.lastName || '',
        major: contextUser.major || '',
        bio: contextUser.bio || '',
        visibility: contextUser.visibility || 'PUBLIC',
        birthday: contextUser.birthday || '',
        schoolYear: contextUser.schoolYear || '',
        phoneNumber: contextUser.phoneNumber || '',
        location: contextUser.location || '',
        linkedin: contextUser.linkedin || '',
        github: contextUser.github || '',
      });
    }
  }, [contextUser]);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const params = new URLSearchParams();
      Object.keys(formData).forEach(key => {
        if (formData[key]) {
          params.append(key, formData[key]);
        }
      });
      const response = await axios.put(`http://localhost:8080/api/users/me?${params.toString()}`);
      setUser(response.data);
      setEditMode(false);
    } catch (error) {
      console.error('Error updating profile:', error);
      alert('Failed to update profile. Please try again.');
    }
  };

  if (!user) {
    return <div className="container">Loading...</div>;
  }

  return (
    <div className="container">
      <h1>My Profile</h1>
      <div className="profile-card card">
        {!editMode ? (
          <>
            <div className="profile-header">
              <h2>{user.username}</h2>
              <button onClick={() => setEditMode(true)} className="btn btn-primary">
                Edit Profile
              </button>
            </div>
            <div className="profile-info">
              <div className="profile-section">
                <h3>Basic Information</h3>
                <p><strong>Email:</strong> {user.email}</p>
                {user.firstName && <p><strong>First Name:</strong> {user.firstName}</p>}
                {user.lastName && <p><strong>Last Name:</strong> {user.lastName}</p>}
                {user.major && <p><strong>Major:</strong> {user.major}</p>}
                {user.schoolYear && <p><strong>School Year:</strong> {user.schoolYear}</p>}
                <p><strong>Role:</strong> {user.role}</p>
                <p><strong>Visibility:</strong> {user.visibility}</p>
              </div>

              {user.bio && (
                <div className="profile-section">
                  <h3>About</h3>
                  <p className="bio-text">{user.bio}</p>
                </div>
              )}

              {(user.birthday || user.phoneNumber || user.location) && (
                <div className="profile-section">
                  <h3>Contact & Location</h3>
                  {user.birthday && <p><strong>Birthday:</strong> {new Date(user.birthday).toLocaleDateString()}</p>}
                  {user.phoneNumber && <p><strong>Phone:</strong> {user.phoneNumber}</p>}
                  {user.location && <p><strong>Location:</strong> {user.location}</p>}
                </div>
              )}

              {(user.linkedin || user.github) && (
                <div className="profile-section">
                  <h3>Social Links</h3>
                  {user.linkedin && (
                    <p>
                      <strong>LinkedIn:</strong>{' '}
                      <a href={user.linkedin} target="_blank" rel="noopener noreferrer">
                        {user.linkedin}
                      </a>
                    </p>
                  )}
                  {user.github && (
                    <p>
                      <strong>GitHub:</strong>{' '}
                      <a href={user.github} target="_blank" rel="noopener noreferrer">
                        {user.github}
                      </a>
                    </p>
                  )}
                </div>
              )}
              {user.interests && user.interests.length > 0 && (
                <div>
                  <strong>Interests:</strong>
                  <ul>
                    {user.interests.map((interest, index) => (
                      <li key={index}>{interest}</li>
                    ))}
                  </ul>
                </div>
              )}
              {user.skills && user.skills.length > 0 && (
                <div>
                  <strong>Skills:</strong>
                  <ul>
                    {user.skills.map((skill, index) => (
                      <li key={index}>{skill}</li>
                    ))}
                  </ul>
                </div>
              )}
              {user.courses && user.courses.length > 0 && (
                <div>
                  <strong>Courses:</strong>
                  <ul>
                    {user.courses.map((course, index) => (
                      <li key={index}>{course}</li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          </>
        ) : (
          <form onSubmit={handleSubmit}>
            <h2>Edit Profile</h2>
            <div className="form-group">
              <label>First Name</label>
              <input
                type="text"
                name="firstName"
                value={formData.firstName}
                onChange={handleChange}
              />
            </div>
            <div className="form-group">
              <label>Last Name</label>
              <input
                type="text"
                name="lastName"
                value={formData.lastName}
                onChange={handleChange}
              />
            </div>
            <div className="form-group">
              <label>Major</label>
              <input
                type="text"
                name="major"
                value={formData.major}
                onChange={handleChange}
              />
            </div>
            <div className="form-group">
              <label>Bio</label>
              <textarea
                name="bio"
                value={formData.bio}
                onChange={handleChange}
                rows="4"
                placeholder="Tell us about yourself..."
              />
            </div>
            <div className="form-group">
              <label>Birthday</label>
              <input
                type="date"
                name="birthday"
                value={formData.birthday}
                onChange={handleChange}
              />
            </div>
            <div className="form-group">
              <label>School Year</label>
              <select
                name="schoolYear"
                value={formData.schoolYear}
                onChange={handleChange}
              >
                <option value="">Select...</option>
                <option value="Freshman">Freshman</option>
                <option value="Sophomore">Sophomore</option>
                <option value="Junior">Junior</option>
                <option value="Senior">Senior</option>
                <option value="Graduate">Graduate</option>
              </select>
            </div>
            <div className="form-group">
              <label>Phone Number</label>
              <input
                type="tel"
                name="phoneNumber"
                value={formData.phoneNumber}
                onChange={handleChange}
                placeholder="(555) 123-4567"
              />
            </div>
            <div className="form-group">
              <label>Location</label>
              <input
                type="text"
                name="location"
                value={formData.location}
                onChange={handleChange}
                placeholder="e.g., Macon, GA or Warner Robins Campus"
              />
            </div>
            <div className="form-group">
              <label>LinkedIn URL</label>
              <input
                type="url"
                name="linkedin"
                value={formData.linkedin}
                onChange={handleChange}
                placeholder="https://linkedin.com/in/yourprofile"
              />
            </div>
            <div className="form-group">
              <label>GitHub URL</label>
              <input
                type="url"
                name="github"
                value={formData.github}
                onChange={handleChange}
                placeholder="https://github.com/yourusername"
              />
            </div>
            <div className="form-group">
              <label>Visibility</label>
              <select
                name="visibility"
                value={formData.visibility}
                onChange={handleChange}
              >
                <option value="PUBLIC">Public</option>
                <option value="PRIVATE">Private</option>
              </select>
            </div>
            <div className="form-actions">
              <button type="submit" className="btn btn-primary">
                Save Changes
              </button>
              <button
                type="button"
                onClick={() => setEditMode(false)}
                className="btn btn-secondary"
              >
                Cancel
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};

export default Profile;

