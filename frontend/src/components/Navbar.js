import React, { useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import './Navbar.css';

const Navbar = () => {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="container">
        <Link to="/dashboard" className="navbar-brand">
          Campus Connect
        </Link>
        {user && (
          <div className="navbar-menu">
            <Link to="/dashboard">Dashboard</Link>
            <Link to="/groups">Groups</Link>
            <Link to="/messages">Messages</Link>
            <Link to="/profile">Profile</Link>
            <span className="navbar-user">Welcome, {user.username}</span>
            <button onClick={handleLogout} className="btn btn-secondary">
              Logout
            </button>
          </div>
        )}
      </div>
    </nav>
  );
};

export default Navbar;

