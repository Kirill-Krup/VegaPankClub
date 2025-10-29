document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('regForm');
  const firstName = document.getElementById('firstName');
  const lastName = document.getElementById('lastName');
  const login = document.getElementById('login');
  const email = document.getElementById('email');
  const password = document.getElementById('password');
  const confirm = document.getElementById('confirm');
  const phone = document.getElementById('phone');
  const birthDate = document.getElementById('birthDate'); // Добавлено поле даты рождения
  const terms = document.getElementById('terms');
  const globalError = document.getElementById('globalError');

  const togglePwd = document.getElementById('togglePwd');
  const toggleConfirm = document.getElementById('toggleConfirm');
  togglePwd?.addEventListener('click', () => togglePassword(password, togglePwd));
  toggleConfirm?.addEventListener('click', () => togglePassword(confirm, toggleConfirm));

  // Устанавливаем максимальную дату как вчерашний день
  if (birthDate) {
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    birthDate.max = yesterday.toISOString().split('T')[0];
  }

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
      phone: phone.value.trim(),
      birthDate: birthDate.value // Добавлено значение даты рождения
    };

    const errors = validate(payload, terms?.checked);
    if (Object.keys(errors).length) {
      for (const [k, v] of Object.entries(errors)) setFieldError(k, v);
      return;
    }

    try {
      // Формируем данные согласно DTO RegisterRequest
      const requestData = {
        login: payload.login,
        password: payload.password,
        email: payload.email,
        phone: payload.phone ? payload.phone : null,
        fullName: `${payload.firstName} ${payload.lastName}`.trim(),
        birthDate: payload.birthDate ? new Date(payload.birthDate).getTime() : null // Конвертируем в timestamp
      };

      const res = await fetch('/api/v1/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
      });

      if (!res.ok) {
        const data = await safeJson(res);
        if (res.status === 400) {
          if (data.errors) {
            for (const [field, message] of Object.entries(data.errors)) {
              setFieldError(field, message);
            }
          }
          throw new Error(data?.message || 'Ошибка валидации данных');
        } else if (res.status === 409) {
          throw new Error(data?.message || 'Пользователь с таким логином или email уже существует');
        } else {
          throw new Error(data?.message || `Ошибка сервера: ${res.status}`);
        }
      }

      // Успешная регистрация
      const result = await safeJson(res);
      alert('Регистрация успешна! Теперь вы можете войти в систему.');
      window.location.href = '/static/html/Authentication/login.html';

    } catch (err) {
      if (globalError) {
        globalError.textContent = err.message || 'Произошла неизвестная ошибка при регистрации';
      }
    }
  });
});

function validate(p, termsChecked) {
  const errors = {};

  // Валидация имени и фамилии
  if (!p.firstName) errors.firstName = 'Введите имя';
  else if (p.firstName.length < 2) errors.firstName = 'Имя должно содержать минимум 2 символа';

  if (!p.lastName) errors.lastName = 'Введите фамилию';
  else if (p.lastName.length < 2) errors.lastName = 'Фамилия должна содержать минимум 2 символа';

  // Проверяем полное имя (firstName + lastName) на длину
  const fullName = `${p.firstName} ${p.lastName}`.trim();
  if (fullName.length > 100) {
    errors.firstName = 'Полное имя не должно превышать 100 символов';
    errors.lastName = 'Полное имя не должно превышать 100 символов';
  }

  // Валидация логина согласно DTO
  if (!p.login) {
    errors.login = 'Введите логин';
  } else {
    if (p.login.length < 3 || p.login.length > 50) {
      errors.login = 'Логин должен быть от 3 до 50 символов';
    } else if (!/^[a-zA-Z0-9_-]+$/.test(p.login)) {
      errors.login = 'Логин может содержать только латинские буквы, цифры, подчеркивания и дефисы';
    }
  }

  // Валидация email
  if (!p.email) {
    errors.email = 'Введите email';
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(p.email)) {
    errors.email = 'Введите корректный email';
  }

  // Валидация пароля согласно DTO
  if (!p.password) {
    errors.password = 'Введите пароль';
  } else {
    if (p.password.length < 8 || p.password.length > 100) {
      errors.password = 'Пароль должен быть от 8 до 100 символов';
    } else if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).*$/.test(p.password)) {
      errors.password = 'Пароль должен содержать хотя бы одну заглавную букву, одну строчную букву и одну цифру';
    }
  }

  // Подтверждение пароля
  if (p.password !== p.confirm) {
    errors.confirm = 'Пароли не совпадают';
  }

  // Валидация телефона согласно DTO
  if (p.phone && !/^\+?[1-9]\d{1,14}$/.test(p.phone.replace(/[\s\-()]/g, ''))) {
    errors.phone = 'Введите корректный номер телефона (например: +375291234567)';
  }

  // Валидация даты рождения
  if (p.birthDate) {
    const birthDate = new Date(p.birthDate);
    const today = new Date();
    if (birthDate >= today) {
      errors.birthDate = 'Дата рождения должна быть в прошлом';
    }
  }

  // Согласие с условиями
  if (!termsChecked) {
    errors.terms = 'Подтвердите согласие с условиями использования';
  }

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

async function safeJson(res) {
  try {
    return await res.json();
  } catch {
    return null;
  }
}