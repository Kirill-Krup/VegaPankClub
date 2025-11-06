const qs = (s, r = document) => r.querySelector(s);
const qsa = (s, r = document) => Array.from(r.querySelectorAll(s));

let currentUser = null;
let selectedSeat = null;
let tariffs = [];
let bookingData = {
  tariff: null,
  date: null,
  startTime: null,
  duration: null,
  seat: null
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

// Fetch Tariffs
async function fetchTariffs() {
  try {
    console.log('Fetching tariffs...');

    const response = await fetch('/api/v1/tariffs/allTariffs', {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include'
    });

    console.log('Tariffs response status:', response.status);

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const tariffsData = await response.json();
    console.log('Tariffs loaded:', tariffsData);

    // Сортировка по hours (по возрастанию)
    tariffs = tariffsData.sort((a, b) => a.hours - b.hours);
    displayTariffs(tariffs);

    return tariffs;

  } catch (error) {
    console.error('Error fetching tariffs:', error);
    tariffs = [];
    displayTariffs(tariffs);
    return tariffs;
  }
}

function displayTariffs(tariffs) {
  const tariffGrid = qs('.tariff-grid');
  if (!tariffGrid) return;

  tariffGrid.innerHTML = tariffs.map(tariff => `
    <div class="tariff-card ${tariff.isVip ? 'vip' : ''}" 
         data-tariff-id="${tariff.tariffId}" data-tariff="${tariff.name.toLowerCase()}">
      ${tariff.isVip ? '<div class="tariff-badge">VIP</div>' : '<div class="tariff-badge">Стандарт</div>'}
      <h3 class="tariff-name">${tariff.name}</h3>
      <div class="tariff-price">
        <span class="price-value">${tariff.price}</span>
        <span class="price-currency">BYN/${tariff.hours} ${getHoursLabel(tariff.hours)}</span>
      </div>
      <div class="tariff-info">
        <p>Тип: ${tariff.isVip ? 'VIP' : 'Обычный'}</p>
      </div>
    </div>
  `).join('');

  initTariffSelection();
}

function getHoursLabel(hours) {
  const lastDigit = hours % 10;
  const lastTwoDigits = hours % 100;

  if (lastTwoDigits >= 11 && lastTwoDigits <= 14) {
    return 'часов';
  }

  if (lastDigit === 1) {
    return 'час';
  }

  if (lastDigit >= 2 && lastDigit <= 4) {
    return 'часа';
  }

  return 'часов';
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

// Step 1: Tariff Selection (первый шаг)
function initTariffSelection() {
  const tariffCards = qsa('.tariff-card');

  tariffCards.forEach(card => {
    card.addEventListener('click', () => {
      if (card.classList.contains('disabled')) return;

      // Deselect all
      tariffCards.forEach(c => c.classList.remove('selected'));

      // Select current
      card.classList.add('selected');

      const tariffId = card.getAttribute('data-tariff-id');
      const tariffName = card.getAttribute('data-tariff');

      const selectedTariff = tariffs.find(t => t.tariffId == tariffId);
      bookingData.tariff = selectedTariff;

      // Enable step 2 (Date & Time)
      const step2 = qs('#step2');
      if (step2) {
        step2.classList.remove('disabled');
        step2.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    });
  });
}

// Step 2: Date & Time (второй шаг)
function initDateTimeStep() {
  const dateInput = qs('#bookingDate');
  const startTimeSelect = qs('#startTime');
  const endTimeSelect = qs('#endTime');
  const continueBtn = qs('#continueToSeats');

  // Определяем checkForm перед использованием!
  const checkForm = () => {
    const isValid = dateInput?.value && startTimeSelect?.value && endTimeSelect?.value;
    if (continueBtn) continueBtn.disabled = !isValid;
  };

  const today = new Date().toISOString().split('T')[0];
  if (dateInput) dateInput.setAttribute('min', today);

  populateStartTimes();

  startTimeSelect?.addEventListener('change', () => {
    const selectedTime = startTimeSelect.value;
    if (selectedTime) {
      populateEndTimes(selectedTime);
    }
    checkForm();
  });

  endTimeSelect?.addEventListener('change', checkForm);
  dateInput?.addEventListener('change', checkForm);

  continueBtn?.addEventListener('click', async () => {
    bookingData.date = dateInput.value;
    bookingData.startTime = startTimeSelect.value;
    bookingData.endTime = endTimeSelect.value;

    const duration = calculateDuration(bookingData.startTime, bookingData.endTime);
    bookingData.duration = duration;

    console.log('=== Booking Data ===');
    console.table(bookingData);

    await renderPCs();

    const step3 = qs('#step3');
    if (step3) {
      step3.classList.remove('disabled');
      step3.scrollIntoView({ behavior: 'smooth', block: 'start' });

      updateFloorAvailability();
    }
  });
}


function populateStartTimes() {
  const startTimeSelect = qs('#startTime');
  if (!startTimeSelect) return;

  const now = new Date();
  const currentHour = now.getHours();
  const currentMinute = now.getMinutes();

  let startHour = currentHour;
  let startMinute = currentMinute <= 30 ? 30 : 0;
  if (currentMinute > 30) {
    startHour++;
  }

  const options = ['<option value="">Выберите время</option>'];

  for (let hour = 0; hour < 24; hour++) {
    for (let minute of [0, 30]) {
      const timeValue = `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`;
      const displayTime = formatTimeDisplay(hour, minute);
      options.push(`<option value="${timeValue}">${displayTime}</option>`);
    }
  }

  startTimeSelect.innerHTML = options.join('');
}

function populateEndTimes(startTime) {
  const endTimeSelect = qs('#endTime');
  if (!endTimeSelect) return;

  const [startHour, startMinute] = startTime.split(':').map(Number);

  const options = ['<option value="">Выберите время окончания</option>'];

  let currentHour = startHour;
  let currentMinute = startMinute + 30;

  if (currentMinute >= 60) {
    currentMinute = 0;
    currentHour++;
  }

  for (let i = 0; i < 96; i++) {
    if (currentHour >= 24) {
      currentHour = 0;
    }

    const timeValue = `${String(currentHour).padStart(2, '0')}:${String(currentMinute).padStart(2, '0')}`;
    const displayTime = formatTimeDisplay(currentHour, currentMinute);

    const hoursFromStart = i * 0.5;
    const dayIndicator = hoursFromStart >= 24 ? ' (+1 день)' : hoursFromStart >= 48 ? ' (+2 дня)' : '';

    options.push(`<option value="${timeValue}">${displayTime}${dayIndicator}</option>`);

    currentMinute += 30;
    if (currentMinute >= 60) {
      currentMinute = 0;
      currentHour++;
    }
  }

  endTimeSelect.innerHTML = options.join('');
}

function formatTimeDisplay(hour, minute) {
  return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`;
}

function calculateDuration(startTime, endTime) {
  const [startHour, startMinute] = startTime.split(':').map(Number);
  const [endHour, endMinute] = endTime.split(':').map(Number);

  let startTotalMinutes = startHour * 60 + startMinute;
  let endTotalMinutes = endHour * 60 + endMinute;

  if (endTotalMinutes <= startTotalMinutes) {
    endTotalMinutes += 24 * 60;
  }

  const durationMinutes = endTotalMinutes - startTotalMinutes;
  return durationMinutes / 60;
}


// Step 3: Floor & Seat Selection (третий шаг)
function initFloorSelection() {
  const floorBtns = qsa('.floor-btn');
  const floors = qsa('.floor-plan');

  floorBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      const floorNum = btn.getAttribute('data-floor');

      // Update buttons
      floorBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');

      floors.forEach(f => f.classList.remove('active'));
      const targetFloor = qs(`#floor${floorNum}`);
      if (targetFloor) targetFloor.classList.add('active');
    });
  });
}


