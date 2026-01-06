const qs = (s, r = document) => r.querySelector(s);
const qsa = (s, r = document) => Array.from(r.querySelectorAll(s));

let currentUser = null;
let tariffs = [];
let userBonusCoins = 0;
let usedBonusCoins = 0;
let bookingData = {
  tariff: null,
  date: null,
  startTime: null,
  endTime: null,
  duration: null,
  seats: [],
  pcsInfo: []
};

async function fetchUserProfile() {
  try {
    const response = await fetch('/api/v1/profile/getProfile', {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include'
    });

    if (!response.ok) {
      if (response.status === 401 || response.status === 403) {
        renderAuth(null);
        return null;
      }
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const userData = await response.json();
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

async function fetchUserBonusCoins() {
  if (!currentUser) {
    userBonusCoins = 0;
    return 0;
  }

  try {
    const response = await fetch('/api/v1/profile/myCoins', {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    userBonusCoins = data.bonusCoins || 0;
    return userBonusCoins;
  } catch (error) {
    console.error('Error fetching bonus coins:', error);
    userBonusCoins = 0;
    return 0;
  }
}

async function fetchTariffs() {
  try {
    const response = await fetch('/api/v1/tariffs/allTariffs', {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include'
    });

    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

    const tariffsData = await response.json();
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
    <div class="tariff-card ${tariff.vip ? 'vip' : ''}" 
         data-tariff-id="${tariff.tariffId}" data-tariff="${tariff.name.toLowerCase()}">
      ${tariff.vip ? '<div class="tariff-badge">VIP</div>' : '<div class="tariff-badge">Стандарт</div>'}
      <h3 class="tariff-name">${tariff.name}</h3>
      <div class="tariff-price">
        <span class="price-value">${tariff.price}</span>
        <span class="price-currency">BYN/${tariff.hours} ${getHoursLabel(tariff.hours)}</span>
      </div>
      <div class="tariff-info">
        <p>Тип: ${tariff.vip ? 'VIP' : 'Обычный'}</p>
      </div>
    </div>
  `).join('');

  initTariffSelection();
}

function getHoursLabel(hours) {
  const lastDigit = hours % 10;
  const lastTwoDigits = hours % 100;
  if (lastTwoDigits >= 11 && lastTwoDigits <= 14) return 'часов';
  if (lastDigit === 1) return 'час';
  if (lastDigit >= 2 && lastDigit <= 4) return 'часа';
  return 'часов';
}

function renderAuth(user) {
  const guest = qs('[data-guest-section]');
  const userSection = qs('[data-user-section]');
  if (!guest || !userSection) return;

  if (user) {
    guest.style.display = 'none';
    userSection.style.display = 'flex';
    const nameEl = qs('[data-username]');
    const balEl = qs('[data-balance]');
    const avatar = qs('[data-avatar]');
    if (nameEl) nameEl.textContent = user.username || 'Пользователь';
    if (balEl) balEl.textContent = (user.balance ?? 0).toFixed(2);
    if (avatar) avatar.src = user.avatar;
  } else {
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

function initTariffSelection() {
  const tariffCards = qsa('.tariff-card');

  tariffCards.forEach(card => {
    card.addEventListener('click', () => {
      if (card.classList.contains('disabled')) return;

      tariffCards.forEach(c => c.classList.remove('selected'));
      card.classList.add('selected');

      const tariffId = card.getAttribute('data-tariff-id');
      const selectedTariff = tariffs.find(t => t.tariffId == tariffId);
      bookingData.tariff = selectedTariff;

      updateBookingSummary();

      const step2 = qs('#step2');
      if (step2) {
        step2.classList.remove('disabled');
        step2.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    });
  });
}

function initDateTimeStep() {
  const dateInput = qs('#bookingDate');
  const startTimeSelect = qs('#startTime');
  const endTimeSelect = qs('#endTime');
  const continueBtn = qs('#continueToSeats');

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

  endTimeSelect?.addEventListener('change', () => {
    checkForm();
    if (endTimeSelect.value) {
      bookingData.endTime = endTimeSelect.value;
      bookingData.duration = calculateDuration(bookingData.startTime, bookingData.endTime);
      updateBookingSummary();
    }
  });

  dateInput?.addEventListener('change', () => {
    bookingData.date = dateInput.value;
    checkForm();
    updateBookingSummary();
  });

  continueBtn?.addEventListener('click', async () => {
    bookingData.date = dateInput.value;
    bookingData.startTime = startTimeSelect.value;
    bookingData.endTime = endTimeSelect.value;
    bookingData.duration = calculateDuration(bookingData.startTime, bookingData.endTime);

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

  const options = ['<option value="">Выберите время</option>'];

  for (let hour = 0; hour < 24; hour++) {
    for (let minute of [0, 30]) {
      const timeValue = `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`;
      const displayTime = formatTimeDisplay(hour, minute);
      options.push(`<option value="${timeValue}">${displayTime}</option>`);
    }
  }

  startTimeSelect.innerHTML = options.join('');

  startTimeSelect.addEventListener('change', () => {
    bookingData.startTime = startTimeSelect.value;
    updateBookingSummary();
  });
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
    if (currentHour >= 24) currentHour = 0;

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

function formatDateWithOffset(dateString, offsetDays = 0) {
  const [year, month, day] = dateString.split('-').map(Number);
  const utcDate = new Date(Date.UTC(year, month - 1, day));
  utcDate.setUTCDate(utcDate.getUTCDate() + offsetDays);
  const yyyy = utcDate.getUTCFullYear();
  const mm = String(utcDate.getUTCMonth() + 1).padStart(2, '0');
  const dd = String(utcDate.getUTCDate()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
}

function getStartDateTimeISO() {
  if (!bookingData.date || !bookingData.startTime) {
    return null;
  }
  return `${bookingData.date}T${bookingData.startTime}:00`;
}

function getEndTimeDayOffset(startTime = bookingData.startTime, endTime = bookingData.endTime) {
  if (!startTime || !endTime) {
    return 0;
  }

  const [startHour, startMinute] = startTime.split(':').map(Number);
  const [endHour, endMinute] = endTime.split(':').map(Number);
  const startTotalMinutes = startHour * 60 + startMinute;
  const endTotalMinutes = endHour * 60 + endMinute;

  let offset = endTotalMinutes < startTotalMinutes ? 1 : 0;

  const endTimeSelect = qs('#endTime');
  if (endTimeSelect && endTimeSelect.value === endTime) {
    const optionText = endTimeSelect.selectedOptions[0]?.textContent || '';
    if (optionText.includes('(+2 дня)')) {
      offset = 2;
    } else if (optionText.includes('(+1 день)')) {
      offset = 1;
    }
  }

  return offset;
}

function getEndDateTimeISO() {
  if (!bookingData.date || !bookingData.endTime) {
    return null;
  }

  const formattedDate = formatDateWithOffset(bookingData.date, getEndTimeDayOffset());
  return `${formattedDate}T${bookingData.endTime}:00`;
}

function calculateDuration(startTime, endTime) {
  if (!startTime || !endTime) {
    return 0;
  }

  const [startHour, startMinute] = startTime.split(':').map(Number);
  const [endHour, endMinute] = endTime.split(':').map(Number);

  let startTotalMinutes = startHour * 60 + startMinute;
  let endTotalMinutes = endHour * 60 + endMinute;

  const daysDifference = getEndTimeDayOffset(startTime, endTime);
  endTotalMinutes += daysDifference * 24 * 60;

  const durationMinutes = endTotalMinutes - startTotalMinutes;
  return durationMinutes / 60;
}



function initFloorSelection() {
  const floorBtns = qsa('.floor-btn');
  const floors = qsa('.floor-plan');

  floorBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      const floorNum = btn.getAttribute('data-floor');
      floorBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      floors.forEach(f => f.classList.remove('active'));
      const targetFloor = qs(`#floor${floorNum}`);
      if (targetFloor) targetFloor.classList.add('active');
    });
  });
}

function updateFloorAvailability() {
  const vipTariff = bookingData.tariff?.vip;
  const floor1Btn = qs('[data-floor="1"]');
  const floor2Btn = qs('[data-floor="2"]');

  if (vipTariff) {
    floor1Btn?.classList.remove('disabled');
    floor2Btn?.classList.remove('disabled');
  } else {
    floor1Btn?.classList.remove('disabled');
    floor2Btn?.classList.add('disabled');
    const activeFloorBtn = qs('.floor-btn.active');
    if (activeFloorBtn?.getAttribute('data-floor') === '2') {
      floor1Btn?.click();
    }
  }
}

function initSeatSelection() {
  const seats = qsa('.pc-seat');
  seats.forEach(seat => {
    seat.removeEventListener('click', handleSeatClick);
    seat.addEventListener('click', handleSeatClick);
  });
}

function handleSeatClick(event) {
  const seat = event.currentTarget;

  if (seat.classList.contains('occupied')) {
    alert('Это место уже занято');
    return;
  }

  if (!seat.classList.contains('available')) return;

  const seatId = seat.getAttribute('data-seat');
  const pcInfo = JSON.parse(seat.getAttribute('data-pc-info'));
  const isAlreadySelected = seat.classList.contains('selected');

  if (isAlreadySelected) {
    seat.classList.remove('selected');
    const index = bookingData.seats.indexOf(seatId);
    if (index > -1) {
      bookingData.seats.splice(index, 1);
      bookingData.pcsInfo.splice(index, 1);
    }
  } else {
    seat.classList.add('selected');
    bookingData.seats.push(seatId);
    bookingData.pcsInfo.push(pcInfo);
  }

  updateBookingSummary();
}

function initBonusSystem() {
  const checkbox = qs('#useBonusCheckbox');
  const sliderContainer = qs('#bonusSliderContainer');
  const slider = qs('#bonusSlider');
  const bonusUseValue = qs('#bonusUseValue');
  const bonusInRubles = qs('#bonusInRubles');

  if (!checkbox || !sliderContainer || !slider) return;

  checkbox.addEventListener('change', () => {
    if (checkbox.checked) {
      sliderContainer.style.display = 'block';
      updateBonusSlider();
    } else {
      sliderContainer.style.display = 'none';
      usedBonusCoins = 0;
      slider.value = 0;
      updatePriceBreakdown();
    }
  });

  slider.addEventListener('input', () => {
    usedBonusCoins = parseInt(slider.value);
    if (bonusUseValue) {
      bonusUseValue.textContent = usedBonusCoins;
    }

    if (bonusInRubles) {
      bonusInRubles.textContent = (usedBonusCoins / 100).toFixed(2);
    }

    updatePriceBreakdown();
  });
}

function updateBonusSlider() {
  const slider = qs('#bonusSlider');
  const maxBonusLabel = qs('#maxBonusLabel');
  const bonusUseValue = qs('#bonusUseValue');

  if (!slider) return;

  const originalPrice = calculateOriginalPrice();

  const maxUsableCoinsInByn = originalPrice * 0.5;
  const maxUsableCoins = Math.min(
      userBonusCoins,
      Math.floor(maxUsableCoinsInByn * 100)
  );

  slider.max = maxUsableCoins;
  slider.value = 0;
  usedBonusCoins = 0;

  if (maxBonusLabel) {
    maxBonusLabel.textContent = maxUsableCoins;
  }

  if (bonusUseValue) {
    bonusUseValue.textContent = '0';
  }
}

function calculateOriginalPrice() {
  if (!bookingData.duration || bookingData.seats.length === 0 || !bookingData.tariff) {
    return 0;
  }
  const duration = bookingData.duration;
  const pricePerHour = bookingData.tariff.price / bookingData.tariff.hours;
  const pcCount = bookingData.seats.length;
  return duration * pricePerHour * pcCount;
}

function calculateFinalPrice() {
  const originalPrice = calculateOriginalPrice(); // в BYN
  const discountInByn = usedBonusCoins / 100;
  return Math.max(0, originalPrice - discountInByn);
}

function calculateEarnedCoins() {
  const finalPrice = calculateFinalPrice(); // цена в BYN
  return Math.floor(finalPrice * 0.1 * 100);
}

function updatePriceBreakdown() {
  const originalPriceEl = qs('#originalPrice');
  const discountRow = qs('#discountRow');
  const discountValue = qs('#discountValue');
  const summaryPriceEl = qs('#summaryPrice');

  const originalPrice = calculateOriginalPrice();
  const finalPrice = calculateFinalPrice();

  if (originalPriceEl) {
    originalPriceEl.textContent = `${originalPrice.toFixed(2)} BYN`;
  }

  if (usedBonusCoins > 0) {
    if (discountRow) discountRow.style.display = 'flex';
    if (discountValue) {

      const discountInByn = (usedBonusCoins / 100).toFixed(2);
      discountValue.textContent = `-${discountInByn} BYN`;
    }
  } else {
    if (discountRow) discountRow.style.display = 'none';
  }

  if (summaryPriceEl) {
    summaryPriceEl.textContent = `${finalPrice.toFixed(2)} BYN`;
  }

  const earnCoinsEl = qs('#earnCoins');
  if (earnCoinsEl) {

    earnCoinsEl.textContent = calculateEarnedCoins();
  }
}

async function updateBookingSummary() {
  const summary = qs('#bookingSummary');
  if (!summary) return;

  if (!bookingData.tariff) {
    summary.style.display = 'none';
    return;
  }

  summary.style.display = 'block';

  const tariffEl = qs('#summaryTariff');
  if (tariffEl) {
    tariffEl.textContent = bookingData.tariff?.name || '-';
  }

  const dateEl = qs('#summaryDate');
  if (dateEl) {
    dateEl.textContent = bookingData.date ? formatDate(bookingData.date) : '-';
  }

  const timeEl = qs('#summaryTime');
  if (timeEl && bookingData.startTime && bookingData.endTime && bookingData.duration) {
    timeEl.textContent = `${bookingData.startTime} - ${bookingData.endTime} (${bookingData.duration.toFixed(1)} ч)`;
  } else if (timeEl) {
    timeEl.textContent = '-';
  }

  const seatsSummary = qs('#summarySeat');
  if (seatsSummary) {
    if (bookingData.pcsInfo.length > 0) {
      const seatsHtml = bookingData.pcsInfo.map(pc => `
        <div class="summary-pc-item">
          <i class="fas fa-desktop"></i>
          <span>${pc.name}</span>
          <span class="room-label">(${pc.roomName})</span>
        </div>
      `).join('');
      seatsSummary.innerHTML = `<div class="summary-pcs-list">${seatsHtml}</div>`;
    } else {
      seatsSummary.innerHTML = '<span class="summary-placeholder">Выберите ПК</span>';
    }
  }

  const confirmBtn = qs('#confirmBookingBtn');
  const bonusSection = qs('#bonusSection');

  if (bookingData.duration && bookingData.seats.length > 0) {
    await fetchUserBonusCoins();

    if (bonusSection && currentUser) {
      bonusSection.style.display = 'block';

      const availableCoinsEl = qs('#availableCoins');
      if (availableCoinsEl) {
        availableCoinsEl.textContent = userBonusCoins;
      }

      updateBonusSlider();
    }

    updatePriceBreakdown();

    if (confirmBtn) confirmBtn.disabled = false;
  } else {
    if (bonusSection) bonusSection.style.display = 'none';
    if (confirmBtn) confirmBtn.disabled = true;

    const originalPriceEl = qs('#originalPrice');
    const summaryPriceEl = qs('#summaryPrice');
    if (originalPriceEl) originalPriceEl.textContent = '0.00 BYN';
    if (summaryPriceEl) summaryPriceEl.textContent = '0.00 BYN';
  }
}

function formatDate(dateString) {
  const date = new Date(dateString);
  return date.toLocaleDateString('ru-RU', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  });
}

function initConfirmBooking() {
  const confirmBtn = qs('#confirmBookingBtn');

  confirmBtn?.addEventListener('click', async () => {
    if (!currentUser) {
      alert('Пожалуйста, войдите в систему для бронирования');
      window.location.href = '/static/html/Authentication/login.html';
      return;
    }

    if (!bookingData.tariff || !bookingData.date || !bookingData.startTime ||
        !bookingData.endTime || bookingData.seats.length === 0) {
      alert('Пожалуйста, заполните все данные и выберите хотя бы один ПК');
      return;
    }

    const finalPrice = calculateFinalPrice();

    if (currentUser.balance < finalPrice) {
      alert(`Недостаточно средств. Необходимо: ${finalPrice.toFixed(2)} BYN, Доступно: ${currentUser.balance.toFixed(2)} BYN`);
      return;
    }

    const startDateTime = getStartDateTimeISO();
    const endDateTime = getEndDateTimeISO();

    if (!startDateTime || !endDateTime) {
      alert('Не удалось определить время бронирования. Пожалуйста, попробуйте снова.');
      return;
    }

    try {
      const sessionPromises = bookingData.seats.map(async seat => {
        const [, pcIdRaw] = seat.split('-');
        const pcId = Number(pcIdRaw);

        if (Number.isNaN(pcId)) {
          throw new Error(`Некорректный идентификатор ПК: ${seat}`);
        }

        const sessionPayload = {
          pcId,
          tariffId: bookingData.tariff.tariffId,
          startTime: startDateTime,
          endTime: endDateTime
        };

        const response = await fetch('/api/v1/sessions/createSession', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include',
          body: JSON.stringify(sessionPayload)
        });

        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(errorText || `Не удалось создать сессию для ПК ${pcId}`);
        }

        return response.json().catch(() => null);
      });

      await Promise.all(sessionPromises);

      const earnedCoins = calculateEarnedCoins();
      alert(`Успешно! 
Бронирований: ${bookingData.seats.length}
Оплачено: ${finalPrice.toFixed(2)} BYN
${usedBonusCoins > 0 ? `Использовано бонусов: ${usedBonusCoins}\n` : ''}Начислено бонусов: ${earnedCoins}`);
      window.location.href = '/static/html/profile.html';
    } catch (error) {
      console.error('Booking error:', error);
      alert('Ошибка при создании бронирования: ' + error.message);
    }
  });
}

