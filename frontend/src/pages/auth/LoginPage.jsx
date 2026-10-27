import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { useToast } from '../../hooks/useToast';
import { ShieldCheck, LogIn, Key, User, Info } from 'lucide-react';

const LoginPage = () => {
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('Password@123');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const { login } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();

  const handleDemoFill = (demoUser, demoPass) => {
    setUsername(demoUser);
    setPassword(demoPass);
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!username.trim() || !password) {
      setError('Please provide both username and password');
      return;
    }

    try {
      setLoading(true);
      setError('');
      const userData = await login(username, password);
      toast.success(`Welcome back, ${userData.fullName || userData.username}!`);

      if (userData.role === 'ADMIN') {
        navigate('/admin/dashboard');
      } else if (userData.role === 'INSTITUTION_OFFICER') {
        navigate('/officer/dashboard');
      } else {
        navigate('/complainant/dashboard');
      }
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Invalid username or password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="w-full max-w-md">
      <div className="bg-white rounded-2xl shadow-xl border border-slate-200 overflow-hidden">
        {/* Header */}
        <div className="bg-slate-900 px-8 py-6 text-center text-white">
          <div className="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-blue-600 shadow-md mb-3">
            <ShieldCheck className="w-6 h-6 text-white" />
          </div>
          <h2 className="text-lg font-bold tracking-tight">AICTE Grievance Portal</h2>
          <p className="text-xs text-slate-400 mt-1">
            Sign in to access your grievance monitoring dashboard
          </p>
        </div>

        {/* Demo Fast-Switch Buttons */}
        <div className="bg-slate-50 border-b border-slate-200 px-6 py-3">
          <p className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider mb-2 flex items-center">
            <Info className="w-3.5 h-3.5 mr-1 text-blue-500" />
            Quick Demo Login Accounts:
          </p>
          <div className="flex flex-wrap gap-1.5">
            <button
              type="button"
              onClick={() => handleDemoFill('admin', 'Password@123')}
              className="px-2.5 py-1 text-[11px] font-medium bg-rose-50 text-rose-700 border border-rose-200 rounded-md hover:bg-rose-100 transition-colors"
            >
              Central Admin
            </button>
            <button
              type="button"
              onClick={() => handleDemoFill('officer_iit', 'Password@123')}
              className="px-2.5 py-1 text-[11px] font-medium bg-indigo-50 text-indigo-700 border border-indigo-200 rounded-md hover:bg-indigo-100 transition-colors"
            >
              Officer (IIT)
            </button>
            <button
              type="button"
              onClick={() => handleDemoFill('officer_bits', 'Password@123')}
              className="px-2.5 py-1 text-[11px] font-medium bg-indigo-50 text-indigo-700 border border-indigo-200 rounded-md hover:bg-indigo-100 transition-colors"
            >
              Officer (BITS)
            </button>
            <button
              type="button"
              onClick={() => handleDemoFill('student_rohit', 'Password@123')}
              className="px-2.5 py-1 text-[11px] font-medium bg-blue-50 text-blue-700 border border-blue-200 rounded-md hover:bg-blue-100 transition-colors"
            >
              Student (Rohit)
            </button>
            <button
              type="button"
              onClick={() => handleDemoFill('parent_rajesh', 'Password@123')}
              className="px-2.5 py-1 text-[11px] font-medium bg-blue-50 text-blue-700 border border-blue-200 rounded-md hover:bg-blue-100 transition-colors"
            >
              Parent (Rajesh)
            </button>
          </div>
        </div>

        {/* Form */}
        <div className="p-6 sm:p-8">
          {error && (
            <div className="mb-4 p-3 rounded-lg bg-rose-50 border border-rose-200 text-rose-700 text-xs font-medium">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4 text-xs">
            <div>
              <label className="block font-semibold text-slate-700 mb-1">
                Username or Email
              </label>
              <div className="relative">
                <User className="w-4 h-4 absolute left-3 top-2.5 text-slate-400" />
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="admin or user email"
                  required
                  className="w-full pl-9 pr-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-brand-500"
                />
              </div>
            </div>

            <div>
              <label className="block font-semibold text-slate-700 mb-1">
                Password
              </label>
              <div className="relative">
                <Key className="w-4 h-4 absolute left-3 top-2.5 text-slate-400" />
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter password"
                  required
                  className="w-full pl-9 pr-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-brand-500"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full mt-2 inline-flex items-center justify-center px-4 py-2.5 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-sm transition-colors disabled:opacity-50"
            >
              <LogIn className="w-4 h-4 mr-1.5" />
              {loading ? 'Authenticating...' : 'Sign In'}
            </button>
          </form>

          {/* Links */}
          <div className="mt-6 pt-4 border-t border-slate-200 flex items-center justify-between text-xs text-slate-500">
            <Link
              to="/complainant/track"
              className="text-brand-600 hover:text-brand-700 font-medium"
            >
              Track Public Case →
            </Link>
            <Link
              to="/register"
              className="text-brand-600 hover:text-brand-700 font-medium"
            >
              Create Account
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
