const qs = (s, r = document) => r.querySelector(s);
const qsa = (s, r = document) => Array.from(r.querySelectorAll(s));

let currentUser = null;

async function fetchUserProfile() {
  try {
    console.log('Fetching user profile...');

    const response = await fetch('/api/v1/profile/getProfile', {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include'
    });

    console.log('Profile response status:', response.status);

    if (!response.ok) {
      if (response.status === 401 || response.status === 403) {
        console.log('User not authenticated (401/403)');
        renderAuth(null);
        return null;
      }
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const userData = await response.json();
    console.log('User profile loaded:', userData);

    const userProfile = {
      username: userData.login || 'Пользователь',
      balance: userData.wallet || 0,
      avatar: userData.photoPath || `https://i.pravatar.cc/100?img=${Math.floor(Math.random() * 70)}`,
      id: userData.id,
      email: userData.email
    };

    renderAuth(userProfile);
    return userProfile;

  } catch (error) {
    console.error('Error fetching profile:', error);
    renderAuth(null);
    return null;
  }
}

function renderAuth(user) {
  const guest = qs('[data-guest-section]');
  const userSection = qs('[data-user-section]');

  if (!guest || !userSection) return;

  if (user) {
    console.log('Rendering user section:', user);
    guest.style.display = 'none';
    userSection.style.display = 'flex';

    const nameEl = qs('[data-username]');
    const balEl = qs('[data-balance]');
    const avatar = qs('[data-avatar]');

    if (nameEl) nameEl.textContent = user.username || 'Пользователь';
    if (balEl) balEl.textContent = (user.balance ?? 0).toFixed(2);
    if (avatar) avatar.src = user.avatar || `https://i.pravatar.cc/100?img=15`;
  } else {
    console.log('Rendering guest section');
    guest.style.display = 'flex';
    userSection.style.display = 'none';
  }
}


function attachAuthHandlers() {
  const loginBtn = qs('[data-login]');
  const regBtn = qs('[data-register]');
  const logoutBtn = qs('[data-logout]');

  regBtn?.addEventListener('click', () => {
    window.location.href = '/static/html/Authentication/registartion.html';
  });

  loginBtn?.addEventListener('click', () => {
    window.location.href = '/static/html/Authentication/login.html';
  });

  logoutBtn?.addEventListener('click', async () => {
    try {
      console.log('Logging out...');

      const res = await fetch('/api/v1/users/logout', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include'
      });

      console.log('Logout response:', res.status);

      currentUser = null;

      window.location.href = '/static/html/index.html';

    } catch (err) {
      console.error('Logout error:', err);
      currentUser = null;
      window.location.href = '/static/html/index.html';
    }
  });
}

function attachSmoothScroll() {
  qsa('[data-scroll]').forEach(a => {
    a.addEventListener('click', (e) => {
      const href = a.getAttribute('href');
      if (!href || !href.startsWith('#')) return;
      e.preventDefault();
      const target = qs(href);
      target?.scrollIntoView({ behavior: 'smooth', block: 'start' });
      qs('[data-nav-list]')?.classList.remove('show');
      const btn = qs('[data-nav-toggle]');
      if (btn) btn.setAttribute('aria-expanded', 'false');
    });
  });
}

function attachNavToggle() {
  const btn = qs('[data-nav-toggle]');
  const list = qs('[data-nav-list]');
  if (!btn || !list) return;
  btn.addEventListener('click', () => {
    const shown = list.classList.toggle('show');
    btn.setAttribute('aria-expanded', String(shown));
  });
}

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

function attachPricing() {
  qsa('[data-book]').forEach(btn => {
    btn.addEventListener('click', () => {
      const plan = btn.getAttribute('data-plan');
      alert(`Вы выбрали план: ${plan?.toUpperCase()}`);
    });
  });
}

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

function setYear() {
  const y = qs('#year');
  if (y) y.textContent = String(new Date().getFullYear());
}

async function initApp() {
  console.log('Initializing app...');

  await fetchUserProfile();

  attachAuthHandlers();
  attachSmoothScroll();
  attachNavToggle();
  setupReveals();
  attachParallax();
  attachPricing();
  attachContactForm();
  setYear();

  const profileContainer = qs('.auth__profile');
  profileContainer?.addEventListener('click', () => {
    window.location.href = '/static/html/profile.html';
  });

  console.log('App initialized');
}

document.addEventListener('DOMContentLoaded', initApp);