async function fetchAllPcs() {
  try {
    const response = await fetch('/api/v1/pcs/allPc', {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include'
    });

    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    return await response.json();
  } catch (error) {
    console.error('Error fetching PCs:', error);
    return [];
  }
}

async function fetchAvailableSeats() {
  if (!bookingData.date || !bookingData.startTime || !bookingData.endTime) {
    return new Set();
  }

  try {
    const startDateTime = getStartDateTimeISO();
    const endDateTime = getEndDateTimeISO();

    if (!startDateTime || !endDateTime) {
      return new Set();
    }

    const startQueryDate = bookingData.date;
    const endQueryDate = endDateTime.split('T')[0];

    const response = await fetch(
        `/api/v1/sessions/sessionsForInfo?startDate=${startQueryDate}&endDate=${endQueryDate}`,
        {
          method: 'GET',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include'
        }
    );

    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

    const sessions = await response.json();
    const occupiedPcIds = new Set();

    const bookingStart = new Date(startDateTime);
    const bookingEnd = new Date(endDateTime);

    sessions.forEach(session => {
      const sessionStart = new Date(session.startTime);
      const sessionEnd = new Date(session.endTime);

      const isOverlapping = sessionStart < bookingEnd && sessionEnd > bookingStart;
      if (isOverlapping) {
        occupiedPcIds.add(session.pcId);
      }
    });

    return occupiedPcIds;
  } catch (error) {
    console.error('Error fetching sessions:', error);
    return new Set();
  }
}


