document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('loginForm');
  const loginInput = document.getElementById('login');
  const passwordInput = document.getElementById('password');
  const togglePwd = document.getElementById('togglePwd');
  const globalError = document.getElementById('globalError');

  if (togglePwd) {
    togglePwd.addEventListener('click', () => {
      const type = passwordInput.type === 'password' ? 'text' : 'password';
      passwordInput.type = type;
      togglePwd.innerHTML = type === 'password' ? '<i class="fas fa-eye"></i>' : '<i class="fas fa-eye-slash"></i>';
    });
  }

  form?.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearErrors();

    const login = loginInput.value.trim();
    const password = passwordInput.value.trim();

    let hasError = false;
    if (!login) { setFieldError('login', 'Введите логин или email'); hasError = true; }
    if (!password) { setFieldError('password', 'Введите пароль'); hasError = true; }
    if (hasError) return;

    try {
      const res = await fetch('/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ login, password, rememberMe: document.getElementById('rememberMe')?.checked })
      });
      if (!res.ok) {
        const data = await safeJson(res);
        throw new Error(data?.message || 'Не удалось войти');
      }
      console.log(res);
      window.location.href = '/static/html/index.html';
    } catch (err) {
      globalError.textContent = err.message || 'Ошибка авторизации';
    }
  });
});

function setFieldError(field, message) {
  const el = document.querySelector(`.form-error[data-for="${field}"]`);
  if (el) el.textContent = message;
}

function clearErrors() {
  document.querySelectorAll('.form-error').forEach((e) => (e.textContent = ''));
  const globalError = document.getElementById('globalError');
  if (globalError) globalError.textContent = '';
}

async function safeJson(res) {
  try { return await res.json(); } catch { return null; }
}


