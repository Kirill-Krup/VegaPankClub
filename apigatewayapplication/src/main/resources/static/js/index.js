// index.js – главная страница

// Утилиты для поиска элементов
const qs = (selector, root = document) => root.querySelector(selector);
const qsa = (selector, root = document) => Array.from(root.querySelectorAll(selector));

// Ключ для аватарки в localStorage
const AVATAR_KEY = 'vegapank_user_avatar_url';

let currentUser = null;

/* =========================
 * Работа с профилем / авторизацией
 * ========================= */

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

    // Проверки блокировки и админки
    if (userData.banned === true || userData.isBanned === true) {
      window.location.href = "/static/html/YouAreBlocked.html";
      return null;
    }
    if (userData.role === 2) {
      window.location.href = "/static/html/admin.html";
      return null;
    }

    // URL из бэкенда
    const backendAvatar = userData.photoPath && userData.photoPath.trim().length > 0
        ? userData.photoPath
        : null;

    // То, что уже лежит в localStorage
    let storedAvatar = null;
    try {
      storedAvatar = localStorage.getItem(AVATAR_KEY);
    } catch (e) {
      console.warn('Cannot read avatar from localStorage:', e);
    }

    // Финальный URL для аватарки
    const finalAvatar =
        backendAvatar ||
        storedAvatar ||
        `https://i.pravatar.cc/100?img=${Math.floor(Math.random() * 70)}`;

    // Обновляем localStorage актуальным значением
    try {
      localStorage.setItem(AVATAR_KEY, finalAvatar);
    } catch (e) {
      console.warn('Cannot write avatar to localStorage:', e);
    }

    const userProfile = {
      id: userData.id,
      username: userData.login,
      balance: userData.wallet || 0,
      avatar: finalAvatar,
      email: userData.email
    };

    currentUser = userProfile;
    renderAuth(userProfile);
    return userProfile;

  } catch (error) {
    console.error('Error fetching profile:', error);
    renderAuth(null);
    return null;
  }
}

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

    // Проверки блокировки и админки
    if (userData.banned === true || userData.isBanned === true) {
      window.location.href = "/static/html/YouAreBlocked.html";
      return null;
    }
    if (userData.role === 2) {
      window.location.href = "/static/html/admin.html";
      return null;
    }

    let finalAvatar = null;
    if (userData.photoPath && userData.photoPath.trim().length > 0) {
      finalAvatar = userData.photoPath;
      try {
        localStorage.setItem(AVATAR_KEY, finalAvatar);
      } catch (e) {
        console.warn('Cannot write avatar to localStorage:', e);
      }
    } else {
      // Если фото нет, удаляем из localStorage
      try {
        localStorage.removeItem(AVATAR_KEY);
      } catch (e) {
        console.warn('Cannot remove avatar from localStorage:', e);
      }
    }

    const userProfile = {
      id: userData.id,
      username: userData.login,
      balance: userData.wallet || 0,
      avatar: finalAvatar, // Может быть null
      email: userData.email
    };

    currentUser = userProfile;
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
    const avatarEl = qs('[data-avatar]');

    if (nameEl) {
      nameEl.textContent = user.username || 'Пользователь';
    }
    if (balEl) {
      balEl.textContent = (user.balance ?? 0).toFixed(2);
    }

    if (avatarEl) {
      // Если есть валидная аватарка
      if (user.avatar && user.avatar.trim().length > 0) {
        avatarEl.src = user.avatar;
        avatarEl.style.display = 'block';
        avatarEl.parentElement.classList.remove('no-avatar');
      } else {
        // Если аватарки нет - скрываем картинку и показываем иконку
        avatarEl.style.display = 'none';
        avatarEl.parentElement.classList.add('no-avatar');
      }
    }
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
      const response = await fetch('/api/v1/auth/logout', {
        method: 'POST',
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error('Logout failed');
      }

      // При логауте чистим аватар из localStorage
      try {
        localStorage.removeItem(AVATAR_KEY);
      } catch (e) {
        console.warn('Cannot clear avatar from localStorage:', e);
      }

      window.location.href = '/static/html/index.html';
    } catch (err) {
      console.error('Logout error:', err);
    }
  });
}

/* =========================
 * Навигация и скролл
 * ========================= */

