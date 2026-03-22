import { useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import './App.css'

type AuthResponse = {
  token: string
  tokenType: string
  email: string
  role: string
}

type OtpResponse = {
  message: string
}

type AnalysisResponse = {
  topics: Array<{
    topicName: string
    confidenceScore: number
    weaknessLevel: 'NORMAL' | 'WEAK' | 'CRITICAL'
  }>
}

type TimetableResponse = {
  schedule: Array<{
    topicName: string
    allocatedHours: number
    reason: string
  }>
}

type Task = {
  id: string
  title: string
  done: boolean
}

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

function App() {
  const [mode, setMode] = useState<'signup' | 'signin'>('signup')
  const [authStep, setAuthStep] = useState<'form' | 'otp'>('form')

  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
  const [password, setPassword] = useState('')
  const [otp, setOtp] = useState('')

  const [token, setToken] = useState<string>(() => localStorage.getItem('study_token') ?? '')
  const [role, setRole] = useState<string>(() => localStorage.getItem('study_role') ?? '')
  const [studentId, setStudentId] = useState('')
  const [status, setStatus] = useState('Ready')

  const [analysis, setAnalysis] = useState<AnalysisResponse | null>(null)
  const [timetable, setTimetable] = useState<TimetableResponse | null>(null)
  const [tasks, setTasks] = useState<Task[]>([])

  const isLoggedIn = Boolean(token)

  const progressPercent = useMemo(() => {
    if (!analysis?.topics?.length) return 0
    const sum = analysis.topics.reduce((acc, item) => acc + item.confidenceScore, 0)
    return Math.round(sum / analysis.topics.length)
  }, [analysis])

  const completedTasks = tasks.filter((item) => item.done)
  const incompleteTasks = tasks.filter((item) => !item.done)

  const api = async <T,>(path: string, method = 'GET', body?: object, useAuth = false): Promise<T> => {
    const headers: Record<string, string> = { 'Content-Type': 'application/json' }
    if (useAuth && token) headers.Authorization = `Bearer ${token}`

    const response = await fetch(`${API_BASE}${path}`, {
      method,
      headers,
      body: body ? JSON.stringify(body) : undefined,
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: 'Request failed' }))
      throw new Error(errorData.message ?? 'Request failed')
    }

    return response.json() as Promise<T>
  }

  const sendOtp = async (event: FormEvent) => {
    event.preventDefault()
    try {
      if (mode === 'signup') {
        const data = await api<OtpResponse>('/auth/signup/send-otp', 'POST', {
          name,
          email,
          phoneNumber,
          password,
        })
        setStatus(data.message)
      } else {
        const data = await api<OtpResponse>('/auth/signin/send-otp', 'POST', {
          email,
          password,
        })
        setStatus(data.message)
      }
      setAuthStep('otp')
    } catch (error) {
      setStatus((error as Error).message)
    }
  }

  const verifyOtp = async (event: FormEvent) => {
    event.preventDefault()
    try {
      const path = mode === 'signup' ? '/auth/signup/verify' : '/auth/signin/verify'
      const data = await api<AuthResponse>(path, 'POST', { email, otp })

      setToken(data.token)
      setRole(data.role)
      localStorage.setItem('study_token', data.token)
      localStorage.setItem('study_role', data.role)
      setStatus(`Welcome ${data.email}`)
      setAuthStep('form')
      setOtp('')
    } catch (error) {
      setStatus((error as Error).message)
    }
  }

  const logout = () => {
    localStorage.removeItem('study_token')
    localStorage.removeItem('study_role')
    setToken('')
    setRole('')
    setStatus('Logged out')
    setAnalysis(null)
    setTimetable(null)
    setTasks([])
  }

  const loadDashboard = async () => {
    if (!studentId) {
      setStatus('Enter student ID first')
      return
    }

    try {
      const analysisData = await api<AnalysisResponse>(`/analysis/${studentId}`, 'GET', undefined, true)
      const timetableData = await api<TimetableResponse>(`/timetable/${studentId}`, 'GET', undefined, true)
      setAnalysis(analysisData)
      setTimetable(timetableData)

      const initialTasks: Task[] = timetableData.schedule.map((item, index) => ({
        id: `${item.topicName}-${index}`,
        title: `Study ${item.topicName} for ${item.allocatedHours} hour(s)`,
        done: false,
      }))
      setTasks(initialTasks)
      setStatus('Dashboard loaded')
    } catch (error) {
      setStatus((error as Error).message)
    }
  }

  const toggleTask = (taskId: string) => {
    setTasks((current) => current.map((item) => (item.id === taskId ? { ...item, done: !item.done } : item)))
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <div>
          <p className="brand">Smart Study Hub</p>
          <h1>AI Study Planner Dashboard</h1>
        </div>
        <div className="chips">
          <span>Role: {role || 'Guest'}</span>
          <span>Status: {status}</span>
          {isLoggedIn && (
            <button className="btn ghost" onClick={logout}>
              Logout
            </button>
          )}
        </div>
      </header>

      {!isLoggedIn ? (
        <section className="auth-card">
          <div className="auth-tabs">
            <button className={mode === 'signup' ? 'active' : ''} onClick={() => { setMode('signup'); setAuthStep('form') }}>
              Sign Up
            </button>
            <button className={mode === 'signin' ? 'active' : ''} onClick={() => { setMode('signin'); setAuthStep('form') }}>
              Sign In
            </button>
          </div>

          {authStep === 'form' ? (
            <form onSubmit={sendOtp} className="form-grid">
              {mode === 'signup' && (
                <input placeholder="Full Name" value={name} onChange={(e) => setName(e.target.value)} required />
              )}
              <input type="email" placeholder="Email ID" value={email} onChange={(e) => setEmail(e.target.value)} required />
              {mode === 'signup' && (
                <input
                  placeholder="Phone Number"
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                  required
                />
              )}
              <input
                type="password"
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
              <button className="btn" type="submit">
                Send OTP to Email
              </button>
            </form>
          ) : (
            <form onSubmit={verifyOtp} className="form-grid">
              <p className="hint">OTP sent to {email}. Enter 6-digit code.</p>
              <input
                placeholder="OTP"
                value={otp}
                onChange={(e) => setOtp(e.target.value)}
                maxLength={6}
                required
              />
              <button className="btn" type="submit">
                Verify OTP and Continue
              </button>
              <button className="btn ghost" type="button" onClick={() => setAuthStep('form')}>
                Back
              </button>
            </form>
          )}
        </section>
      ) : (
        <section className="dashboard">
          <article className="card controls">
            <h2>Home</h2>
            <p>Enter student ID to load timetable and progress.</p>
            <div className="inline">
              <input
                placeholder="Student ID"
                value={studentId}
                onChange={(e) => setStudentId(e.target.value)}
              />
              <button className="btn" onClick={loadDashboard}>
                Load Dashboard
              </button>
            </div>
          </article>

          <article className="card">
            <h2>Progress</h2>
            <p className="progress-value">{progressPercent}%</p>
            <div className="progress-bar">
              <div className="progress-fill" style={{ width: `${progressPercent}%` }} />
            </div>
          </article>

          <article className="card wide">
            <h2>Timetable</h2>
            <ul className="list">
              {timetable?.schedule?.length ? (
                timetable.schedule.map((item, index) => (
                  <li key={`${item.topicName}-${index}`}>
                    <strong>{item.topicName}</strong>
                    <span>{item.allocatedHours} hour(s)</span>
                    <small>{item.reason}</small>
                  </li>
                ))
              ) : (
                <li>No timetable loaded</li>
              )}
            </ul>
          </article>

          <article className="card">
            <h2>Completed Tasks</h2>
            <ul className="list tasks">
              {completedTasks.length ? (
                completedTasks.map((task) => (
                  <li key={task.id}>
                    <label>
                      <input type="checkbox" checked={task.done} onChange={() => toggleTask(task.id)} />
                      <span>{task.title}</span>
                    </label>
                  </li>
                ))
              ) : (
                <li>No completed tasks yet</li>
              )}
            </ul>
          </article>

          <article className="card">
            <h2>Incomplete Tasks</h2>
            <ul className="list tasks">
              {incompleteTasks.length ? (
                incompleteTasks.map((task) => (
                  <li key={task.id}>
                    <label>
                      <input type="checkbox" checked={task.done} onChange={() => toggleTask(task.id)} />
                      <span>{task.title}</span>
                    </label>
                  </li>
                ))
              ) : (
                <li>No pending tasks</li>
              )}
            </ul>
          </article>
        </section>
      )}
    </main>
  )
}

export default App