async function renderPCs() {
  const floor1Layout = qs('#floor1 .plan-layout');
  const floor2Layout = qs('#floor2 .plan-layout');

  if (!floor1Layout || !floor2Layout) return;

  try {
    const allPcs = await fetchAllPcs();
    if (allPcs.length === 0) return;

    const occupiedPcIds = await fetchAvailableSeats();

    qsa('.pc-seat').forEach(seat => seat.remove());
    qsa('.vip-room').forEach(room => room.remove());

    const floor1Pcs = allPcs.filter(pc => pc.room && !pc.room.vip);
    if (floor1Pcs.length > 0) {
      renderPcsForFloor(floor1Layout, floor1Pcs, occupiedPcIds, 1);
    }

    const vipRooms = getVipRooms(allPcs, occupiedPcIds);
    if (vipRooms.length > 0) {
      renderVipRooms(floor2Layout, vipRooms);
    }

    initSeatSelection();
  } catch (error) {
    console.error('Error rendering PCs:', error);
  }
}

function getVipRooms(allPcs, occupiedPcIds) {
  const vipPcs = allPcs.filter(pc => pc.room && pc.room.vip);
  const roomsMap = new Map();

  vipPcs.forEach(pc => {
    const roomId = pc.room.id;
    if (!roomsMap.has(roomId)) {
      roomsMap.set(roomId, {
        roomId: roomId,
        roomName: pc.room.name,
        pcs: [],
        availableCount: 0,
        occupiedCount: 0
      });
    }

    const room = roomsMap.get(roomId);
    room.pcs.push(pc);

    if (occupiedPcIds.has(pc.id)) {
      room.occupiedCount++;
    } else {
      room.availableCount++;
    }
  });

  return Array.from(roomsMap.values());
}