function attachSmoothScroll() {
  qsa('[data-scroll]').forEach((a) => {
    a.addEventListener('click', (e) => {
      const href = a.getAttribute('href');
      if (!href || !href.startsWith('#')) return;

      e.preventDefault();
      const target = qs(href);
      target?.scrollIntoView({ behavior: 'smooth', block: 'start' });

      const list = qs('[data-nav-list]');
      const btn = qs('[data-nav-toggle]');
      list?.classList.remove('show');
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

/* =========================
 * Анимации и визуальные эффекты
 * ========================= */

function setupReveals() {
  const items = qsa('.reveal');
  if (!('IntersectionObserver' in window) || items.length === 0) {
    items.forEach((i) => i.classList.add('revealed'));
    return;
  }

  const obs = new IntersectionObserver(
      (entries, o) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('revealed');
            o.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.12 }
  );

  items.forEach((i) => obs.observe(i));
}

function attachParallax() {
  const glow = qs('.hero__glow');
  if (!glow) return;

  let raf = null;
  window.addEventListener('mousemove', (e) => {
    if (raf) cancelAnimationFrame(raf);
    raf = requestAnimationFrame(() => {
      const x = (e.clientX / window.innerWidth - 0.5) * 18;
      const y = (e.clientY / window.innerHeight - 0.5) * 18;
      glow.style.transform = `translate(${x}px, ${y}px)`;
    });
  });
}

/* =========================
 * Тарифы
 * ========================= */

async function fetchPopularTariffs() {
  try {
    const response = await fetch('/api/v1/tariffs/getPopularTariff', {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const tariffs = await response.json();
    console.log('Popular tariffs loaded:', tariffs);
    renderTariffs(tariffs);
    return tariffs;
  } catch (error) {
    console.error('Error fetching popular tariffs:', error);
    // При ошибке оставляем статические тарифы из верстки
    return [];
  }
}

function renderTariffs(tariffs) {
  const pricingContainer = qs('#pricing .grid.grid--3.pricing');
  if (!pricingContainer || !tariffs || tariffs.length === 0) {
    return;
  }

  pricingContainer.innerHTML = '';

  const tariffsToShow = tariffs.slice(0, 3);

  tariffsToShow.forEach((tariff, index) => {
    const pricePerHour =
        tariff.hours > 0
            ? (parseFloat(tariff.price) / tariff.hours).toFixed(2)
            : parseFloat(tariff.price).toFixed(2);

    const isPopular = index === 1;

    const tariffCard = document.createElement('article');
    tariffCard.className = `price reveal ${isPopular ? 'price--popular' : ''}`;
    tariffCard.innerHTML = `
      ${isPopular ? '<div class="price__badge">Популярно</div>' : ''}
      <h3 class="price__title">${tariff.name || 'Тариф'}</h3>
      <div class="price__value">${pricePerHour} BYN/час</div>
      <ul class="price__list">
        <li>${tariff.hours} ${getHoursText(tariff.hours)}</li>
        <li>${tariff.isVip || tariff.vip ? 'VIP зона' : 'Стандартная зона'}</li>
        <li>${parseFloat(tariff.price).toFixed(2)} BYN за пакет</li>
      </ul>
      <button class="btn btn--primary" data-book data-plan="${
        tariff.name?.toLowerCase() || 'tariff'
    }">Выбрать</button>
    `;

    pricingContainer.appendChild(tariffCard);
  });

  setTimeout(() => {
    qsa('#pricing .reveal').forEach((el) => el.classList.add('revealed'));
  }, 100);

  attachPricing();
}

function getHoursText(hours) {
  if (hours === 1) return 'час';
  if (hours >= 2 && hours <= 4) return 'часа';
  return 'часов';
}

function attachPricing() {
  qsa('[data-book]').forEach((btn) => {
    btn.addEventListener('click', () => {
      const plan = btn.getAttribute('data-plan');
      console.log('Selected plan:', plan);
      window.location.href = '/static/html/booking.html';
    });
  });
}

/* =========================
 * Контакты и футер
 * ========================= */

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

/* =========================
 * Инициализация
 * ========================= */

async function initApp() {
  console.log('Initializing app...');

  await fetchUserProfile();
  await fetchPopularTariffs();

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
