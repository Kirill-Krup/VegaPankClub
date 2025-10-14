// Reuse auth/render from index.js via storage keys
const qs = (s, r = document) => r.querySelector(s);
const qsa = (s, r = document) => Array.from(r.querySelectorAll(s));

const STORAGE_KEYS = { user: 'vegapank:user' };
function getUser(){ try{ return JSON.parse(localStorage.getItem(STORAGE_KEYS.user) || 'null'); } catch{ return null; } }
function renderAuth(){
  const user = getUser();
  const guest = qs('[data-guest-section]');
  const u = qs('[data-user-section]');
  if (!guest || !u) return;
  if (user){
    guest.hidden = true; u.hidden = false;
    const nameEl = qs('[data-username]');
    const balEl = qs('[data-balance]');
    const avatar = qs('[data-avatar]');
    if (nameEl) nameEl.textContent = user.username || 'Гость';
    if (balEl) balEl.textContent = (user.balance ?? 0).toFixed(2);
    if (avatar && user.avatar) avatar.src = user.avatar;
  } else { guest.hidden = false; u.hidden = true; }
}

function attachAuthHandlers(){
  const loginBtn = qs('[data-login]');
  const regBtn = qs('[data-register]');
  const logoutBtn = qs('[data-logout]');
  function setUser(user){ user ? localStorage.setItem(STORAGE_KEYS.user, JSON.stringify(user)) : localStorage.removeItem(STORAGE_KEYS.user); }
  loginBtn?.addEventListener('click', () => {
    const username = prompt('Введите логин:', 'player1');
    if (!username) return;
    setUser({ username, balance: 15.5, avatar: `https://i.pravatar.cc/100?u=${encodeURIComponent(username)}` });
    renderAuth();
  });
  regBtn?.addEventListener('click', () => {
    const username = prompt('Придумайте логин:', 'newgamer');
    if (!username) return;
    setUser({ username, balance: 0, avatar: `https://i.pravatar.cc/100?u=${encodeURIComponent(username)}` });
    renderAuth();
  });
  logoutBtn?.addEventListener('click', () => { localStorage.removeItem(STORAGE_KEYS.user); renderAuth(); });
}

// Parallax for timeline items using data-parallax factor
function setupTimelineParallax(){
  const items = qsa('.timeline__item');
  if (items.length === 0) return;
  const onScroll = () => {
    const vh = window.innerHeight;
    items.forEach(el => {
      const rect = el.getBoundingClientRect();
      const progress = Math.min(1, Math.max(0, 1 - Math.abs(rect.top + rect.height/2 - vh/2) / (vh*0.75)));
      const factor = parseFloat(el.getAttribute('data-parallax') || '15');
      el.style.transform = `translateY(${(1-progress) * factor}px)`;
      if (progress > 0.15) el.classList.add('revealed');
    });
  };
  onScroll();
  window.addEventListener('scroll', onScroll, { passive: true });
}

// Mobile nav
function attachNavToggle(){
  const btn = qs('[data-nav-toggle]');
  const list = qs('[data-nav-list]');
  btn?.addEventListener('click', () => {
    const shown = list?.classList.toggle('show');
    if (btn && typeof shown === 'boolean') btn.setAttribute('aria-expanded', String(shown));
  });
}

// Footer year
function setYear(){ const y = qs('#year'); if (y) y.textContent = String(new Date().getFullYear()); }

document.addEventListener('DOMContentLoaded', () => {
  renderAuth();
  attachAuthHandlers();
  attachNavToggle();
  setupTimelineParallax();
  setYear();
  // Navigate to profile on clicking profile container
  const profileContainer = qs('.auth__profile');
  profileContainer?.addEventListener('click', () => { window.location.href = './profile.html'; });
});