function renderVipRooms(floorLayout, vipRooms) {
  const fragment = document.createDocumentFragment();

  vipRooms.forEach((room, index) => {
    const roomElement = document.createElement('div');
    roomElement.className = `vip-room room-${index + 1}`;
    roomElement.setAttribute('data-room-id', room.roomId);

    const allOccupied = room.availableCount === 0;
    const statusClass = allOccupied ? 'fully-occupied' : 'has-available';
    roomElement.classList.add(statusClass);

    roomElement.innerHTML = `
      <div class="vip-header">
        <i class="fas fa-crown"></i>
        <span>${room.roomName}</span>
      </div>
      <div class="vip-status">
        <div class="status-item available">
          <i class="fas fa-check-circle"></i>
          <span>${room.availableCount} свободно</span>
        </div>
        <div class="status-item occupied">
          <i class="fas fa-times-circle"></i>
          <span>${room.occupiedCount} занято</span>
        </div>
      </div>
    `;

    roomElement.addEventListener('mouseenter', () => showVipRoomTooltip(roomElement, room));
    roomElement.addEventListener('mouseleave', () => hideVipRoomTooltip());
    roomElement.addEventListener('click', () => showPcSelectionModal(room));

    fragment.appendChild(roomElement);
  });

  floorLayout.appendChild(fragment);
}

