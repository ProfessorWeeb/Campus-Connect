import React, { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import axios from 'axios';
import './Register.css';

const Register = () => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    major: '',
  });
  const [error, setError] = useState('');
  const { login } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      const response = await axios.post('http://localhost:8080/api/auth/register', formData);
      login(response.data.token);
      navigate('/dashboard');
    } catch (err) {
      console.error('Registration error:', err);
      if (err.code === 'ECONNREFUSED' || err.message.includes('Network Error') || !err.response) {
        setError('❌ Cannot connect to server. Please make sure the backend is running on http://localhost:8080');
      } else if (err.response?.data?.message) {
        setError(`❌ ${err.response.data.message}`);
      } else if (err.response?.data?.error) {
        setError(`❌ ${err.response.data.error}`);
      } else if (err.response?.status === 400) {
        setError('❌ Invalid registration data. Please check all fields.');
      } else if (err.response?.status === 500) {
        setError('❌ Server error. Please try again later.');
      } else {
        setError(`❌ Registration failed: ${err.message || 'Unknown error'}`);
      }
    }
  };

  return (
    <div className="register-container">
      <div className="register-card">
        <h2>Create an Account</h2>
        {error && <div className="error-message">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Username *</label>
            <input
              type="text"
              name="username"
              value={formData.username}
              onChange={handleChange}
              required
            />
          </div>
          <div className="form-group">
            <label>Email *</label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              required
            />
          </div>
          <div className="form-group">
            <label>Password *</label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              required
              minLength="6"
            />
          </div>
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
          <button type="submit" className="btn btn-primary">
            Register
          </button>
        </form>
        <p>
          Already have an account? <a href="/login">Login here</a>
        </p>
      </div>
    </div>
  );
};

export default Register;