function updateFloorAvailability() {
  const isVipTariff = bookingData.tariff?.isVip;
  const floor1Btn = qs('[data-floor="1"]');
  const floor2Btn = qs('[data-floor="2"]');

  if (isVipTariff) {
    // VIP tariff can access both floors
    floor1Btn?.classList.remove('disabled');
    floor2Btn?.classList.remove('disabled');
  } else {
    // Non-VIP tariff can only access floor 1
    floor1Btn?.classList.remove('disabled');
    floor2Btn?.classList.add('disabled');

    // Auto-select floor 1 if VIP floor was selected
    const activeFloorBtn = qs('.floor-btn.active');
    if (activeFloorBtn?.getAttribute('data-floor') === '2') {
      floor1Btn?.click();
    }
  }
}

function initSeatSelection() {
  const seats = qsa('.pc-seat');

  console.log('=== Initializing Seat Selection ===');
  console.log('Total seats:', seats.length);

  seats.forEach(seat => {
    // Удаляем старые обработчики, добавляя новый
    seat.removeEventListener('click', handleSeatClick);
    seat.addEventListener('click', handleSeatClick);
  });
}

function handleSeatClick(event) {
  const seat = event.currentTarget;

  if (seat.classList.contains('occupied')) {
    console.warn('Attempted to select occupied seat:', seat.getAttribute('data-seat'));
    alert('Это место уже занято');
    return;
  }

  if (!seat.classList.contains('available')) {
    console.warn('Attempted to select disabled seat:', seat.getAttribute('data-seat'));
    return;
  }

  const seats = qsa('.pc-seat');
  seats.forEach(s => s.classList.remove('selected'));

  seat.classList.add('selected');
  selectedSeat = seat.getAttribute('data-seat');
  bookingData.seat = selectedSeat;

  const pcInfo = JSON.parse(seat.getAttribute('data-pc-info'));
  bookingData.pcInfo = pcInfo;

  console.log('=== Seat Selected ===');
  console.log('Seat ID:', selectedSeat);
  console.log('PC Info:', pcInfo);
  console.log('Current booking data:', bookingData);

  showBookingSummary();
}


