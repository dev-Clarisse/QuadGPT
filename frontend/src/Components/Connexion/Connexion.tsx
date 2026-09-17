import '../../index.css';

function Connexion() {
  return (
    <div className="connexion-wrapper">
      <div className="connexion-card">
        <h2>Welcome</h2>
        <p className="connexion-subtitle">Please sign in to your account</p>
        
        <form className="connexion-form" onSubmit={(e) => e.preventDefault()}>
          <div className="form-group">
            <label htmlFor="login">Username</label>
            <input 
              type="text" 
              id="login" 
              name="login" 
              placeholder="e.g. clarisse"
              required 
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input 
              type="password" 
              id="password" 
              name="password" 
              placeholder="••••••••"
              required 
            />
          </div>

          <button type="submit" className="submit-btn">Sign in</button>
        </form>
      </div>
    </div>
  );
}

export default Connexion;