function showPcSelectionModal(room) {
  const existingModal = qs('#pcSelectionModal');
  if (existingModal) existingModal.remove();

  const modal = document.createElement('div');
  modal.id = 'pcSelectionModal';
  modal.className = 'modal';

  const pcsHtml = room.pcs.map(pc => {
    const isOccupied = pc.isOccupied || false;
    const statusClass = isOccupied ? 'occupied' : 'available';
    const seatId = `2-${pc.id}`;
    const isSelected = bookingData.seats.includes(seatId);
    const selectedClass = isSelected ? 'selected' : '';

    return `
      <div class="modal-pc-card ${statusClass} ${selectedClass}" 
           data-pc-id="${pc.id}" 
           data-seat-id="${seatId}"
           data-pc-info='${JSON.stringify({
      name: pc.name,
      cpu: pc.cpu,
      gpu: pc.gpu,
      ram: pc.ram,
      monitor: pc.monitor,
      roomName: room.roomName,
      roomvip: true
    })}'>
        <div class="modal-pc-header">
          <i class="fas fa-desktop"></i>
          <span>${pc.name}</span>
          ${isOccupied ? '<span class="badge-occupied">Занято</span>' :
        isSelected ? '<span class="badge-selected">Выбрано</span>' :
            '<span class="badge-available">Свободно</span>'}
        </div>
        <div class="modal-pc-specs">
          <div><strong>CPU:</strong> ${pc.cpu}</div>
          <div><strong>GPU:</strong> ${pc.gpu}</div>
          <div><strong>RAM:</strong> ${pc.ram}</div>
          <div><strong>Monitor:</strong> ${pc.monitor}</div>
        </div>
      </div>
    `;
  }).join('');

  modal.innerHTML = `
    <div class="modal-overlay"></div>
    <div class="modal-content">
      <div class="modal-header">
        <h3><i class="fas fa-crown"></i> ${room.roomName}</h3>
        <button class="modal-close">&times;</button>
      </div>
      <div class="modal-body">
        <p class="modal-subtitle">Выберите компьютеры (можно несколько):</p>
        <div class="modal-pc-grid">${pcsHtml}</div>
      </div>
      <div class="modal-footer">
        <button class="modal-done-btn">Готово</button>
      </div>
    </div>
  `;

  document.body.appendChild(modal);

  const closeBtn = modal.querySelector('.modal-close');
  const overlay = modal.querySelector('.modal-overlay');
  const doneBtn = modal.querySelector('.modal-done-btn');

  closeBtn.addEventListener('click', () => modal.remove());
  overlay.addEventListener('click', () => modal.remove());
  doneBtn.addEventListener('click', () => modal.remove());

  const pcCards = modal.querySelectorAll('.modal-pc-card');
  pcCards.forEach(card => {
    if (!card.classList.contains('occupied')) {
      card.addEventListener('click', () => {
        const seatId = card.getAttribute('data-seat-id');
        const pcInfo = JSON.parse(card.getAttribute('data-pc-info'));
        const isAlreadySelected = card.classList.contains('selected');

        if (isAlreadySelected) {
          card.classList.remove('selected');
          const badge = card.querySelector('.badge-selected');
          if (badge) {
            badge.className = 'badge-available';
            badge.textContent = 'Свободно';
          }

          const index = bookingData.seats.indexOf(seatId);
          if (index > -1) {
            bookingData.seats.splice(index, 1);
            bookingData.pcsInfo.splice(index, 1);
          }
        } else {
          card.classList.add('selected');
          const badge = card.querySelector('.badge-available');
          if (badge) {
            badge.className = 'badge-selected';
            badge.textContent = 'Выбрано';
          }

          bookingData.seats.push(seatId);
          bookingData.pcsInfo.push(pcInfo);
        }

        updateBookingSummary();
      });
    }
  });
}