function showBookingSummary() {
  const summary = qs('#bookingSummary');
  if (!summary) return;

  summary.style.display = 'block';

  qs('#summaryDate').textContent = formatDate(bookingData.date);
  qs('#summaryTime').textContent = `${bookingData.startTime} - ${bookingData.endTime} (${bookingData.duration.toFixed(1)} ч)`;
  qs('#summarySeat').textContent = bookingData.pcInfo?.name || bookingData.seat;
  qs('#summaryTariff').textContent = bookingData.tariff?.name || '';

  const price = calculatePrice();
  qs('#summaryPrice').textContent = `${price.toFixed(2)} BYN`;

  summary.scrollIntoView({ behavior: 'smooth', block: 'start' });
}


function formatDate(dateString) {
  const date = new Date(dateString);
  return date.toLocaleDateString('ru-RU', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  });
}

function calculatePrice() {
  const duration = bookingData.duration;
  const pricePerHour = bookingData.tariff?.price / bookingData.tariff?.hours || 10;
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

    if (!bookingData.tariff || !bookingData.date || !bookingData.startTime || !bookingData.endTime || !bookingData.seat) {
      alert('Пожалуйста, заполните все данные бронирования');
      return;
    }

    try {
      const startDateTime = `${bookingData.date}T${bookingData.startTime}:00`;
      const endDateTime = `${bookingData.date}T${bookingData.endTime}:00`;

      const bookingPayload = {
        tariffId: bookingData.tariff.tariffId,
        startTime: startDateTime,
        endTime: endDateTime,
        seatNumber: bookingData.seat,
        totalPrice: calculatePrice()
      };

      console.log('Sending booking request:', bookingPayload);

      const response = await fetch('/api/v1/bookings/create', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify(bookingPayload)
      });

      if (response.ok) {
        const result = await response.json();
        alert('Бронирование успешно создано!');
        console.log('Booking created:', result);
        window.location.href = '/static/html/profile.html';
      } else {
        const error = await response.text();
        throw new Error(error || 'Ошибка при создании бронирования');
      }

    } catch (error) {
      console.error('Booking error:', error);
      alert('Ошибка при создании бронирования: ' + error.message);
    }
  });
}

// Fetch all PCs from inventory service
async function fetchAllPcs() {
  try {
    console.log('Fetching all PCs...');

    const response = await fetch('/api/v1/pcs/allPc', {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include'
    });

    console.log('PCs response status:', response.status);

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const pcsData = await response.json();
    console.log('=== All PCs Loaded ===');
    console.table(pcsData);

    return pcsData;

  } catch (error) {
    console.error('Error fetching PCs:', error);
    return [];
  }
}

