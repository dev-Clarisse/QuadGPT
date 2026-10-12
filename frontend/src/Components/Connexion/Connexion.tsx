import React, { useState } from 'react';
import { Eye, EyeOff, Bot, ChevronDown, Building2 } from 'lucide-react';
import '../../index.css';

interface ConnexionProps {
  onLogin: (username: string, token: string) => void;
}

const DEPARTMENTS = [
  { value: 'RH', label: 'Human Resources (HR)' },
  { value: 'FINANCE', label: 'Finance & Accounting' },
  { value: 'IT', label: 'IT & Engineering' },
  { value: 'MARKETING', label: 'Marketing & Communication' },
  { value: 'DIRECTION', label: 'Management & Executive' },
  { value: 'SALES', label: 'Sales & Business Development' },
  { value: 'SUPPORT', label: 'Customer Support' },
] as const;

function Connexion({ onLogin }: ConnexionProps) {
  const [formData, setFormData] = useState({ login: '', password: '' });
  const [department, setDepartment] = useState('');
  const [confirmation, setConfirmation] = useState('');
  const [isRegistering, setIsRegistering] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    if (name === 'department') {
      setDepartment(value);
    } else {
      setFormData((prev) => ({ ...prev, [name]: value }));
    }
  };

  const switchMode = () => {
    setIsRegistering((prev) => !prev);
    setFormData({ login: '', password: '' });
    setDepartment('');
    setConfirmation('');
    setError('');
    setSuccess(false);
    setShowPassword(false);
    setShowConfirmPassword(false);
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');

    if (isRegistering) {
      if (!department) {
        setError('Please select a department.');
        return;
      }
      if (formData.password.length < 8) {
        setError('Password must be at least 8 characters long.');
        return;
      }
      if (formData.password !== confirmation) {
        setError('Passwords do not match.');
        return;
      }
    }

    setIsSubmitting(true);
    try {
      if (isRegistering) {
        const registerResponse = await fetch('/api/auth/register', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            email: formData.login,
            password: formData.password,
            department,
          }),
        });

        if (!registerResponse.ok) {
          throw new Error(
            (await registerResponse.text()) || 'Unable to create your account.'
          );
        }
      }

      const loginResponse = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: formData.login,
          password: formData.password,
        }),
      });

      if (!loginResponse.ok) {
        throw new Error(
          (await loginResponse.text()) || 'Invalid email address or password.'
        );
      }

      const { token } = (await loginResponse.json()) as { token: string };
      if (!token) {
        throw new Error('The server did not return an authentication token.');
      }

      setSuccess(true);
      onLogin(formData.login, token);
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : 'Unable to connect to the server.'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="connexion-wrapper">
      <header className="connexion-header">
        <div className="connexion-logo-icon">
          <Bot size={22} />
        </div>
        <span className="connexion-logo-text">QuadGPT</span>
      </header>

      <div className="connexion-card">
        <h2>{isRegistering ? 'Create an account' : 'Welcome back'}</h2>
        <p className="connexion-subtitle">
          {isRegistering ? 'Join QuadGPT in just a few seconds' : 'Please sign in to your account'}
        </p>

        {error && <p className="connexion-message connexion-error">{error}</p>}
        {success && <p className="connexion-message connexion-success">Successfully connected!</p>}

        <form className="connexion-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="login">Email address</label>
            <input
              type="email"
              id="login"
              name="login"
              placeholder="name@company.com"
              value={formData.login}
              onChange={handleChange}
              required
            />
          </div>

          {isRegistering && (
            <div className="form-group">
              <label htmlFor="department">Department / Team</label>
              <div className="select-wrapper">
                <Building2 size={18} className="field-icon-left" />
                <select
                  id="department"
                  name="department"
                  value={department}
                  onChange={handleChange}
                  required
                  className={!department ? 'placeholder-selected' : ''}
                >
                  <option value="" disabled>
                    Select your department
                  </option>
                  {DEPARTMENTS.map((dept) => (
                    <option key={dept.value} value={dept.value}>
                      {dept.label}
                    </option>
                  ))}
                </select>
                <ChevronDown size={18} className="field-icon-right" />
              </div>
            </div>
          )}

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <div className="password-input-wrapper">
              <input
                type={showPassword ? 'text' : 'password'}
                id="password"
                name="password"
                placeholder="••••••••"
                value={formData.password}
                onChange={handleChange}
                minLength={isRegistering ? 8 : undefined}
                required
              />
              <button
                type="button"
                className="toggle-password-btn"
                onClick={() => setShowPassword(!showPassword)}
                title={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
          </div>

          {isRegistering && (
            <div className="form-group">
              <label htmlFor="confirmation">Confirm password</label>
              <div className="password-input-wrapper">
                <input
                  type={showConfirmPassword ? 'text' : 'password'}
                  id="confirmation"
                  name="confirmation"
                  placeholder="••••••••"
                  value={confirmation}
                  onChange={(e) => setConfirmation(e.target.value)}
                  minLength={8}
                  required
                />
                <button
                  type="button"
                  className="toggle-password-btn"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  title={showConfirmPassword ? 'Hide password' : 'Show password'}
                >
                  {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>
          )}

          <button type="submit" className="submit-btn" disabled={isSubmitting}>
            {isSubmitting ? 'Please wait...' : isRegistering ? 'Create account' : 'Sign in'}
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