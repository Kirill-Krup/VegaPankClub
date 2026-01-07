// Reuse auth/render from index.js via storage keys
const qs = (s, r = document) => r.querySelector(s);
const qsa = (s, r = document) => Array.from(r.querySelectorAll(s));

const STORAGE_KEYS = { user: 'vegapank:user' };
const AVATAR_KEY = 'vegapank_user_avatar_url';

let currentUser = null;

// Функция для получения профиля с сервера
async function fetchUserProfile() {
  try {
    const response = await fetch('/api/v1/profile/getProfile', {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include'
    });

    if (!response.ok) {
      if (response.status === 401 || response.status === 403) {
        // Пользователь не авторизован
        localStorage.removeItem(STORAGE_KEYS.user);
        return null;
      }
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const userData = await response.json();

    // Проверки блокировки и админки
    if (userData.banned === true || userData.isBanned === true) {
      window.location.href = "/static/html/YouAreBlocked.html";
      return null;
    }
    if (userData.role === 2) {
      window.location.href = "/static/html/admin.html";
      return null;
    }

    // Определяем аватарку
    let avatarUrl = null;
    if (userData.photoPath && userData.photoPath.trim().length > 0) {
      avatarUrl = userData.photoPath;
    }

    // Сохраняем в localStorage только если есть аватарка
    try {
      if (avatarUrl) {
        localStorage.setItem(AVATAR_KEY, avatarUrl);
      } else {
        localStorage.removeItem(AVATAR_KEY);
      }
    } catch (e) {
      console.warn('Cannot write avatar to localStorage:', e);
    }

    const userProfile = {
      id: userData.id,
      username: userData.login || 'Пользователь',
      balance: userData.wallet || 0,
      avatar: avatarUrl, // Может быть null
      email: userData.email
    };

    // Сохраняем в локальное хранилище
    localStorage.setItem(STORAGE_KEYS.user, JSON.stringify(userProfile));
    currentUser = userProfile;

    return userProfile;

  } catch (error) {
    console.error('Error fetching profile:', error);
    return null;
  }
}

// Функция для получения пользователя (сначала пробуем с сервера, потом из localStorage)
async function getUser() {
  // Если уже есть currentUser, возвращаем его
  if (currentUser) return currentUser;

  // Пробуем получить с сервера
  const serverUser = await fetchUserProfile();
  if (serverUser) return serverUser;

  // Если не удалось получить с сервера, пробуем из localStorage
  try {
    const localUser = JSON.parse(localStorage.getItem(STORAGE_KEYS.user) || 'null');
    return localUser;
  } catch {
    return null;
  }
}

// Обновленная функция renderAuth
async function renderAuth() {
  const user = await getUser();
  const guest = qs('[data-guest-section]');
  const u = qs('[data-user-section]');

  if (!guest || !u) return;

  if (user) {
    // ПОЛЬЗОВАТЕЛЬ ВОШЕЛ: скрываем кнопки "Вход" и "Регистрация"
    guest.style.display = 'none'; // ИЛИ guest.hidden = true;
    guest.style.visibility = 'hidden'; // Дополнительно
    guest.style.opacity = '0'; // Дополнительно

    // Показываем блок пользователя
    u.style.display = 'flex';
    u.hidden = false;

    const nameEl = qs('[data-username]');
    const balEl = qs('[data-balance]');
    const avatar = qs('[data-avatar]');

    if (nameEl) nameEl.textContent = user.username || 'Гость';
    if (balEl) balEl.textContent = (user.balance ?? 0).toFixed(2);

    // Обработка аватарки
    if (avatar) {
      // Проверяем наличие валидной аватарки
      const hasValidAvatar = user.avatar && user.avatar.trim().length > 0;

      if (hasValidAvatar) {
        avatar.src = user.avatar;
        avatar.style.display = 'block';
        if (avatar.parentElement) {
          avatar.parentElement.classList.remove('no-avatar');
        }
      } else {
        // Если нет аватарки - скрываем картинку
        avatar.style.display = 'none';
        if (avatar.parentElement) {
          avatar.parentElement.classList.add('no-avatar');
        }
      }
    }
  } else {
    // ПОЛЬЗОВАТЕЛЬ НЕ ВОШЕЛ: показываем кнопки "Вход" и "Регистрация"
    guest.style.display = 'flex';
    guest.style.visibility = 'visible';
    guest.style.opacity = '1';
    guest.hidden = false;

    // Скрываем блок пользователя
    u.style.display = 'none';
    u.hidden = true;

    // Убедимся, что удалили данные пользователя
    localStorage.removeItem(STORAGE_KEYS.user);
  }
}

// Обновленная функция для установки пользователя
function setUser(user) {
  if (user) {
    currentUser = user;
    localStorage.setItem(STORAGE_KEYS.user, JSON.stringify(user));

    // Сохраняем аватарку в отдельный ключ
    if (user.avatar) {
      try {
        localStorage.setItem(AVATAR_KEY, user.avatar);
      } catch (e) {
        console.warn('Cannot save avatar to localStorage:', e);
      }
    }
  } else {
    currentUser = null;
    localStorage.removeItem(STORAGE_KEYS.user);
    localStorage.removeItem(AVATAR_KEY);
  }
}

// Обновленные обработчики авторизации
function attachAuthHandlers() {
  const loginBtn = qs('[data-login]');
  const regBtn = qs('[data-register]');
  const logoutBtn = qs('[data-logout]');

  loginBtn?.addEventListener('click', async () => {
    window.location.href = '/static/html/Authentication/login.html';
  });

  regBtn?.addEventListener('click', () => {
    window.location.href = '/static/html/Authentication/registration.html';
  });

  logoutBtn?.addEventListener('click', async () => {
    try {
      await fetch('/api/v1/users/logout', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include'
      });
    } catch (err) {
      console.error('Logout error:', err);
    }

    // Очищаем локальные данные
    setUser(null);
    currentUser = null;

    // Обновляем отображение (кнопки появятся)
    await renderAuth();

    // Перенаправляем на главную
    window.location.href = '/static/html/index.html';
  });
}

// Parallax for timeline items using data-parallax factor
function setupTimelineParallax() {
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
function attachNavToggle() {
  const btn = qs('[data-nav-toggle]');
  const list = qs('[data-nav-list]');

  btn?.addEventListener('click', () => {
    const shown = list?.classList.toggle('show');
    if (btn && typeof shown === 'boolean') {
      btn.setAttribute('aria-expanded', String(shown));
    }
  });
}

// Footer year
function setYear() {
  const y = qs('#year');
  if (y) y.textContent = String(new Date().getFullYear());
}

// Функция для инициализации приложения
async function initApp() {
  // Сначала рендерим авторизацию
  await renderAuth();

  // Затем добавляем обработчики
  attachAuthHandlers();
  attachNavToggle();
  setupTimelineParallax();
  setYear();

  // Навигация на профиль при клике
  const profileContainer = qs('.auth__profile');
  profileContainer?.addEventListener('click', () => {
    window.location.href = './profile.html';
  });
}

document.addEventListener('DOMContentLoaded', initApp);