async function fetchAvailableSeats() {
  if (!bookingData.date || !bookingData.startTime || !bookingData.endTime) {
    console.warn('Date or time not selected yet');
    return [];
  }

  try {
    console.log('=== Fetching Sessions Info ===');
    console.log('Date:', bookingData.date);
    console.log('Start Time:', bookingData.startTime);
    console.log('End Time:', bookingData.endTime);

    const response = await fetch(
        `/api/v1/sessions/sessionsForInfo?startDate=${bookingData.date}&endDate=${bookingData.date}`,
        {
          method: 'GET',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include'
        }
    );

    console.log('Sessions response status:', response.status);

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const sessions = await response.json();

    console.log('=== Sessions Received ===');
    console.log('Total sessions:', sessions.length);
    console.table(sessions);

    const occupiedPcIds = new Set();

    sessions.forEach(session => {
      const sessionStart = new Date(session.startTime);
      const sessionEnd = new Date(session.endTime);
      const bookingStart = new Date(`${bookingData.date}T${bookingData.startTime}:00`);
      const bookingEnd = new Date(`${bookingData.date}T${bookingData.endTime}:00`);

      const isOverlapping = sessionStart < bookingEnd && sessionEnd > bookingStart;

      if (isOverlapping) {
        occupiedPcIds.add(session.pcId);
        console.log(`PC ${session.pcId} is occupied`);
      }
    });

    console.log('=== Occupied PCs ===');
    console.table(Array.from(occupiedPcIds));

    return occupiedPcIds;

  } catch (error) {
    console.error('Error fetching sessions:', error);
    return new Set();
  }
}

// Render PCs dynamically from API
async function renderPCs() {
  const floor1Layout = qs('#floor1 .plan-layout');
  const floor2Layout = qs('#floor2 .plan-layout');

  if (!floor1Layout || !floor2Layout) {
    console.error('Floor layouts not found');
    return;
  }

  try {
    // Загружаем все ПК
    const allPcs = await fetchAllPcs();

    if (allPcs.length === 0) {
      console.warn('No PCs returned from API');
      return;
    }

    // Группируем ПК по комнатам/этажам
    const pcsByRoom = {};
    allPcs.forEach(pc => {
      const roomId = pc.roomDTO?.id;
      if (!pcsByRoom[roomId]) {
        pcsByRoom[roomId] = [];
      }
      pcsByRoom[roomId].push(pc);
    });

    console.log('=== PCs Grouped by Room ===');
    console.table(pcsByRoom);

    // Получаем информацию о занятых ПК
    const occupiedPcIds = await fetchAvailableSeats();

    // Очищаем старые ПК
    const oldPcSeats = qsa('.pc-seat');
    oldPcSeats.forEach(seat => seat.remove());

    // Отрисовываем ПК на первом этаже
    const floor1Pcs = allPcs.filter(pc => pc.roomDTO?.id === 1 || !pc.roomDTO);
    renderPcsForFloor(floor1Layout, floor1Pcs, occupiedPcIds, 1);

    // Отрисовываем ПК на втором этаже (VIP)
    const floor2Pcs = allPcs.filter(pc => pc.roomDTO?.id === 2);
    if (floor2Pcs.length > 0) {
      renderPcsForFloor(floor2Layout, floor2Pcs, occupiedPcIds, 2);
    }

    // Инициализируем обработчики клика на ПК
    initSeatSelection();

  } catch (error) {
    console.error('Error rendering PCs:', error);
  }
}

