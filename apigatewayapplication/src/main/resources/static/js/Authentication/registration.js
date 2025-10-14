document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('regForm');
  const firstName = document.getElementById('firstName');
  const lastName = document.getElementById('lastName');
  const login = document.getElementById('login');
  const email = document.getElementById('email');
  const password = document.getElementById('password');
  const confirm = document.getElementById('confirm');
  const phone = document.getElementById('phone');
  const terms = document.getElementById('terms');
  const globalError = document.getElementById('globalError');

  const togglePwd = document.getElementById('togglePwd');
  const toggleConfirm = document.getElementById('toggleConfirm');
  togglePwd?.addEventListener('click', () => togglePassword(password, togglePwd));
  toggleConfirm?.addEventListener('click', () => togglePassword(confirm, toggleConfirm));

  form?.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearErrors();

    const payload = {
      firstName: firstName.value.trim(),
      lastName: lastName.value.trim(),
      login: login.value.trim(),
      email: email.value.trim(),
      password: password.value.trim(),
      confirm: confirm.value.trim(),
      phone: phone.value.trim()
    };

    const errors = validate(payload, terms?.checked);
    if (Object.keys(errors).length) {
      for (const [k, v] of Object.entries(errors)) setFieldError(k, v);
      return;
    }

    try {
      const res = await fetch('/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          firstName: payload.firstName,
          lastName: payload.lastName,
          login: payload.login,
          email: payload.email,
          password: payload.password,
          phone: payload.phone
        })
      });
      if (!res.ok) {
        const data = await safeJson(res);
        throw new Error(data?.message || 'Не удалось зарегистрироваться');
      }
      window.location.href = './login.html';
    } catch (err) {
      if (globalError) globalError.textContent = err.message || 'Ошибка регистрации';
    }
  });
});

function validate(p, termsChecked) {
  const errors = {};
  if (!p.firstName) errors.firstName = 'Введите имя';
  if (!p.lastName) errors.lastName = 'Введите фамилию';
  if (!p.login) errors.login = 'Введите логин';
  if (!p.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(p.email)) errors.email = 'Введите корректный email';
  if (!p.password || p.password.length < 8) errors.password = 'Минимум 8 символов';
  if (p.password !== p.confirm) errors.confirm = 'Пароли не совпадают';
  if (p.phone && !/^\+?\d[\d\s\-()]{6,}$/.test(p.phone)) errors.phone = 'Введите корректный телефон';
  if (!termsChecked) errors.terms = 'Подтвердите согласие с условиями';
  return errors;
}

function togglePassword(input, btn) {
  const type = input.type === 'password' ? 'text' : 'password';
  input.type = type;
  btn.innerHTML = type === 'password' ? '<i class="fas fa-eye"></i>' : '<i class="fas fa-eye-slash"></i>';
}

function setFieldError(field, message) {
  const el = document.querySelector(`.form-error[data-for="${field}"]`);
  if (el) el.textContent = message;
}

function clearErrors() {
  document.querySelectorAll('.form-error').forEach((e) => (e.textContent = ''));
  const globalError = document.getElementById('globalError');
  if (globalError) globalError.textContent = '';
}

async function safeJson(res) { try { return await res.json(); } catch { return null; } }


