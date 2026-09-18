import React, { useState } from 'react';
import { Eye, EyeOff, Bot } from 'lucide-react';
import '../../index.css';

interface ConnexionProps {
  onLogin: (username: string) => void;
}

const MOCK_USER = {
  login: 'clarisse15032004@gmail.com',
  password: 'password123'
};


function Connexion({ onLogin }: ConnexionProps) {
  const [formData, setFormData] = useState({ login: '', password: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e: React.SubmitEvent) => {
    e.preventDefault();
    setError('');

    if (
      formData.login === MOCK_USER.login &&
      formData.password === MOCK_USER.password
    ) {
      setSuccess(true);
      localStorage.setItem('fakeToken', '123456789');
      onLogin(formData.login);
    } else {
      setError(`Identifiants incorrects (Essayez: ${MOCK_USER.login} / ${MOCK_USER.password})`);
    }
  };

  return (
    <div className="connexion-wrapper" style={{ position: 'relative', width: '100%', height: '100vh' }}>
      
      
      <header style={{
        position: 'absolute',
        top: '20px',
        left: '24px',
        display: 'flex',
        alignItems: 'center',
        gap: '12px'
      }}>
        <div style={{
          backgroundColor: '#aa3bff',
          borderRadius: '10px',
          padding: '6px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: '#ffffff'
        }}>
          <Bot size={22} />
        </div>
        <span style={{ fontSize: '1.4rem', fontWeight: 'bold', color: '#ffffff', letterSpacing: '0.5px' }}>
          QuadGPT
        </span>
      </header>

   
      <div className="connexion-card">
        <h2>Welcome back</h2>
        <p className="connexion-subtitle">Please sign in to your account</p>

        {error && <p style={{ color: 'red', fontSize: '0.9rem' }}>{error}</p>}
        {success && <p style={{ color: 'green', fontSize: '0.9rem' }}>Connexion réussie !</p>}

        <form className="connexion-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="login">Email</label>
            <input
              type="email"
              id="login"
              name="login"
              placeholder="e.g. email@gmail.com"
              value={formData.login}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
              <input
                type={showPassword ? 'text' : 'password'}
                id="password"
                name="password"
                placeholder="••••••••"
                value={formData.password}
                onChange={handleChange}
                style={{ width: '100%', paddingRight: '40px' }}
                required
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                style={{
                  position: 'absolute',
                  right: '10px',
                  background: 'none',
                  border: 'none',
                  cursor: 'pointer',
                  color: '#8e8ea0',
                  display: 'flex',
                  alignItems: 'center'
                }}
                title={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
          </div>

          <button type="submit" className="submit-btn">Sign in</button>
        </form>
      </div>
    </div>
  );
}

export default Connexion;