// Render PCs for specific floor
function renderPcsForFloor(floorLayout, pcs, occupiedPcIds, floorNum) {
  const fragment = document.createDocumentFragment();

  pcs.forEach(pc => {
    const isOccupied = occupiedPcIds.has(pc.id);
    const status = isOccupied ? 'occupied' : 'available';

    const pcElement = document.createElement('div');
    pcElement.className = `pc-seat ${status}`;
    pcElement.setAttribute('data-seat', `${floorNum}-${pc.id}`);
    pcElement.setAttribute('data-pc-id', pc.id);
    pcElement.setAttribute('data-pc-name', pc.name);

    // Создаём информацию о ПК для tooltip
    const pcInfo = {
      name: pc.name,
      cpu: pc.cpu,
      gpu: pc.gpu,
      ram: pc.ram,
      monitor: pc.monitor,
      isEnabled: pc.isEnabled,
      room: pc.roomDTO?.name || 'Unknown'
    };

    pcElement.setAttribute('data-pc-info', JSON.stringify(pcInfo));
    pcElement.setAttribute('title', formatPcTooltip(pcInfo));

    pcElement.innerHTML = `
      <i class="fas fa-desktop"></i>
      <span>ПК ${pc.id}</span>
    `;

    // Добавляем обработчик для вывода информации при наведении
    pcElement.addEventListener('mouseenter', () => {
      showPcTooltip(pcElement, pcInfo);
    });

    pcElement.addEventListener('mouseleave', () => {
      hidePcTooltip();
    });

    fragment.appendChild(pcElement);
  });

  // Добавляем ПК в layout после существующих элементов (entrance, reception, stairs)
  const existingElements = floorLayout.querySelectorAll('.room-element');
  if (existingElements.length > 0) {
    existingElements[existingElements.length - 1].after(...Array.from(fragment.childNodes));
  } else {
    floorLayout.appendChild(fragment);
  }

  console.log(`Rendered ${pcs.length} PCs on floor ${floorNum}`);
}

// Format PC information for tooltip
function formatPcTooltip(pcInfo) {
  return `
${pcInfo.name}
CPU: ${pcInfo.cpu}
GPU: ${pcInfo.gpu}
RAM: ${pcInfo.ram}
Monitor: ${pcInfo.monitor}
Room: ${pcInfo.room}
Status: ${pcInfo.isEnabled ? 'Enabled' : 'Disabled'}
  `.trim();
}

function showPcTooltip(element, pcInfo) {
  let tooltip = document.getElementById('pc-tooltip');

  if (!tooltip) {
    tooltip = document.createElement('div');
    tooltip.id = 'pc-tooltip';
    tooltip.className = 'pc-tooltip';
    document.body.appendChild(tooltip);
  }

  tooltip.innerHTML = `
    <div class="tooltip-header">${pcInfo.name}</div>
    <div class="tooltip-body">
      <div class="tooltip-row">
        <span class="tooltip-label">CPU:</span>
        <span class="tooltip-value">${pcInfo.cpu}</span>
      </div>
      <div class="tooltip-row">
        <span class="tooltip-label">GPU:</span>
        <span class="tooltip-value">${pcInfo.gpu}</span>
      </div>
      <div class="tooltip-row">
        <span class="tooltip-label">RAM:</span>
        <span class="tooltip-value">${pcInfo.ram}</span>
      </div>
      <div class="tooltip-row">
        <span class="tooltip-label">Monitor:</span>
        <span class="tooltip-value">${pcInfo.monitor}</span>
      </div>
      <div class="tooltip-row">
        <span class="tooltip-label">Room:</span>
        <span class="tooltip-value">${pcInfo.room}</span>
      </div>
    </div>
  `;

  const rect = element.getBoundingClientRect();
  tooltip.style.position = 'fixed';
  tooltip.style.left = (rect.left + rect.width / 2) + 'px';
  tooltip.style.top = (rect.top - 10) + 'px';
  tooltip.style.display = 'block';
  tooltip.style.transform = 'translateX(-50%)';
  tooltip.style.zIndex = '1000';
}

function hidePcTooltip() {
  const tooltip = document.getElementById('pc-tooltip');
  if (tooltip) {
    tooltip.style.display = 'none';
  }
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
  await fetchTariffs();

  attachAuthHandlers();
  attachNavToggle();
  initTariffSelection();
  initDateTimeStep();
  initFloorSelection();
  initSeatSelection();
  initConfirmBooking();
  setYear();

  const profileContainer = qs('.auth__profile');
  profileContainer?.addEventListener('click', () => {
    window.location.href = '/static/html/profile.html';
  });

  console.log('Booking app initialized');
}

document.addEventListener('DOMContentLoaded', initApp);