function showVipRoomTooltip(element, room) {
  let tooltip = document.getElementById('vip-room-tooltip');

  if (!tooltip) {
    tooltip = document.createElement('div');
    tooltip.id = 'vip-room-tooltip';
    tooltip.className = 'vip-room-tooltip';
    document.body.appendChild(tooltip);
  }

  const pcsHtml = room.pcs.map(pc => `
    <div class="tooltip-pc">
      <div class="tooltip-pc-name">
        <i class="fas fa-desktop"></i> ${pc.name}
      </div>
      <div class="tooltip-pc-specs">
        <div class="spec-row"><span class="spec-label">CPU:</span> <span class="spec-value">${pc.cpu}</span></div>
        <div class="spec-row"><span class="spec-label">GPU:</span> <span class="spec-value">${pc.gpu}</span></div>
        <div class="spec-row"><span class="spec-label">RAM:</span> <span class="spec-value">${pc.ram}</span></div>
        <div class="spec-row"><span class="spec-label">Monitor:</span> <span class="spec-value">${pc.monitor}</span></div>
      </div>
    </div>
  `).join('');

  tooltip.innerHTML = `
    <div class="tooltip-header"><i class="fas fa-crown"></i> ${room.roomName}</div>
    <div class="tooltip-stats">
      <span class="stat available">✓ ${room.availableCount} свободно</span>
      <span class="stat occupied">✗ ${room.occupiedCount} занято</span>
    </div>
    <div class="tooltip-divider"></div>
    <div class="tooltip-pcs">${pcsHtml}</div>
    <div class="tooltip-hint"><i class="fas fa-mouse-pointer"></i> Нажмите, чтобы выбрать ПК</div>
  `;

  const rect = element.getBoundingClientRect();
  tooltip.style.position = 'fixed';
  tooltip.style.left = (rect.left + rect.width / 2) + 'px';
  tooltip.style.top = (rect.top - 10) + 'px';
  tooltip.style.display = 'block';
  tooltip.style.transform = 'translateX(-50%) translateY(-100%)';
  tooltip.style.zIndex = '1000';
}

