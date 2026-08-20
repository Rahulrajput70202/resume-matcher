(() => {
  const API_BASE = '';
  const TOKEN_KEY = 'resumeMatcherToken';
  const USER_KEY = 'resumeMatcherUsername';
  const DIAL_CIRCUMFERENCE = 439.8;

  // ---------- DOM refs ----------
  const authScreen = document.getElementById('auth-screen');
  const appScreen = document.getElementById('app-screen');

  const tabLogin = document.getElementById('tab-login');
  const tabRegister = document.getElementById('tab-register');
  const loginForm = document.getElementById('login-form');
  const registerForm = document.getElementById('register-form');
  const authStatus = document.getElementById('auth-status');

  const currentUsernameEl = document.getElementById('current-username');
  const logoutBtn = document.getElementById('logout-btn');

  const analyzeForm = document.getElementById('analyze-form');
  const analyzeBtn = document.getElementById('analyze-btn');
  const jobTitleInput = document.getElementById('job-title');
  const jobDescriptionInput = document.getElementById('job-description');
  const resumeFileInput = document.getElementById('resume-file');
  const dropzone = document.getElementById('dropzone');
  const dropzoneFilename = document.getElementById('dropzone-filename');
  const consoleLog = document.getElementById('console-log');

  const reportEmpty = document.getElementById('report-empty');
  const reportContent = document.getElementById('report-content');
  const dialFill = document.getElementById('dial-fill');
  const dialScore = document.getElementById('dial-score');
  const metaFilename = document.getElementById('meta-filename');
  const metaJobtitle = document.getElementById('meta-jobtitle');
  const metaStatus = document.getElementById('meta-status');
  const matchedChips = document.getElementById('matched-chips');
  const missingChips = document.getElementById('missing-chips');
  const aiSection = document.getElementById('ai-section');
  const aiText = document.getElementById('ai-text');

  const historyEmpty = document.getElementById('history-empty');
  const historyList = document.getElementById('history-list');

  // ---------- Auth screen tab switching ----------
  tabLogin.addEventListener('click', () => switchTab('login'));
  tabRegister.addEventListener('click', () => switchTab('register'));

  function switchTab(which) {
    const isLogin = which === 'login';
    tabLogin.classList.toggle('active', isLogin);
    tabRegister.classList.toggle('active', !isLogin);
    tabLogin.setAttribute('aria-selected', String(isLogin));
    tabRegister.setAttribute('aria-selected', String(!isLogin));
    loginForm.classList.toggle('hidden', !isLogin);
    registerForm.classList.toggle('hidden', isLogin);
    authStatus.textContent = '';
  }

  // ---------- Auth requests ----------
  loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('login-username').value.trim();
    const password = document.getElementById('login-password').value;
    await doAuth('api/auth/login', { username, password });
  });

  registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('register-username').value.trim();
    const email = document.getElementById('register-email').value.trim();
    const password = document.getElementById('register-password').value;
    await doAuth('api/auth/register', { username, email, password });
  });

  async function doAuth(path, body) {
    setAuthStatus('Connecting…', false);
    try {
      const res = await fetch(API_BASE + path, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      });
      const data = await res.json().catch(() => ({}));

      if (!res.ok) {
        setAuthStatus(data.error || `Request failed (${res.status})`, true);
        return;
      }

      localStorage.setItem(TOKEN_KEY, data.token);
      localStorage.setItem(USER_KEY, data.username);
      enterApp(data.username);
    } catch (err) {
      setAuthStatus('Could not reach the server. Is it running?', true);
    }
  }

  function setAuthStatus(msg, isError) {
    authStatus.textContent = msg;
    authStatus.style.color = isError ? 'var(--rust)' : 'var(--slate-dim)';
  }

  // ---------- Session ----------
  function getToken() {
    return localStorage.getItem(TOKEN_KEY);
  }

  function enterApp(username) {
    authScreen.classList.add('hidden');
    appScreen.classList.remove('hidden');
    currentUsernameEl.textContent = username;
    loadHistory();
  }

  function exitApp() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    appScreen.classList.add('hidden');
    authScreen.classList.remove('hidden');
    loginForm.reset();
    registerForm.reset();
    setAuthStatus('', false);
  }

  logoutBtn.addEventListener('click', exitApp);

  (function bootstrap() {
    const token = getToken();
    const username = localStorage.getItem(USER_KEY);
    if (token && username) {
      enterApp(username);
    }
  })();

  // ---------- Dropzone ----------
  dropzone.addEventListener('click', () => resumeFileInput.click());
  dropzone.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      resumeFileInput.click();
    }
  });
  resumeFileInput.addEventListener('change', () => {
    if (resumeFileInput.files[0]) showSelectedFile(resumeFileInput.files[0]);
  });
  ['dragover', 'dragenter'].forEach(evt =>
    dropzone.addEventListener(evt, (e) => { e.preventDefault(); dropzone.classList.add('drag-over'); })
  );
  ['dragleave', 'drop'].forEach(evt =>
    dropzone.addEventListener(evt, (e) => { e.preventDefault(); dropzone.classList.remove('drag-over'); })
  );
  dropzone.addEventListener('drop', (e) => {
    const file = e.dataTransfer.files[0];
    if (file && file.type === 'application/pdf') {
      resumeFileInput.files = e.dataTransfer.files;
      showSelectedFile(file);
    } else {
      logMessage('Only PDF files are accepted.', true);
    }
  });
  function showSelectedFile(file) {
    dropzoneFilename.textContent = file.name;
  }

  // ---------- Analyze ----------
  analyzeForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const file = resumeFileInput.files[0];
    if (!file) {
      logMessage('Select a resume PDF first.', true);
      return;
    }

    const formData = new FormData();
    formData.append('resume', file);
    formData.append('jobTitle', jobTitleInput.value.trim());
    formData.append('jobDescription', jobDescriptionInput.value.trim());

    setAnalyzing(true);
    logMessage('Scanning…', false);

    try {
      const res = await fetch(API_BASE + 'api/resume/analyze', {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + getToken() },
        body: formData
      });

      if (res.status === 401) {
        logMessage('Session expired. Please log in again.', true);
        exitApp();
        return;
      }

      const data = await res.json().catch(() => ({}));

      if (!res.ok) {
        logMessage(data.error || `Scan failed (${res.status})`, true);
        return;
      }

      logMessage('Scan complete.', false, true);
      renderReport(data);
      loadHistory();
    } catch (err) {
      logMessage('Could not reach the server. Is it running?', true);
    } finally {
      setAnalyzing(false);
    }
  });

  function setAnalyzing(isBusy) {
    analyzeBtn.disabled = isBusy;
    analyzeBtn.textContent = isBusy ? 'Scanning…' : 'Run scan';
  }

  function logMessage(msg, isError, isSuccess) {
    consoleLog.textContent = msg;
    consoleLog.classList.toggle('log-error', !!isError);
    consoleLog.classList.toggle('log-success', !!isSuccess);
  }

  // ---------- Report rendering ----------
  function renderReport(analysis) {
    reportEmpty.classList.add('hidden');
    reportContent.classList.remove('hidden');

    const score = Math.max(0, Math.min(100, analysis.matchScore || 0));
    animateDial(score);

    metaFilename.textContent = analysis.fileName || '—';
    metaJobtitle.textContent = analysis.jobTitle || '—';
    metaStatus.textContent = statusLabel(score);

    renderChips(matchedChips, analysis.matchedKeywords, 'chip-matched', 'No matched skills detected.');
    renderChips(missingChips, analysis.missingKeywords, 'chip-gap', 'No gaps detected — strong match.');

    if (analysis.aiSuggestions) {
      aiSection.classList.remove('hidden');
      aiText.textContent = analysis.aiSuggestions;
    } else {
      aiSection.classList.add('hidden');
      aiText.textContent = '';
    }
  }

  function statusLabel(score) {
    if (score >= 75) return 'Strong match';
    if (score >= 45) return 'Partial match';
    return 'Weak match';
  }

  function animateDial(score) {
    const offset = DIAL_CIRCUMFERENCE - (DIAL_CIRCUMFERENCE * score) / 100;
    dialFill.style.stroke = score >= 75 ? 'var(--teal)' : score >= 45 ? 'var(--amber)' : 'var(--rust)';
    // reset then animate on next frame so the transition always plays
    dialFill.style.strokeDashoffset = String(DIAL_CIRCUMFERENCE);
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        dialFill.style.strokeDashoffset = String(offset);
      });
    });

    let current = 0;
    const target = Math.round(score);
    const step = () => {
      current += Math.max(1, Math.round((target - current) / 6));
      if (current >= target) {
        dialScore.textContent = target;
        return;
      }
      dialScore.textContent = current;
      requestAnimationFrame(step);
    };
    requestAnimationFrame(step);
  }

  function renderChips(container, items, className, emptyText) {
    container.innerHTML = '';
    if (!items || items.length === 0) {
      const span = document.createElement('span');
      span.className = 'chip chip-empty';
      span.textContent = emptyText;
      container.appendChild(span);
      return;
    }
    items.forEach((kw, i) => {
      const span = document.createElement('span');
      span.className = 'chip ' + className;
      span.textContent = kw;
      span.style.animationDelay = (i * 30) + 'ms';
      container.appendChild(span);
    });
  }

  // ---------- History ----------
  async function loadHistory() {
    try {
      const res = await fetch(API_BASE + 'api/resume/history', {
        headers: { 'Authorization': 'Bearer ' + getToken() }
      });
      if (res.status === 401) {
        exitApp();
        return;
      }
      const items = await res.json();
      renderHistory(items);
    } catch (err) {
      // history is non-critical; fail silently in the UI, keep console log for the analyze action only
    }
  }

  function renderHistory(items) {
    historyList.innerHTML = '';
    if (!items || items.length === 0) {
      historyEmpty.classList.remove('hidden');
      return;
    }
    historyEmpty.classList.add('hidden');

    items.forEach((item) => {
      const row = document.createElement('div');
      row.className = 'history-row';

      const info = document.createElement('div');
      const title = document.createElement('div');
      title.className = 'history-title';
      title.textContent = item.jobTitle || 'Untitled scan';
      const sub = document.createElement('div');
      sub.className = 'history-sub';
      sub.textContent = item.fileName || '';
      info.appendChild(title);
      info.appendChild(sub);

      const score = document.createElement('span');
      score.className = 'history-score';
      score.textContent = Math.round(item.matchScore) + '%';

      const date = document.createElement('span');
      date.className = 'history-date';
      date.textContent = formatDate(item.createdAt);

      const del = document.createElement('button');
      del.className = 'history-delete';
      del.setAttribute('aria-label', 'Delete this scan');
      del.textContent = '✕';
      del.addEventListener('click', (e) => {
        e.stopPropagation();
        deleteAnalysis(item.id);
      });

      row.appendChild(info);
      row.appendChild(score);
      row.appendChild(date);
      row.appendChild(del);

      row.addEventListener('click', () => renderReport(item));

      historyList.appendChild(row);
    });
  }

  function formatDate(iso) {
    if (!iso) return '';
    const d = new Date(iso);
    if (isNaN(d.getTime())) return '';
    return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' }) +
      ' ' + d.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' });
  }

  async function deleteAnalysis(id) {
    try {
      const res = await fetch(API_BASE + 'api/resume/' + id, {
        method: 'DELETE',
        headers: { 'Authorization': 'Bearer ' + getToken() }
      });
      if (res.status === 401) {
        exitApp();
        return;
      }
      loadHistory();
    } catch (err) {
      // non-critical
    }
  }
})();
