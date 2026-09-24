import React, { useState } from 'react';
import { Eye, EyeOff, Bot } from 'lucide-react';
import '../../index.css';

interface ConnexionProps {
  onLogin: (username: string) => void;
}

interface UserAccount {
  login: string;
  password: string;
}

const MOCK_USER = {
  login: 'clarisse15032004@gmail.com',
  password: 'password123'
};


function Connexion({ onLogin }: ConnexionProps) {
  const [formData, setFormData] = useState({ login: '', password: '' });
  const [confirmation, setConfirmation] = useState('');
  const [isRegistering, setIsRegistering] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const switchMode = () => {
    setIsRegistering((prev) => !prev);
    setFormData({ login: '', password: '' });
    setConfirmation('');
    setError('');
    setSuccess(false);
  };

  const handleSubmit = (e: React.SubmitEvent) => {
    e.preventDefault();
    setError('');

    if (isRegistering) {
      if (formData.password !== confirmation) {
        setError('The passwords do not match.');
        return;
      }

      const accounts: UserAccount[] = JSON.parse(
        localStorage.getItem('userAccounts') || '[]'
      );
      const accountExists = [MOCK_USER, ...accounts].some(
        (account) => account.login === formData.login
      );

      if (accountExists) {
        setError('An account already exists with this email address.');
        return;
      }

      localStorage.setItem(
        'userAccounts',
        JSON.stringify([...accounts, formData])
      );
      setSuccess(true);
      localStorage.setItem('fakeToken', '123456789');
      onLogin(formData.login);
      return;
    }

    const accounts: UserAccount[] = JSON.parse(
      localStorage.getItem('userAccounts') || '[]'
    );
    const isValidAccount = [MOCK_USER, ...accounts].some(
      (account) =>
        account.login === formData.login && account.password === formData.password
    );

    if (isValidAccount) {
      setSuccess(true);
      localStorage.setItem('fakeToken', '123456789');
      onLogin(formData.login);
    } else {
      setError('Invalid email address or password.');
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
        <h2>{isRegistering ? 'Create your account' : 'Welcome back'}</h2>
        <p className="connexion-subtitle">
          {isRegistering ? 'Join QuadGPT in a few seconds' : 'Please sign in to your account'}
        </p>

        {error && <p className="connexion-message connexion-error">{error}</p>}
        {success && <p className="connexion-message connexion-success">Connexion réussie !</p>}

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

          {isRegistering && (
            <div className="form-group">
              <label htmlFor="confirmation">Confirm password</label>
              <input
                type="password"
                id="confirmation"
                name="confirmation"
                placeholder="••••••••"
                value={confirmation}
                onChange={(e) => setConfirmation(e.target.value)}
                required
              />
            </div>
          )}

          <button type="submit" className="submit-btn">
            {isRegistering ? 'Create account' : 'Sign in'}
          </button>
        </form>

        <p className="connexion-switch">
          {isRegistering ? 'Already have an account?' : "Don't have an account?"}{' '}
          <button type="button" onClick={switchMode}>
            {isRegistering ? 'Sign in' : 'Create one'}
          </button>
        </p>
      </div>
    </div>
  );
}

export default Connexion;