function hideVipRoomTooltip() {
  const tooltip = document.getElementById('vip-room-tooltip');
  if (tooltip) tooltip.style.display = 'none';
}

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

    const pcInfo = {
      name: pc.name,
      cpu: pc.cpu,
      gpu: pc.gpu,
      ram: pc.ram,
      monitor: pc.monitor,
      isEnabled: pc.isEnabled,
      roomName: pc.room?.name || 'Unknown',
      roomvip: pc.room?.vip || false
    };

    pcElement.setAttribute('data-pc-info', JSON.stringify(pcInfo));
    pcElement.innerHTML = `<i class="fas fa-desktop"></i><span>ПК ${pc.id}</span>`;

    pcElement.addEventListener('mouseenter', () => showPcTooltip(pcElement, pcInfo));
    pcElement.addEventListener('mouseleave', () => hidePcTooltip());

    fragment.appendChild(pcElement);
  });

  const existingElements = floorLayout.querySelectorAll('.room-element');
  if (existingElements.length > 0) {
    existingElements[existingElements.length - 1].after(...Array.from(fragment.childNodes));
  } else {
    floorLayout.appendChild(fragment);
  }
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
    <div class="tooltip-header">
      ${pcInfo.name}
      ${pcInfo.roomvip ? '<span class="vip-badge">VIP</span>' : ''}
    </div>
    <div class="tooltip-body">
      <div class="tooltip-row"><span class="tooltip-label">Комната:</span><span class="tooltip-value">${pcInfo.roomName}</span></div>
      <div class="tooltip-row"><span class="tooltip-label">CPU:</span><span class="tooltip-value">${pcInfo.cpu}</span></div>
      <div class="tooltip-row"><span class="tooltip-label">GPU:</span><span class="tooltip-value">${pcInfo.gpu}</span></div>
      <div class="tooltip-row"><span class="tooltip-label">RAM:</span><span class="tooltip-value">${pcInfo.ram}</span></div>
      <div class="tooltip-row"><span class="tooltip-label">Monitor:</span><span class="tooltip-value">${pcInfo.monitor}</span></div>
    </div>
  `;

  const rect = element.getBoundingClientRect();
  tooltip.style.position = 'fixed';
  tooltip.style.left = (rect.left + rect.width / 2) + 'px';
  tooltip.style.top = (rect.top - 10) + 'px';
  tooltip.style.display = 'block';
  tooltip.style.transform = 'translateX(-50%) translateY(-100%)';
  tooltip.style.zIndex = '1000';
}

function hidePcTooltip() {
  const tooltip = document.getElementById('pc-tooltip');
  if (tooltip) tooltip.style.display = 'none';
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
  await fetchUserProfile();
  await fetchTariffs();

  attachAuthHandlers();
  attachNavToggle();
  initTariffSelection();
  initDateTimeStep();
  initFloorSelection();
  initSeatSelection();
  initConfirmBooking();
  initBonusSystem();
  setYear();

  const profileContainer = qs('.auth__profile');
  profileContainer?.addEventListener('click', () => {
    window.location.href = '/static/html/profile.html';
  });

  updateBookingSummary();
}

document.addEventListener('DOMContentLoaded', initApp);
