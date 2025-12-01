import React, { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import axios from 'axios';
import './Login.css';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { login } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      const response = await axios.post('http://localhost:8080/api/auth/login', {
        email,
        password,
      });
      login(response.data.token);
      navigate('/dashboard');
    } catch (err) {
      console.error('Login error:', err);
      if (err.code === 'ECONNREFUSED' || err.message.includes('Network Error') || !err.response) {
        setError('❌ Cannot connect to server. Please make sure the backend is running on http://localhost:8080');
      } else if (err.response?.data?.message) {
        setError(`❌ ${err.response.data.message}`);
      } else if (err.response?.status === 401) {
        setError('❌ Invalid email or password.');
      } else if (err.response?.status === 500) {
        setError('❌ Server error. Please try again later.');
      } else {
        setError(`❌ Login failed: ${err.message || 'Unknown error'}`);
      }
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <h2>Login to Campus Connect</h2>
        {error && <div className="error-message">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          <button type="submit" className="btn btn-primary">
            Login
          </button>
        </form>
        <p>
          Don't have an account? <a href="/register">Register here</a>
        </p>
      </div>
    </div>
  );
};

export default Login;

