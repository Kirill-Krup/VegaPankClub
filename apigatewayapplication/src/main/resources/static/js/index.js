// Utilities
const qs = (s, r = document) => r.querySelector(s);
const qsa = (s, r = document) => Array.from(r.querySelectorAll(s));

// State (mock auth for demo)
const STORAGE_KEYS = { user: 'vegapank:user' };

function getUser() {
  try { return JSON.parse(localStorage.getItem(STORAGE_KEYS.user) || 'null'); } catch { return null; }
}
function setUser(user) {
  if (user) localStorage.setItem(STORAGE_KEYS.user, JSON.stringify(user));
  else localStorage.removeItem(STORAGE_KEYS.user);
}

// Auth / profile toggle
function renderAuth() {
  const user = getUser();
  const guest = qs('[data-guest-section]');
  const u = qs('[data-user-section]');
  if (!guest || !u) return;
  if (user) {
    guest.hidden = true;
    u.hidden = false;
    const nameEl = qs('[data-username]');
    const balEl = qs('[data-balance]');
    const avatar = qs('[data-avatar]');
    if (nameEl) nameEl.textContent = user.username || 'Гость';
    if (balEl) balEl.textContent = (user.balance ?? 0).toFixed(2);
    if (avatar && user.avatar) avatar.src = user.avatar;
  } else {
    guest.hidden = false;
    u.hidden = true;
  }
}

// Auth handlers - redirect to registration page
function attachAuthHandlers() {
  const loginBtn = qs('[data-login]');
  const regBtn = qs('[data-register]');
  const logoutBtn = qs('[data-logout]');

  regBtn?.addEventListener('click', () => {
    window.location.href = '/static/html/Authentication/registartion.html';
  });

  // Redirect to login page (if needed)
  loginBtn?.addEventListener('click', () => {
    window.location.href = '/static/html/Authentication/login.html';
  });

  logoutBtn?.addEventListener('click', async () => {
    try {
      const res = await fetch('/api/v1/users/test', {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include'
      });

      if (!res.ok) {
        throw new Error(`HTTP error! status: ${res.status}`);
      }

      const data = await res.json();
      console.log('Logout test response:', data);
      window.location.href = '/static/html/index.html';

    } catch (err) {
      console.error('Logout error:', err);
      window.location.href = '/static/html/index.html';
    }
  });
}

// Smooth scroll
function attachSmoothScroll() {
  qsa('[data-scroll]').forEach(a => {
    a.addEventListener('click', (e) => {
      const href = a.getAttribute('href');
      if (!href || !href.startsWith('#')) return;
      e.preventDefault();
      const target = qs(href);
      target?.scrollIntoView({ behavior: 'smooth', block: 'start' });
      // Close mobile menu if open
      qs('[data-nav-list]')?.classList.remove('show');
      const btn = qs('[data-nav-toggle]');
      if (btn) btn.setAttribute('aria-expanded', 'false');
    });
  });
}

// Mobile nav toggle
function attachNavToggle() {
  const btn = qs('[data-nav-toggle]');
  const list = qs('[data-nav-list]');
  if (!btn || !list) return;
  btn.addEventListener('click', () => {
    const shown = list.classList.toggle('show');
    btn.setAttribute('aria-expanded', String(shown));
  });
}

// Intersection-based reveal animations
function setupReveals() {
  const items = qsa('.reveal');
  if (!('IntersectionObserver' in window) || items.length === 0) {
    items.forEach(i => i.classList.add('revealed'));
    return;
  }
  const obs = new IntersectionObserver((entries, o) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('revealed');
        o.unobserve(entry.target);
      }
    });
  }, { threshold: .12 });
  items.forEach(i => obs.observe(i));
}

// Parallax-ish hero glow
function attachParallax() {
  const glow = qs('.hero__glow');
  if (!glow) return;
  let raf = null;
  window.addEventListener('mousemove', (e) => {
    if (raf) cancelAnimationFrame(raf);
    raf = requestAnimationFrame(() => {
      const x = (e.clientX / window.innerWidth - .5) * 18;
      const y = (e.clientY / window.innerHeight - .5) * 18;
      glow.style.transform = `translate(${x}px, ${y}px)`;
    });
  });
}

// Pricing selection demo
function attachPricing() {
  qsa('[data-book]').forEach(btn => {
    btn.addEventListener('click', () => {
      const plan = btn.getAttribute('data-plan');
      alert(`Вы выбрали план: ${plan?.toUpperCase()}`);
    });
  });
}

// Contact form demo submit
function attachContactForm() {
  const form = qs('[data-contact-form]');
  form?.addEventListener('submit', (e) => {
    e.preventDefault();
    const data = new FormData(form);
    const name = data.get('name');
    alert(`Спасибо, ${name}! Мы скоро свяжемся.`);
    form.reset();
  });
}

// Footer year
function setYear() {
  const y = qs('#year');
  if (y) y.textContent = String(new Date().getFullYear());
}

// Init
document.addEventListener('DOMContentLoaded', () => {
  renderAuth();
  attachAuthHandlers();
  attachSmoothScroll();
  attachNavToggle();
  setupReveals();
  attachParallax();
  attachPricing();
  attachContactForm();
  setYear();
  // Navigate to profile on clicking profile container
  const profileContainer = qs('.auth__profile');
  profileContainer?.addEventListener('click', () => {
    const href = './profile.html';
    window.location.href = href;
  });
});