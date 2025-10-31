const qs = (s, r = document) => r.querySelector(s);
const qsa = (s, r = document) => Array.from(r.querySelectorAll(s));

let currentUser = null;
let selectedSeat = null;
let bookingData = {
  date: null,
  startTime: null,
  duration: null,
  seat: null,
  tariff: null
};

// Fetch User Profile
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

    currentUser = {
      username: userData.login || 'Пользователь',
      balance: userData.wallet || 0,
      avatar: userData.photoPath || `https://i.pravatar.cc/100?img=${Math.floor(Math.random() * 70)}`,
      id: userData.id,
      email: userData.email
    };

    renderAuth(currentUser);
    return currentUser;

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
    if (avatar) avatar.src = user.avatar;
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
    window.location.href = '/static/html/Authentication/registration.html';
  });

  loginBtn?.addEventListener('click', () => {
    window.location.href = '/static/html/Authentication/login.html';
  });

  logoutBtn?.addEventListener('click', async () => {
    try {
      await fetch('/api/v1/users/logout', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include'
      });
      currentUser = null;
      window.location.href = '/static/html/index.html';
    } catch (err) {
      console.error('Logout error:', err);
      window.location.href = '/static/html/index.html';
    }
  });
}

// Step 1: Date & Time
function initDateTimeStep() {
  const dateInput = qs('#bookingDate');
  const timeInput = qs('#startTime');
  const durationInput = qs('#duration');
  const continueBtn = qs('#continueToSeats');

  // Set min date to today
  const today = new Date().toISOString().split('T')[0];
  if (dateInput) dateInput.setAttribute('min', today);

  const checkForm = () => {
    const isValid = dateInput?.value && timeInput?.value && durationInput?.value;
    if (continueBtn) continueBtn.disabled = !isValid;
  };

  dateInput?.addEventListener('change', checkForm);
  timeInput?.addEventListener('change', checkForm);
  durationInput?.addEventListener('change', checkForm);

  continueBtn?.addEventListener('click', () => {
    bookingData.date = dateInput.value;
    bookingData.startTime = timeInput.value;
    bookingData.duration = durationInput.value;

    // Enable step 2
    const step2 = qs('#step2');
    if (step2) {
      step2.classList.remove('disabled');
      step2.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  });
}

// Step 2: Floor & Seat Selection
function initFloorSelection() {
  const floorBtns = qsa('.floor-btn');
  const floors = qsa('.floor-plan');

  floorBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      const floorNum = btn.getAttribute('data-floor');

      // Update buttons
      floorBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');

      // Update floors
      floors.forEach(f => f.classList.remove('active'));
      const targetFloor = qs(`#floor${floorNum}`);
      if (targetFloor) targetFloor.classList.add('active');
    });
  });
}

function initSeatSelection() {
  const seats = qsa('.pc-seat');

  seats.forEach(seat => {
    seat.addEventListener('click', () => {
      if (seat.classList.contains('occupied')) return;

      // Deselect previous
      seats.forEach(s => s.classList.remove('selected'));

      // Select current
      seat.classList.add('selected');
      selectedSeat = seat.getAttribute('data-seat');

      bookingData.seat = selectedSeat;

      // Show tariff selection
      const step3 = qs('#step3');
      if (step3) {
        step3.classList.remove('disabled');
        step3.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    });
  });
}

// Step 3: Tariff Selection
function initTariffSelection() {
  const tariffCards = qsa('.tariff-card');

  tariffCards.forEach(card => {
    card.addEventListener('click', () => {
      if (card.classList.contains('disabled')) return;

      // Deselect all
      tariffCards.forEach(c => c.classList.remove('selected'));

      // Select current
      card.classList.add('selected');
      const tariffId = card.getAttribute('data-tariff');
      bookingData.tariff = tariffId;

      // Show confirmation
      showBookingSummary();
    });
  });
}

function showBookingSummary() {
  const summary = qs('#bookingSummary');
  if (!summary) return;

  summary.style.display = 'block';

  // Fill summary data
  qs('#summaryDate').textContent = bookingData.date;
  qs('#summaryTime').textContent = `${bookingData.startTime} (${bookingData.duration} ч)`;
  qs('#summarySeat').textContent = bookingData.seat;
  qs('#summaryTariff').textContent = getTariffName(bookingData.tariff);

  const price = calculatePrice();
  qs('#summaryPrice').textContent = `${price} BYN`;

  summary.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function getTariffName(tariffId) {
  const names = {
    'basic': 'Базовый',
    'standard': 'Стандарт',
    'premium': 'Премиум',
    'vip': 'VIP'
  };
  return names[tariffId] || tariffId;
}

function calculatePrice() {
  const duration = parseInt(bookingData.duration);
  const tariffPrices = {
    'basic': 8,
    'standard': 12,
    'premium': 16,
    'vip': 25
  };
  const pricePerHour = tariffPrices[bookingData.tariff] || 10;
  return duration * pricePerHour;
}

function initConfirmBooking() {
  const confirmBtn = qs('#confirmBookingBtn');

  confirmBtn?.addEventListener('click', async () => {
    if (!currentUser) {
      alert('Пожалуйста, войдите в систему для бронирования');
      window.location.href = '/static/html/Authentication/login.html';
      return;
    }

    // TODO: Send booking request
    console.log('Booking data:', bookingData);
    alert('Бронирование успешно создано!\n(Функционал в разработке)');
  });
}

function setYear() {
  const y = qs('#year');
  if (y) y.textContent = String(new Date().getFullYear());
}

function attachNavToggle() {
  const btn = qs('[data-nav-toggle]');
  const list = qs('.nav-menu');
  if (!btn || !list) return;
  btn.addEventListener('click', () => {
    list.classList.toggle('show');
  });
}

async function initApp() {
  console.log('Initializing booking app...');

  await fetchUserProfile();

  attachAuthHandlers();
  attachNavToggle();
  initDateTimeStep();
  initFloorSelection();
  initSeatSelection();
  initTariffSelection();
  initConfirmBooking();
  setYear();

  const profileContainer = qs('.auth__profile');
  profileContainer?.addEventListener('click', () => {
    window.location.href = '/static/html/profile.html';
  });

  console.log('Booking app initialized');
}

document.addEventListener('DOMContentLoaded', initApp);
