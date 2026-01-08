const qs = (s, r = document) => r.querySelector(s);
const qsa = (s, r = document) => Array.from(r.querySelectorAll(s));

let allUsers = [];
let allProducts = [];
let allTariffs = [];
let allComputers = [];
let allSessions = [];
let allOrders = [];
let allPayments = [];
let currentUser = null;
let currentProduct = null;
let currentTariff = null;
let currentComputer = null;
let currentCategoryFilter = 'all';



function formatDateTime(dateStr) {
  if (!dateStr) return '-';
  try {
    const date = new Date(dateStr);
    return date.toLocaleString('ru-RU', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (e) {
    return dateStr;
  }
}

function getStatusBadge(status) {
  if (!status) {
    return '<span class="status-badge">Неизвестно</span>';
  }

  const normalizedStatus = status.toString().toLowerCase().replace(/\s+/g, '_');

  const statusConfigs = {
    'pending': {
      text: 'Ожидание',
      class: 'status-warning',
      icon: 'fas fa-clock'
    },
    'paid': {
      text: 'Оплачена',
      class: 'status-active',
      icon: 'fas fa-check-circle'
    },
    'in_progress': {
      text: 'В процессе',
      class: 'status-info',
      icon: 'fas fa-play-circle'
    },
    'completed': {
      text: 'Завершена',
      class: 'status-success',
      icon: 'fas fa-flag-checkered'
    },
    'not_show': {
      text: 'Не явился',
      class: 'status-banned',
      icon: 'fas fa-user-slash'
    },
    'cancelled': {
      text: 'Отменена',
      class: 'status-danger',
      icon: 'fas fa-times-circle'
    },
    'refunded': {
      text: 'Возврат',
      class: 'status-secondary',
      icon: 'fas fa-undo'
    },
    'error': {
      text: 'Ошибка',
      class: 'status-danger',
      icon: 'fas fa-exclamation-triangle'
    }
  };

  const config = statusConfigs[normalizedStatus];

  if (config) {
    return `
      <span class="status-badge ${config.class}">
        <i class="${config.icon}"></i>
        ${config.text}
      </span>
    `;
  }

  return `<span class="status-badge">${status}</span>`;
}


// ========== ТАРИФЫ ==========
async function fetchAllTariffs() {
  try {
    const response = await fetch('/api/v1/tariffs/allTariffs', {
      method: 'GET',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to fetch tariffs');
    }

    allTariffs = await response.json();
    console.log('Tariffs loaded:', allTariffs);
    renderTariffs(allTariffs);
  } catch (error) {
    console.error('Error fetching tariffs:', error);
    showError('Не удалось загрузить список тарифов');
  }
}

function renderTariffs(tariffs) {
  const container = qs('#tariffsContainer');
  if (!container) return;

  // Сортируем тарифы в обратном порядке (новые сверху)
  const reversedTariffs = [...tariffs].reverse();

  if (reversedTariffs.length === 0) {
    container.innerHTML = `
      <div style="text-align: center; padding: 3rem; color: var(--gray); grid-column: 1 / -1;">
        <i class="fas fa-tags" style="font-size: 3rem; margin-bottom: 1rem; opacity: 0.5;"></i>
        <p>Тарифы не найдены</p>
      </div>
    `;
    return;
  }

  container.innerHTML = reversedTariffs.map(tariff => {
    const vipBadge = tariff.isVip || tariff.vip
        ? '<span class="vip-badge"><i class="fas fa-crown"></i> VIP</span>'
        : '';

    const price = tariff.price || tariff.pricePerHour || 0;
    const hours = tariff.hours || 1;

    // Рассчитываем стоимость за час для информации
    const pricePerHour = hours > 0 ? parseFloat(price) / hours : 0;

    return `
      <div class="tariff-card" data-tariff-id="${tariff.tariffId}">
        <div class="tariff-header">
          <h3>${tariff.name}</h3>
          ${vipBadge}
        </div>
        <div class="tariff-info">
          <div class="tariff-details">
            <div class="tariff-hours">
              <i class="fas fa-clock"></i>
              <span>${hours} час${getHoursSuffix(hours)}</span>
            </div>
            <div class="tariff-price-breakdown">
              <div class="total-price">
                ${parseFloat(price).toFixed(2)} BYN
              </div>
              <div class="price-per-hour">
                ${pricePerHour.toFixed(2)} BYN/час
              </div>
            </div>
          </div>
        </div>
        <div class="tariff-actions">
          <button class="btn-icon btn-primary" title="Редактировать" onclick="handleEditTariff(${tariff.tariffId})">
            <i class="fas fa-edit"></i>
          </button>
          <button class="btn-icon btn-danger" title="Удалить" onclick="handleDeleteTariff(${tariff.tariffId})">
            <i class="fas fa-trash"></i>
          </button>
        </div>
      </div>
    `;
  }).join('');
}

// Вспомогательная функция для правильного склонения часов
function getHoursSuffix(hours) {
  if (hours % 10 === 1 && hours % 100 !== 11) return '';
  if (hours % 10 >= 2 && hours % 10 <= 4 && (hours % 100 < 10 || hours % 100 >= 20)) return 'а';
  return 'ов';
}

function openAddTariffModal() {
  const modal = qs('#tariffModal');
  const form = qs('#tariffForm');
  const header = modal?.querySelector('.modal-header h2');

  if (header) header.textContent = 'Добавить тариф';
  if (form) form.reset();

  currentTariff = null;

  if (modal) modal.classList.add('active');
}

async function handleAddTariff(event) {
  event.preventDefault();

  const form = qs('#tariffForm');
  const name = form.querySelector('#tariffName')?.value.trim();
  const price = form.querySelector('#tariffPrice')?.value;
  const hours = form.querySelector('#tariffHours')?.value;
  const isVip = form.querySelector('#tariffVip')?.checked || false;

  // Валидация
  if (!name || name.length < 2 || name.length > 100) {
    showError('Название тарифа должно быть от 2 до 100 символов');
    return;
  }

  if (!price || parseFloat(price) <= 0) {
    showError('Введите корректную стоимость пакета (больше 0)');
    return;
  }

  if (!hours || parseInt(hours) <= 0) {
    showError('Введите корректное количество часов (больше 0)');
    return;
  }

  const tariffData = {
    name: name,
    price: parseFloat(price),
    hours: parseInt(hours),
    isVip: isVip
  };

  try {
    const response = await fetch('/api/v1/tariffs/createTariff', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(tariffData)
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Failed to create tariff');
    }

    showSuccess('Тариф успешно создан');
    closeTariffModal();
    await fetchAllTariffs();
  } catch (error) {
    console.error('Error creating tariff:', error);
    showError('Не удалось создать тариф: ' + error.message);
  }
}

function handleEditTariff(tariffId) {
  currentTariff = allTariffs.find(t => t.tariffId === tariffId);
  if (!currentTariff) {
    showError('Тариф не найден');
    return;
  }

  const modal = qs('#tariffModal');
  const form = qs('#tariffForm');
  const header = modal?.querySelector('.modal-header h2');

  if (header) header.textContent = 'Редактировать тариф';

  if (form) {
    form.querySelector('#tariffName').value = currentTariff.name;
    form.querySelector('#tariffPrice').value = currentTariff.price || currentTariff.pricePerHour;
    form.querySelector('#tariffHours').value = currentTariff.hours || 1;
    form.querySelector('#tariffVip').checked = currentTariff.isVip || currentTariff.vip || false;
  }

  if (modal) modal.classList.add('active');
}

async function handleUpdateTariff(event) {
  event.preventDefault();

  if (!currentTariff) {
    showError('Тариф не выбран для редактирования');
    return;
  }

  const form = qs('#tariffForm');
  const name = form.querySelector('#tariffName')?.value.trim();
  const price = form.querySelector('#tariffPrice')?.value;
  const hours = form.querySelector('#tariffHours')?.value;
  const isVip = form.querySelector('#tariffVip')?.checked || false;

  // Валидация
  if (!name || name.length < 2 || name.length > 100) {
    showError('Название тарифа должно быть от 2 до 100 символов');
    return;
  }

  if (!price || parseFloat(price) <= 0) {
    showError('Введите корректную стоимость пакета (больше 0)');
    return;
  }

  if (!hours || parseInt(hours) <= 0) {
    showError('Введите корректное количество часов (больше 0)');
    return;
  }

  const tariffData = {
    name: name,
    price: parseFloat(price), // Фиксированная цена за весь пакет
    hours: parseInt(hours),
    isVip: isVip
  };

  try {
    const response = await fetch(`/api/v1/tariffs/updateTariff/${currentTariff.tariffId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(tariffData)
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Failed to update tariff');
    }

    showSuccess('Тариф успешно обновлён');
    closeTariffModal();
    await fetchAllTariffs();
  } catch (error) {
    console.error('Error updating tariff:', error);
    showError('Не удалось обновить тариф: ' + error.message);
  }
}

async function handleDeleteTariff(tariffId) {
  if (!confirm('Вы уверены, что хотите удалить этот тариф?')) return;

  try {
    const response = await fetch(`/api/v1/tariffs/deleteTariff/${tariffId}`, {
      method: 'DELETE',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to delete tariff');
    }

    showSuccess('Тариф удалён');
    await fetchAllTariffs();
  } catch (error) {
    console.error('Error deleting tariff:', error);
    showError('Не удалось удалить тариф');
  }
}

function closeTariffModal() {
  const modal = qs('#tariffModal');
  if (modal) modal.classList.remove('active');
  currentTariff = null;
}

// ========== КОМПЬЮТЕРЫ ==========
async function fetchAllComputers() {
  const tbody = qs('#computersTableBody');
  if (tbody) {
    tbody.dataset.loading = 'true';
  }

  try {
    const response = await fetch('/api/v1/pcs/allPc', {
      method: 'GET',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to fetch computers');
    }

    allComputers = await response.json();
    // Сортируем по ID для стабильного порядка
    allComputers.sort((a, b) => (a.id || 0) - (b.id || 0));
    renderComputers(allComputers);
    console.log('Computers loaded:', allComputers);
  } catch (error) {
    console.error('Error fetching computers:', error);
    showError('Не удалось загрузить список компьютеров');
  } finally {
    if (tbody) {
      delete tbody.dataset.loading;
    }
  }
}

function renderComputers(computers) {
  const tbody = qs('#computersTableBody');
  if (!tbody) return;

  if (!computers || computers.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="8" style="text-align: center; padding: 2rem; color: var(--gray);">
          Компьютеры не найдены
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = computers.map(pc => {
    const roomName = pc.room?.name || 'Не указано';
    const isVip = pc.room?.isVip || pc.room?.vip || false;
    const isActive = pc.isEnabled ?? pc.enabled ?? true;
    const statusClass = isActive ? 'status-active' : 'status-inactive';
    const statusText = isActive ? 'Активен' : 'Неактивен';
    const toggleTitle = isActive ? 'Деактивировать' : 'Активировать';
    const toggleIcon = isActive ? 'fa-power-off' : 'fa-bolt';
    const toggleBtnClass = isActive ? 'btn-warning' : 'btn-success';

    return `
      <tr data-pc-id="${pc.id}">
        <td>#${pc.id}</td>
        <td>
          <div class="pc-name">${pc.name || 'Без названия'}</div>
          <span class="room-badge ${isVip ? 'vip' : ''}">
            <i class="fas fa-door-open"></i>
            ${roomName}
          </span>
        </td>
        <td>
          <span class="spec-chip"><i class="fas fa-microchip"></i>${pc.cpu || '-'}</span>
        </td>
        <td>
          <span class="spec-chip"><i class="fas fa-chart-area"></i>${pc.gpu || '-'}</span>
        </td>
        <td>
          <span class="spec-chip"><i class="fas fa-memory"></i>${pc.ram || '-'}</span>
        </td>
        <td>
          <span class="spec-chip"><i class="fas fa-tv"></i>${pc.monitor || '-'}</span>
        </td>
        <td><span class="status-badge ${statusClass}">${statusText}</span></td>
        <td>
          <div class="action-buttons">
            <button class="btn-icon btn-primary" title="Редактировать" onclick="handleEditComputer(${pc.id})">
              <i class="fas fa-edit"></i>
            </button>
            <button class="btn-icon ${toggleBtnClass}" title="${toggleTitle}" onclick="handleToggleComputerStatus(${pc.id})">
              <i class="fas ${toggleIcon}"></i>
            </button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

function handleEditComputer(pcId) {
  currentComputer = allComputers.find(pc => pc.id === pcId);
  if (!currentComputer) {
    showError('Компьютер не найден');
    return;
  }

  const modal = qs('#editComputerModal');
  const form = qs('#editComputerForm');
  if (!modal || !form) return;

  form.querySelector('#editComputerName').value = currentComputer.name || '';
  form.querySelector('#editComputerCpu').value = currentComputer.cpu || '';
  form.querySelector('#editComputerGpu').value = currentComputer.gpu || '';
  form.querySelector('#editComputerRam').value = currentComputer.ram || '';
  form.querySelector('#editComputerMonitor').value = currentComputer.monitor || '';
  form.querySelector('#editComputerRoomName').value = currentComputer.room?.name || 'Не указано';
  form.querySelector('#editComputerRoomId').value = currentComputer.room?.id || '';
  const currentEnabled = currentComputer.isEnabled ?? currentComputer.enabled ?? true;
  form.querySelector('#editComputerStatus').checked = currentEnabled;

  modal.classList.add('active');
}

async function handleUpdateComputer(event) {
  event.preventDefault();

  if (!currentComputer) {
    showError('Компьютер не выбран для редактирования');
    return;
  }

  const form = event.target;
  const name = form.querySelector('#editComputerName')?.value.trim();
  const cpu = form.querySelector('#editComputerCpu')?.value.trim();
  const gpu = form.querySelector('#editComputerGpu')?.value.trim();
  const ram = form.querySelector('#editComputerRam')?.value.trim();
  const monitor = form.querySelector('#editComputerMonitor')?.value.trim();
  const status = form.querySelector('#editComputerStatus')?.checked ?? true;
  const roomIdValue = form.querySelector('#editComputerRoomId')?.value || currentComputer.room?.id;
  const roomId = roomIdValue ? parseInt(roomIdValue, 10) : null;

  if (!name || !cpu || !gpu || !ram || !monitor || !roomId) {
    showError('Заполните все обязательные поля для компьютера');
    return;
  }

  const payload = {
    name,
    roomId,
    cpu,
    gpu,
    ram,
    monitor,
    isEnabled: status,
    isOccupied: false
  };

  try {
    const response = await fetch(`/api/v1/pcs/updatePc/${currentComputer.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Failed to update computer');
    }

    showSuccess('Компьютер обновлён');
    closeEditComputerModal();
    await fetchAllComputers();
  } catch (error) {
    console.error('Error updating computer:', error);
    showError('Не удалось обновить компьютер: ' + error.message);
  }
}

async function handleToggleComputerStatus(pcId) {
  const pc = allComputers.find(item => item.id === pcId);
  if (!pc) {
    showError('Компьютер не найден');
    return;
  }

  const isActive = pc.isEnabled ?? pc.enabled ?? true;
  const endpoint = isActive ? `/api/v1/pcs/disablePs/${pcId}` : `/api/v1/pcs/activatePs/${pcId}`;
  const actionText = isActive ? 'деактивирован' : 'активирован';

  try {
    const response = await fetch(endpoint, {
      method: 'PUT',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to toggle PC status');
    }

    showSuccess(`Компьютер ${actionText}`);
    await fetchAllComputers();
  } catch (error) {
    console.error('Error toggling computer status:', error);
    showError('Не удалось изменить статус компьютера');
  }
}

function closeEditComputerModal() {
  const modal = qs('#editComputerModal');
  modal?.classList.remove('active');
  currentComputer = null;
}

function initComputerSearch() {
  const searchInput = qs('#computerSearch');
  if (!searchInput) return;

  searchInput.addEventListener('input', (e) => {
    const query = e.target.value.toLowerCase().trim();

    if (!query) {
      renderComputers(allComputers);
      return;
    }

    const filtered = allComputers.filter(pc => {
      const name = (pc.name || '').toLowerCase();
      const cpu = (pc.cpu || '').toLowerCase();
      const gpu = (pc.gpu || '').toLowerCase();
      const room = (pc.room?.name || '').toLowerCase();

      return name.includes(query) || cpu.includes(query) || gpu.includes(query) || room.includes(query);
    });

    renderComputers(filtered);
  });
}

// ========== ПОЛЬЗОВАТЕЛИ ==========
async function fetchAllUsers() {
  try {
    const response = await fetch('/api/v1/users/getAllUsers', {
      method: 'GET',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to fetch users');
    }

    allUsers = await response.json();
    console.log('Users loaded:', allUsers);
    renderUsers(allUsers);
  } catch (error) {
    console.error('Error fetching users:', error);
    showError('Не удалось загрузить список пользователей');
  }
}


async function fetchAllSessions() {
  try {
    const response = await fetch('/api/v1/admin/sessions/getAllSessions', {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Accept': 'application/json'
      }
    });

    if (!response.ok) {
      throw new Error(`Ошибка сервера: ${response.status} ${response.statusText}`);
    }

    allSessions = await response.json();
    console.log('Sessions loaded:', allSessions);
    renderSessions(allSessions);
  } catch (error) {
    console.error('Error fetching sessions:', error);
    showError(`Не удалось загрузить список сессий: ${error.message}`);
  }
}

function renderSessions(sessions) {
  const tbody = qs('#sessionsTableBody');
  if (!tbody) return;

  if (!sessions || sessions.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="9" style="text-align: center; padding: 2rem; color: var(--gray);">
          Сессии не найдены
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = sessions.map(s => {
    const pc = s.pcdto;
    const pcName = pc?.name || `#${s.pcId || '-'}`;
    const tariffName = s.tariff?.name || 'Неизвестный тариф';
    const start = s.startTime || '';
    const end = s.endTime || '';
    const price = (s.totalCost || 0).toFixed(2);
    const status = s.status || 'UNKNOWN';
    const userName = s.user?.login || s.user?.username || `ID ${s.userId || '-'}`;

    return `
      <tr data-session-id="${s.sessionId}">
        <td>${s.sessionId}</td>
        <td>${userName}</td>
        <td>${pcName}</td>
        <td>${tariffName}</td>
        <td>${formatDateTime(start)}</td>
        <td>${end ? formatDateTime(end) : '-'}</td>
        <td>${price} BYN</td>
        <td>${getStatusBadge(status)}</td>
        <td>
          <button class="btn-icon btn-primary" title="Подробнее" onclick="openSessionDetailsModal('${s.sessionId}')">
            <i class="fas fa-eye"></i>
          </button>
        </td>
      </tr>
    `;
  }).join('');
}

function openSessionDetailsModal(sessionId) {
  const modal = qs('#sessionDetailsModal');
  const body = qs('#sessionDetailsBody');
  if (!modal || !body) return;

  const session = allSessions.find(s => s.sessionId == sessionId);
  if (!session) {
    showError('Сессия не найдена');
    return;
  }

  const pc = session.pcdto;
  const room = pc?.roomDTO || pc?.room;
  const tariff = session.tariff;
  const user = session.user;

  body.innerHTML = `
    <div class="session-details">
      <!-- Информация о сессии -->
      <div class="details-section">
        <h4><i class="fas fa-clock"></i> Информация о сессии</h4>
        <div class="details-grid">
          <div class="detail-item">
            <span class="detail-label">ID сессии:</span>
            <span class="detail-value">#${session.sessionId}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Статус:</span>
            <span class="detail-value">${getStatusBadge(session.status)}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Начало:</span>
            <span class="detail-value">${formatDateTime(session.startTime)}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Конец:</span>
            <span class="detail-value">${session.endTime ? formatDateTime(session.endTime) : '-'}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Стоимость:</span>
            <span class="detail-value"><strong>${(session.totalCost || 0).toFixed(2)} BYN</strong></span>
          </div>
        </div>
      </div>

      <!-- Информация о пользователе -->
      <div class="details-section">
        <h4><i class="fas fa-user"></i> Пользователь</h4>
        <div class="details-grid">
          <div class="detail-item">
            <span class="detail-label">ID:</span>
            <span class="detail-value">${session.userId || '-'}</span>
          </div>
          ${user ? `
          <div class="detail-item">
            <span class="detail-label">Логин:</span>
            <span class="detail-value">${user.login || user.username || '-'}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Email:</span>
            <span class="detail-value">${user.email || '-'}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Баланс:</span>
            <span class="detail-value">${(user.wallet || 0).toFixed(2)} BYN</span>
          </div>
          ` : `
          <div class="detail-item">
            <span class="detail-label">Информация:</span>
            <span class="detail-value" style="color: var(--gray);">Пользователь не найден</span>
          </div>
          `}
        </div>
      </div>

      <!-- Информация о компьютере -->
      <div class="details-section">
        <h4><i class="fas fa-desktop"></i> Компьютер</h4>
        ${pc ? `
        <div class="details-grid">
          <div class="detail-item">
            <span class="detail-label">ID:</span>
            <span class="detail-value">#${pc.id || session.pcId}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Название:</span>
            <span class="detail-value">${pc.name || '-'}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">CPU:</span>
            <span class="detail-value"><i class="fas fa-microchip"></i> ${pc.cpu || '-'}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">GPU:</span>
            <span class="detail-value"><i class="fas fa-chart-area"></i> ${pc.gpu || '-'}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">RAM:</span>
            <span class="detail-value"><i class="fas fa-memory"></i> ${pc.ram || '-'}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Монитор:</span>
            <span class="detail-value"><i class="fas fa-tv"></i> ${pc.monitor || '-'}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Статус:</span>
            <span class="detail-value">
              ${pc.isEnabled !== false ? '<span class="status-badge status-active">Активен</span>' : '<span class="status-badge status-inactive">Неактивен</span>'}
            </span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Занят:</span>
            <span class="detail-value">
              ${pc.isOccupied ? '<span class="status-badge status-banned">Да</span>' : '<span class="status-badge status-active">Нет</span>'}
            </span>
          </div>
        </div>
        ` : `
        <div class="detail-item">
          <span class="detail-value" style="color: var(--gray);">Информация о компьютере недоступна</span>
        </div>
        `}
      </div>

      <!-- Информация о комнате -->
      <div class="details-section">
        <h4><i class="fas fa-door-open"></i> Комната</h4>
        ${room ? `
        <div class="details-grid">
          <div class="detail-item">
            <span class="detail-label">ID:</span>
            <span class="detail-value">#${room.id || '-'}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Название:</span>
            <span class="detail-value">
              ${room.name || '-'}
              ${room.isVip ? ' <span class="vip-badge"><i class="fas fa-crown"></i> VIP</span>' : ''}
            </span>
          </div>
          <div class="detail-item">
            <span class="detail-label">VIP:</span>
            <span class="detail-value">
              ${room.isVip ? '<span class="status-badge status-active">Да</span>' : '<span class="status-badge">Нет</span>'}
            </span>
          </div>
        </div>
        ` : `
        <div class="detail-item">
          <span class="detail-value" style="color: var(--gray);">Информация о комнате недоступна</span>
        </div>
        `}
      </div>

      <!-- Информация о тарифе -->
      <div class="details-section">
        <h4><i class="fas fa-ticket-alt"></i> Тариф</h4>
        ${tariff ? `
        <div class="details-grid">
          <div class="detail-item">
            <span class="detail-label">Название:</span>
            <span class="detail-value">
              ${tariff.name || '-'}
              ${tariff.isVip ? ' <span class="vip-badge"><i class="fas fa-crown"></i> VIP</span>' : ''}
            </span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Стоимость за час:</span>
            <span class="detail-value"><strong>${(tariff.price || 0).toFixed(2)} BYN</strong></span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Количество часов:</span>
            <span class="detail-value"><i class="fas fa-clock"></i> ${tariff.hours || '-'} час${tariff.hours === 1 ? '' : tariff.hours >= 2 && tariff.hours <= 4 ? 'а' : 'ов'}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">VIP тариф:</span>
            <span class="detail-value">
              ${tariff.isVip ? '<span class="status-badge status-active">Да</span>' : '<span class="status-badge">Нет</span>'}
            </span>
          </div>
        </div>
        ` : `
        <div class="detail-item">
          <span class="detail-value" style="color: var(--gray);">Информация о тарифе недоступна</span>
        </div>
        `}
      </div>
    </div>
  `;

  modal.classList.add('active');
}


function closeSessionDetailsModal() {
  const modal = qs('#sessionDetailsModal');
  if (modal) modal.classList.remove('active');
}

// ========== ПЛАТЕЖИ ==========
async function fetchAllPayments() {
  try {
    const response = await fetch('/api/v1/admin/payments/getAllPayments', {
      method: 'GET',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to fetch payments');
    }

    allPayments = await response.json();
    console.log('Payments loaded:', allPayments);
    renderPayments(allPayments);
  } catch (error) {
    console.error('Error fetching payments:', error);
    showError('Не удалось загрузить платежи');
  }
}

function renderPayments(payments) {
  const tbody = qs('#paymentsTableBody');
  if (!tbody) return;

  if (!payments || payments.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7" style="text-align: center; padding: 2rem; color: var(--gray);">
          Платежи не найдены
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = payments.map(p => {
    const payment = p.paymentDTO || p.payment || p;
    const userName = payment.user?.login || payment.user?.username || `ID ${payment.userId || '-'}`;
    const type = payment.paymentType || payment.type || '-';
    const amount = (payment.amount || payment.total || 0).toFixed(2);
    const status = payment.status || 'UNKNOWN';
    const date = payment.createdAt || payment.createdDate || payment.date || '';

    return `
      <tr data-payment-id="${payment.paymentId || payment.id}">
        <td>${payment.paymentId || payment.id}</td>
        <td>${userName}</td>
        <td>${type}</td>
        <td>${amount} BYN</td>
        <td>${status}</td>
        <td>${date}</td>
        <td>
          <button class="btn-icon btn-primary" title="Подробнее" onclick="openPaymentDetailsModal('${payment.paymentId || payment.id}')">
            <i class="fas fa-eye"></i>
          </button>
        </td>
      </tr>
    `;
  }).join('');
}

function openPaymentDetailsModal(paymentId) {
  const modal = qs('#paymentDetailsModal');
  const body = qs('#paymentDetailsBody');
  if (!modal || !body) return;

  const wrapper = allPayments.find(p => {
    const payment = p.paymentDTO || p.payment || p;
    return (payment.paymentId || payment.id) == paymentId;
  });
  const payment = wrapper?.paymentDTO || wrapper?.payment || wrapper;

  if (!payment) {
    showError('Платеж не найден');
    return;
  }

  body.innerHTML = `
    <div class="details-grid">
      <div><strong>ID платежа:</strong> ${payment.paymentId || payment.id}</div>
      <div><strong>Пользователь:</strong> ${payment.user?.login || payment.user?.username || payment.userId || '-'}</div>
      <div><strong>Тип платежа:</strong> ${payment.paymentType || payment.type || '-'}</div>
      <div><strong>Сумма:</strong> ${(payment.amount || payment.total || 0).toFixed(2)} BYN</div>
      <div><strong>Статус:</strong> ${payment.status || 'UNKNOWN'}</div>
      <div><strong>Дата:</strong> ${payment.createdAt || payment.createdDate || payment.date || '-'}</div>
    </div>
  `;

  modal.classList.add('active');
}

function closePaymentDetailsModal() {
  const modal = qs('#paymentDetailsModal');
  if (modal) modal.classList.remove('active');
}

function renderUsers(users) {
  const tbody = qs('#usersTableBody');
  if (!tbody) return;

  if (users.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7" style="text-align: center; padding: 2rem; color: var(--gray);">
          Пользователи не найдены
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = users.map(user => {
    const isBanned = user.banned || user.isBanned || false;
    const statusClass = isBanned ? 'status-banned' : 'status-active';
    const statusText = isBanned ? 'Заблокирован' : 'Активен';

    return `
      <tr data-user-id="${user.id}">
        <td>${user.id}</td>
        <td>
          <div style="display: flex; align-items: center; gap: 0.75rem;">
            <img src="${user.photoPath || 'https://i.pravatar.cc/40?img=' + user.id}" 
                 alt="${user.login}" 
                 style="width: 40px; height: 40px; border-radius: 50%; object-fit: cover; border: 2px solid var(--primary);">
            <span>${user.login}</span>
          </div>
        </td>
        <td>${user.email || '-'}</td>
        <td>${(user.wallet || 0).toFixed(2)} BYN</td>
        <td>${user.bonusCoins || 0}</td>
        <td><span class="status-badge ${statusClass}">${statusText}</span></td>
        <td>
          <div class="action-buttons">
            ${isBanned ? `
              <button class="btn-icon btn-success" title="Разблокировать" onclick="handleUnbanUser(${user.id})">
                <i class="fas fa-unlock"></i>
              </button>
            ` : `
              <button class="btn-icon btn-warning" title="Заблокировать" onclick="handleBanUser(${user.id})">
                <i class="fas fa-ban"></i>
              </button>
            `}
            <button class="btn-icon btn-success" title="Добавить бонусы" onclick="handleAddBonus(${user.id}, '${user.login}')">
              <i class="fas fa-coins"></i>
            </button>
            <button class="btn-icon btn-danger" title="Удалить" onclick="handleDeleteUser(${user.id})">
              <i class="fas fa-trash"></i>
            </button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

async function handleBanUser(userId) {
  if (!confirm('Вы уверены, что хотите заблокировать пользователя?')) return;

  try {
    const response = await fetch(`/api/v1/users/blockUser/${userId}`, {
      method: 'PUT',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to ban user');
    }

    showSuccess('Пользователь заблокирован');
    await fetchAllUsers();
  } catch (error) {
    console.error('Error banning user:', error);
    showError('Не удалось заблокировать пользователя');
  }
}

async function handleUnbanUser(userId) {
  if (!confirm('Вы уверены, что хотите разблокировать пользователя?')) return;

  try {
    const response = await fetch(`/api/v1/users/unBlockUser/${userId}`, {
      method: 'PUT',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to unban user');
    }

    showSuccess('Пользователь разблокирован');
    await fetchAllUsers();
  } catch (error) {
    console.error('Error unbanning user:', error);
    showError('Не удалось разблокировать пользователя');
  }
}

async function submitBonus() {
  const modal = qs('#bonusModal');
  const amountInput = modal?.querySelector('input[type="number"]');
  const amount = parseInt(amountInput?.value);

  if (!amount || amount <= 0) {
    showError('Введите корректное количество бонусов');
    return;
  }

  if (!currentUser) {
    showError('Пользователь не выбран');
    return;
  }

  try {
    const response = await fetch(`/api/v1/users/coins/${currentUser.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ coins: amount })
    });

    if (!response.ok) {
      throw new Error('Failed to add bonus');
    }

    showSuccess(`Добавлено ${amount} бонусов пользователю ${currentUser.login}`);
    modal.classList.remove('active');
    amountInput.value = '';
    await fetchAllUsers();
  } catch (error) {
    console.error('Error adding bonus:', error);
    showError('Не удалось добавить бонусы');
  }
}

async function handleDeleteUser(userId) {
  if (!confirm('Вы уверены, что хотите удалить пользователя? Это действие необратимо.')) return;

  try {
    const response = await fetch(`/api/v1/users/${userId}`, {
      method: 'DELETE',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to delete user');
    }

    showSuccess('Пользователь удалён');
    await fetchAllUsers();
  } catch (error) {
    console.error('Error deleting user:', error);
    showError('Не удалось удалить пользователя');
  }
}

function handleAddBonus(userId, username) {
  currentUser = { id: userId, login: username };
  const modal = qs('#bonusModal');
  const usernameInput = modal?.querySelector('.form-input[readonly]');
  if (usernameInput) {
    usernameInput.value = username;
  }
  if (modal) modal.classList.add('active');
}

// ========== ТОВАРЫ ==========
async function fetchAllProducts() {
  try {
    const response = await fetch('/api/v1/products/getAllProducts', {
      method: 'GET',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to fetch products');
    }

    allProducts = await response.json();
    console.log('Products loaded:', allProducts);
    renderProducts(allProducts);
  } catch (error) {
    console.error('Error fetching products:', error);
    showError('Не удалось загрузить список товаров');
  }
}

function renderProducts(products) {
  const container = qs('#productsContainer');
  if (!container) return;

  let filtered = products;
  if (currentCategoryFilter !== 'all') {
    filtered = products.filter(p => p.categoryId == currentCategoryFilter || p.category?.id == currentCategoryFilter);
  }

  if (filtered.length === 0) {
    container.innerHTML = `
      <div style="text-align: center; padding: 3rem; color: var(--gray); grid-column: 1 / -1;">
        <i class="fas fa-box-open" style="font-size: 3rem; margin-bottom: 1rem; opacity: 0.5;"></i>
        <p>Товары не найдены</p>
      </div>
    `;
    return;
  }

  container.innerHTML = filtered.map(product => {
    const isActive = product.active !== false;
    const statusClass = isActive ? 'status-active' : 'status-inactive';
    const statusText = isActive ? 'Активен' : 'Неактивен';

    const imagePath = product.photoPath
        ? product.photoPath
        : 'https://via.placeholder.com/150';

    return `
    <div class="product-card" data-product-id="${product.id}">
      <img src="${imagePath}" alt="${product.name}" class="product-image">
      <div class="product-info">
        <h3>${product.name}</h3>
        <p class="product-price">${(product.price || 0).toFixed(2)} BYN</p>
        <p class="product-stock">В наличии: ${product.stock || 0} шт</p>
        <span class="status-badge ${statusClass}">${statusText}</span>
      </div>
      <div class="product-actions">
        <button class="btn-icon btn-primary" title="Редактировать" onclick="handleEditProduct(${product.id})">
          <i class="fas fa-edit"></i>
        </button>
        ${isActive ? `
          <button class="btn-icon btn-warning" title="В неактивные" onclick="handleToggleProductStatus(${product.id})">
            <i class="fas fa-pause"></i>
          </button>
        ` : `
          <button class="btn-icon btn-success" title="В активные" onclick="handleToggleProductStatus(${product.id})">
            <i class="fas fa-play"></i>
          </button>
        `}
        <button class="btn-icon btn-danger" title="Удалить" onclick="handleDeleteProduct(${product.id})">
          <i class="fas fa-trash"></i>
        </button>
      </div>
    </div>
  `;
  }).join('');
}

function openAddProductModal() {
  const modal = qs('#productModal');
  const form = qs('#productForm');
  const header = modal.querySelector('.modal-header h2');
  const imageGroup = qs('#productImageGroup');

  header.textContent = 'Добавить товар';
  imageGroup.style.display = 'block';
  form.reset();

  modal.classList.add('active');
}

async function handleAddProduct(event) {
  event.preventDefault();

  const form = qs('#productForm');
  const name = form.querySelector('#productName').value;
  const price = form.querySelector('#productPrice').value;
  const stock = form.querySelector('#productStock').value;
  const categoryId = form.querySelector('#productCategory').value;
  const imageFile = form.querySelector('#productImage').files[0];

  if (!name || !price || !stock || !categoryId) {
    showError('Заполните все обязательные поля');
    return;
  }

  if (!imageFile) {
    showError('Загрузите изображение товара');
    return;
  }

  const formData = new FormData();
  formData.append('image', imageFile);

  const productData = {
    name: name,
    price: parseFloat(price),
    stock: parseInt(stock),
    categoryId: parseInt(categoryId),
    active: true
  };

  formData.append('product', new Blob([JSON.stringify(productData)], {
    type: 'application/json'
  }));

  try {
    const response = await fetch('/api/v1/products/createProduct', {
      method: 'POST',
      credentials: 'include',
      body: formData
    });

    if (!response.ok) {
      throw new Error('Failed to create product');
    }

    showSuccess('Товар успешно создан');
    closeProductModal();
    await fetchAllProducts();
  } catch (error) {
    console.error('Error creating product:', error);
    showError('Не удалось создать товар');
  }
}

function handleEditProduct(productId) {
  currentProduct = allProducts.find(p => p.id === productId);
  if (!currentProduct) return;

  const modal = qs('#editProductModal');
  const form = qs('#editProductForm');

  form.querySelector('#editProductName').value = currentProduct.name;
  form.querySelector('#editProductPrice').value = currentProduct.price;
  form.querySelector('#editProductStock').value = currentProduct.stock;
  form.querySelector('#editProductCategory').value = currentProduct.categoryId || currentProduct.category?.id || '';

  modal.classList.add('active');
}

async function handleUpdateProduct(event) {
  event.preventDefault();

  const form = qs('#editProductForm');
  const name = form.querySelector('#editProductName').value;
  const price = form.querySelector('#editProductPrice').value;
  const categoryId = form.querySelector('#editProductCategory').value;

  if (!name || !price || !categoryId) {
    showError('Заполните все обязательные поля');
    return;
  }

  const productData = {
    name: name,
    price: parseFloat(price),
    categoryId: parseInt(categoryId),
    isAvailable: true
  };

  try {
    const response = await fetch(`/api/v1/products/updateProduct/${currentProduct.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(productData)
    });

    if (!response.ok) {
      throw new Error('Failed to update product');
    }

    showSuccess('Товар успешно обновлён');
    closeEditProductModal();
    await fetchAllProducts();
  } catch (error) {
    console.error('Error updating product:', error);
    showError('Не удалось обновить товар');
  }
}

async function handleToggleProductStatus(productId) {
  try {
    const response = await fetch(`/api/v1/products/updateAvailableStatus/${productId}`, {
      method: 'PUT',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to toggle status');
    }

    showSuccess('Статус товара изменён');
    await fetchAllProducts();
  } catch (error) {
    console.error('Error toggling product status:', error);
    showError('Не удалось изменить статус товара');
  }
}

async function handleDeleteProduct(productId) {
  if (!confirm('Вы уверены, что хотите удалить товар?')) return;

  try {
    const response = await fetch(`/api/v1/products/deleteProduct/${productId}`, {
      method: 'DELETE',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to delete product');
    }

    showSuccess('Товар удалён');
    await fetchAllProducts();
  } catch (error) {
    console.error('Error deleting product:', error);
    showError('Не удалось удалить товар');
  }
}

function closeProductModal() {
  const modal = qs('#productModal');
  modal.classList.remove('active');
}

function closeEditProductModal() {
  const modal = qs('#editProductModal');
  modal.classList.remove('active');
}

// ========== УТИЛИТЫ ==========
function initCategoryFilter() {
  const filterButtons = qsa('#categoryFilters .filter-btn');

  filterButtons.forEach(btn => {
    btn.addEventListener('click', (e) => {
      filterButtons.forEach(b => b.classList.remove('active'));
      e.target.classList.add('active');
      currentCategoryFilter = e.target.getAttribute('data-category');
      renderProducts(allProducts);
    });
  });
}

function initSearch() {
  const searchInput = qs('#userSearch');
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      const query = e.target.value.toLowerCase().trim();

      if (!query) {
        renderUsers(allUsers);
        return;
      }

      const filtered = allUsers.filter(user => {
        const login = (user.login || '').toLowerCase();
        const email = (user.email || '').toLowerCase();
        const fullName = (user.fullName || '').toLowerCase();

        return login.includes(query) || email.includes(query) || fullName.includes(query);
      });

      renderUsers(filtered);
    });
  }
}

function initTariffPreview() {
  const form = qs('#tariffForm');
  if (!form) return;

  const nameInput = form.querySelector('#tariffName');
  const priceInput = form.querySelector('#tariffPrice');
  const hoursInput = form.querySelector('#tariffHours');

  function updatePreview() {
    const name = nameInput.value || 'Название тарифа';
    const price = parseFloat(priceInput.value) || 0;
    const hours = parseInt(hoursInput.value) || 1;
    const pricePerHour = hours > 0 ? price / hours : 0;

    const previewName = qs('#previewName');
    const previewHours = qs('#previewHours');
    const previewPrice = qs('#previewPrice');
    const previewPricePerHour = qs('#previewPricePerHour');

    if (previewName) previewName.textContent = name;
    if (previewHours) previewHours.textContent = `${hours} час${getHoursSuffix(hours)}`;
    if (previewPrice) previewPrice.textContent = `${price.toFixed(2)} BYN`;
    if (previewPricePerHour) previewPricePerHour.textContent = `${pricePerHour.toFixed(2)} BYN/час`;
  }

  [nameInput, priceInput, hoursInput].forEach(input => {
    input.addEventListener('input', updatePreview);
  });

  updatePreview();
}

// ========== ОТЗЫВЫ ==========
let allReviews = [];
let currentReviewFilter = 'all';

async function fetchAllReviews() {
  try {
    const response = await fetch('/api/v1/reviews/getAllReviews', {
      method: 'GET',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to fetch reviews');
    }

    allReviews = await response.json();
    console.log('Reviews loaded:', allReviews);
    renderReviews(allReviews);
  } catch (error) {
    console.error('Error fetching reviews:', error);
    showError('Не удалось загрузить отзывы');
  }
}

function renderReviews(reviews) {
  const container = qs('#reviewsContainer');
  if (!container) return;

  let filtered = reviews;

  // Применяем фильтры
  if (currentReviewFilter === 'visible') {
    filtered = reviews.filter(r => r.visible !== false);
  } else if (currentReviewFilter === 'hidden') {
    filtered = reviews.filter(r => r.visible === false);
  } else if (['5', '4', '3', '2', '1'].includes(currentReviewFilter)) {
    const rating = parseInt(currentReviewFilter);
    filtered = reviews.filter(r => r.rating === rating || r.stars === rating);
  }

  if (filtered.length === 0) {
    container.innerHTML = `
      <div class="reviews-empty-state">
        <i class="fas fa-star"></i>
        <h3>Отзывы не найдены</h3>
        <p>${currentReviewFilter !== 'all' ? 'Попробуйте изменить фильтр' : 'Пользователи еще не оставили отзывов'}</p>
      </div>
    `;
    return;
  }

  // Сортируем отзывы: сначала скрытые, потом по дате (новые сверху)
  const sorted = [...filtered].sort((a, b) => {
    // Сначала скрытые отзывы
    if (a.visible === false && b.visible !== false) return -1;
    if (a.visible !== false && b.visible === false) return 1;

    // Затем по дате (новые сверху)
    const dateA = new Date(a.createdAt || a.date || 0);
    const dateB = new Date(b.createdAt || b.date || 0);
    return dateB - dateA;
  });

  container.innerHTML = sorted.map(review => {
    const isHidden = review.visible === false;
    const rating = review.rating || review.stars || 0;
    const createdAt = review.createdAt || review.date || '';
    const formattedDate = formatDateForReview(createdAt);
    const user = review.user || review.userDTO || {};
    const adminResponse = review.response || review.adminAnswer || '';

    return `
      <div class="review-card ${isHidden ? 'hidden' : ''}" data-review-id="${review.id || review.reviewId}">
        <div class="review-header">
          <div class="review-user">
            <img src="${user.photoPath || 'https://i.pravatar.cc/40?img=' + (user.id || '1')}" 
                 alt="${user.login || 'Пользователь'}" 
                 class="review-avatar">
            <div class="review-user-info">
              <div class="review-user-name">${user.login || user.username || `Пользователь #${user.id || '?'}`}</div>
              <div class="review-date">${formattedDate}</div>
            </div>
          </div>
          <div class="review-rating">
            ${generateStarRating(rating)}
            <span class="rating-number">${rating}/5</span>
          </div>
        </div>
        
        <div class="review-body">
          <p class="review-text">${review.text || review.reviewText || 'Без текста'}</p>
          
          ${adminResponse ? `
            <div class="admin-response">
              <div class="response-header">
                <i class="fas fa-reply"></i>
                <span>Ответ администратора</span>
              </div>
              <p class="response-text">${adminResponse}</p>
            </div>
          ` : ''}
        </div>
        
        <div class="review-actions">
          ${isHidden ? `
            <button class="btn btn-sm btn-success" onclick="handleToggleReviewVisibility(${review.id || review.reviewId}, true)">
              <i class="fas fa-eye"></i> Показать
            </button>
          ` : `
            <button class="btn btn-sm btn-warning" onclick="handleToggleReviewVisibility(${review.id || review.reviewId}, false)">
              <i class="fas fa-eye-slash"></i> Скрыть
            </button>
          `}
          
          <button class="btn btn-sm btn-danger" onclick="handleDeleteReview(${review.id || review.reviewId})">
            <i class="fas fa-trash"></i> Удалить
          </button>
          
        </div>
        
        ${isHidden ? '<div class="review-hidden-badge"><i class="fas fa-eye-slash"></i> Скрыт</div>' : ''}
      </div>
    `;
  }).join('');
}

function generateStarRating(rating) {
  let stars = '';
  for (let i = 1; i <= 5; i++) {
    if (i <= rating) {
      stars += '<i class="fas fa-star active"></i>';
    } else {
      stars += '<i class="fas fa-star"></i>';
    }
  }
  return stars;
}

function formatDateForReview(dateString) {
  if (!dateString) return 'Без даты';

  try {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now - date;
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffDays = Math.floor(diffHours / 24);

    if (diffHours < 1) {
      return 'только что';
    } else if (diffHours < 24) {
      return `${diffHours} ${getHourWord(diffHours)} назад`;
    } else if (diffDays < 7) {
      return `${diffDays} ${getDayWord(diffDays)} назад`;
    } else {
      return date.toLocaleDateString('ru-RU', {
        day: 'numeric',
        month: 'long',
        year: 'numeric'
      });
    }
  } catch (e) {
    return dateString;
  }
}

function getHourWord(hours) {
  if (hours % 10 === 1 && hours % 100 !== 11) return 'час';
  if (hours % 10 >= 2 && hours % 10 <= 4 && (hours % 100 < 10 || hours % 100 >= 20)) return 'часа';
  return 'часов';
}

function getDayWord(days) {
  if (days % 10 === 1 && days % 100 !== 11) return 'день';
  if (days % 10 >= 2 && days % 10 <= 4 && (days % 100 < 10 || days % 100 >= 20)) return 'дня';
  return 'дней';
}

async function handleToggleReviewVisibility(reviewId, makeVisible) {
  try {
    const response = await fetch(`/api/v1/reviews/editVisibility/${reviewId}`, {
      method: 'PUT',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to toggle review visibility');
    }

    showSuccess(`Отзыв ${makeVisible ? 'показан' : 'скрыт'}`);
    await fetchAllReviews();
  } catch (error) {
    console.error('Error toggling review visibility:', error);
    showError('Не удалось изменить видимость отзыва');
  }
}

async function handleDeleteReview(reviewId) {
  if (!confirm('Вы уверены, что хотите удалить этот отзыв? Это действие необратимо.')) return;

  try {
    const response = await fetch(`/api/v1/reviews/deleteReview/${reviewId}`, {
      method: 'DELETE',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to delete review');
    }

    showSuccess('Отзыв удалён');
    await fetchAllReviews();
  } catch (error) {
    console.error('Error deleting review:', error);
    showError('Не удалось удалить отзыв');
  }
}

function updateStarsDisplay(container, rating) {
  container.querySelectorAll('.fa-star').forEach((star, index) => {
    if (index < rating) {
      star.classList.add('active');
    } else {
      star.classList.remove('active');
    }
  });
}

function closeEditReviewModal() {
  const modal = qs('#editReviewModal');
  if (modal) {
    modal.classList.remove('active');
  }
}

function initReviewFilters() {
  const filterBtns = qsa('#reviews .filter-btn');

  filterBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      filterBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      currentReviewFilter = btn.getAttribute('data-filter');
      renderReviews(allReviews);
    });
  });
}




// ========== УВЕДОМЛЕНИЯ ==========
function showToast(message, type = 'success', title = null) {
  const container = qs('#toastContainer');

  const icons = {
    success: 'fas fa-check-circle',
    error: 'fas fa-times-circle',
    warning: 'fas fa-exclamation-triangle',
    info: 'fas fa-info-circle'
  };

  const titles = {
    success: title || 'Успешно',
    error: title || 'Ошибка',
    warning: title || 'Предупреждение',
    info: title || 'Информация'
  };

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `
    <i class="${icons[type]} toast-icon"></i>
    <div class="toast-content">
      <div class="toast-title">${titles[type]}</div>
      <div class="toast-message">${message}</div>
    </div>
    <button class="toast-close">
      <i class="fas fa-times"></i>
    </button>
  `;

  container.appendChild(toast);

  toast.querySelector('.toast-close').addEventListener('click', () => {
    removeToast(toast);
  });

  setTimeout(() => {
    removeToast(toast);
  }, 5000);
}

function removeToast(toast) {
  toast.classList.add('hiding');
  setTimeout(() => {
    toast.remove();
  }, 300);
}

function showSuccess(message, title = null) {
  showToast(message, 'success', title);
}

function showError(message, title = null) {
  showToast(message, 'error', title);
}

function showWarning(message, title = null) {
  showToast(message, 'warning', title);
}

function showInfo(message, title = null) {
  showToast(message, 'info', title);
}

// ========== НАВИГАЦИЯ И МОДАЛКИ ==========
function initNavigation() {
  const navItems = qsa('.nav-item');
  const sections = qsa('.section');

  navItems.forEach(item => {
    item.addEventListener('click', async () => {
      const sectionId = item.getAttribute('data-section');

      navItems.forEach(nav => nav.classList.remove('active'));
      item.classList.add('active');

      sections.forEach(section => section.classList.remove('active'));
      const targetSection = qs(`#${sectionId}`);
      if (targetSection) targetSection.classList.add('active');

      if (sectionId === 'products') {
        await fetchAllProducts();
      } else if (sectionId === 'tariffs') {
        await fetchAllTariffs();
      } else if (sectionId === 'computers') {
        await fetchAllComputers();
      } else if (sectionId === 'sessions') {
        await fetchAllSessions();
      } else if (sectionId === 'orders') {
        await fetchAllOrders();
      } else if (sectionId === 'payments') {
        await fetchAllPayments();
      } else if (sectionId === 'reviews') {
        await fetchAllReviews();
      }
    });
  });
}

function initModals() {
  qs('#addProductBtn')?.addEventListener('click', openAddProductModal);
  qs('#addTariffBtn')?.addEventListener('click', openAddTariffModal);

  qsa('.modal-close').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const modal = e.target.closest('.modal');
      modal?.classList.remove('active');
      if (modal?.id === 'editComputerModal') {
        currentComputer = null;
      }
    });
  });

  qsa('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', (e) => {
      const modal = e.target.closest('.modal');
      modal?.classList.remove('active');
      if (modal?.id === 'editComputerModal') {
        currentComputer = null;
      }
    });
  });

  qsa('.modal-footer .btn--secondary').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const modal = e.target.closest('.modal');
      modal?.classList.remove('active');
      if (modal?.id === 'editComputerModal') {
        currentComputer = null;
      }
    });
  });

  const bonusModal = qs('#bonusModal');
  const bonusSubmitBtn = bonusModal?.querySelector('.modal-footer .btn--primary');
  if (bonusSubmitBtn) {
    bonusSubmitBtn.addEventListener('click', submitBonus);
  }

  const productForm = qs('#productForm');
  if (productForm) {
    productForm.addEventListener('submit', handleAddProduct);
  }

  const editProductForm = qs('#editProductForm');
  if (editProductForm) {
    editProductForm.addEventListener('submit', handleUpdateProduct);
  }

  const tariffForm = qs('#tariffForm');
  if (tariffForm) {
    tariffForm.addEventListener('submit', (e) => {
      e.preventDefault();
      if (currentTariff) {
        handleUpdateTariff(e);
      } else {
        handleAddTariff(e);
      }
    });
  }

  const editComputerForm = qs('#editComputerForm');
  if (editComputerForm) {
    editComputerForm.addEventListener('submit', handleUpdateComputer);
  }
}

async function initLogout() {
  qs('#logoutBtn')?.addEventListener('click', async () => {
    try {
      const response = await fetch('/api/v1/auth/logout', {
        method: 'POST',
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error('Logout failed');
      }

      window.location.href = '/static/html/index.html';
    } catch (err) {
      console.error('Logout error:', err);
      window.location.href = '/static/html/index.html';
    }
  });
}

async function fetchAllOrders() {
  try {
    const response = await fetch('/api/v1/admin/orders/getAllOrders', {
      method: 'GET',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to fetch orders');
    }

    allOrders = await response.json();
    console.log('Orders loaded:', allOrders);
    renderOrders(allOrders);
  } catch (error) {
    console.error('Error fetching orders:', error);
    showError('Не удалось загрузить заказы мини-бара');
  }
}

function renderOrders(orders) {
  const tbody = qs('#ordersTableBody');
  if (!tbody) return;

  if (!orders || orders.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7" style="text-align: center; padding: 2rem; color: var(--gray);">
          Заказы не найдены
        </td>
      </tr>
    `;
    return;
  }

  const sortedOrders = [...orders].sort((a, b) => {
    const dateA = new Date(a.order?.createdAt || 0);
    const dateB = new Date(b.order?.createdAt || 0);
    return dateB - dateA;
  });

  tbody.innerHTML = sortedOrders.map(orderWrapper => {
    const order = orderWrapper.order;
    const payment = orderWrapper.payment;
    const user = orderWrapper.user;

    if (!order) return '';

    const userName = user?.login || user?.username || `ID ${order.userId || '-'}`;
    const items = (order.orderItems || [])
        .map(item => {
          const productName = item.productDTO?.name || 'Товар';
          const quantity = item.quantity || 1;
          return `${productName} x${quantity}`;
        })
        .join(', ');

    const amount = order.totalCost ? parseFloat(order.totalCost).toFixed(2) : '0.00';
    const status = order.status || 'CREATED';
    const createdAt = order.createdAt ? formatDateTime(order.createdAt) : '-';
    const paymentStatus = payment?.status || 'Неизвестно';
    const paymentStatusBadge = getPaymentStatusBadge(paymentStatus);
    const orderStatusBadge = getOrderStatusBadge(status);

    return `
      <tr data-order-id="${order.id}">
        <td>#${order.id}</td>
        <td>
          <div class="user-info-cell">
            <img src="${user?.photoPath || 'https://i.pravatar.cc/30?img=' + (user?.id || '1')}" 
                 alt="${userName}" 
                 class="user-avatar">
            <span>${userName}</span>
          </div>
        </td>
        <td title="${items}">${items.length > 50 ? items.substring(0, 50) + '...' : items}</td>
        <td><strong>${amount} BYN</strong></td>
        <td>${orderStatusBadge}</td>
        <td>${paymentStatusBadge}</td>
        <td>${createdAt}</td>
        <td>
          <div class="action-buttons">
            <button class="btn-icon btn-primary" title="Подробнее" onclick="openOrderDetailsModal('${order.id}')">
              <i class="fas fa-eye"></i>
            </button>
            <button class="btn-icon btn-success" title="Отметить как доставленный" 
                    onclick="markOrderAsDelivered('${order.id}')"
                    ${status === 'DELIVERED' || status === 'CANCELLED' ? 'disabled' : ''}>
              <i class="fas fa-check"></i>
            </button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

function getPaymentStatusBadge(status) {
  const statusConfigs = {
    'CREATED': { class: 'status-secondary', text: 'Создан', icon: 'fas fa-plus-circle' },
    'PAID': { class: 'status-success', text: 'Оплачен', icon: 'fas fa-check-circle' },
    'FAILED': { class: 'status-danger', text: 'Ошибка', icon: 'fas fa-times-circle' },
    'CANCELLED': { class: 'status-warning', text: 'Отменен', icon: 'fas fa-ban' },
    'REFUNDED': { class: 'status-info', text: 'Возврат', icon: 'fas fa-undo' },
    'SUCCESSFULLY': { class: 'status-active', text: 'Успешно', icon: 'fas fa-check-double' }
  };

  const config = statusConfigs[status] || { class: 'status-secondary', text: status, icon: 'fas fa-question-circle' };

  return `
    <span class="status-badge ${config.class}">
      <i class="${config.icon}"></i>
      ${config.text}
    </span>
  `;
}

function getOrderStatusBadge(status) {
  const statusConfigs = {
    'CREATED': { class: 'status-secondary', text: 'Создан', icon: 'fas fa-plus-circle' },
    'PROCESSING': { class: 'status-info', text: 'В обработке', icon: 'fas fa-spinner fa-spin' },
    'PAID': { class: 'status-success', text: 'Оплачен', icon: 'fas fa-check-circle' },
    'ERROR_IN_PAID': { class: 'status-danger', text: 'Ошибка оплаты', icon: 'fas fa-exclamation-triangle' },
    'SHIPPED': { class: 'status-warning', text: 'Отправлен', icon: 'fas fa-shipping-fast' },
    'DELIVERED': { class: 'status-active', text: 'Доставлен', icon: 'fas fa-check-double' },
    'CANCELLED': { class: 'status-banned', text: 'Отменен', icon: 'fas fa-ban' }
  };

  const config = statusConfigs[status] || { class: 'status-secondary', text: status, icon: 'fas fa-question-circle' };

  return `
    <span class="status-badge ${config.class}">
      <i class="${config.icon}"></i>
      ${config.text}
    </span>
  `;
}

async function markOrderAsDelivered(orderId) {
  try {
    const response = await fetch(`/api/v1/orders/markAsDelivered/${orderId}`, {
      method: 'PUT',
      credentials: 'include'
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Failed to mark order as delivered');
    }

    showSuccess('Заказ отмечен как доставленный');
    await fetchAllOrders();
  } catch (error) {
    console.error('Error marking order as delivered:', error);
    showError('Не удалось обновить статус заказа: ' + error.message);
  }
}

function openOrderDetailsModal(orderId) {
  const modal = qs('#orderDetailsModal');
  const body = qs('#orderDetailsBody');
  if (!modal || !body) return;

  const wrapper = allOrders.find(o => o.order?.id == orderId);
  if (!wrapper) {
    showError('Заказ не найден');
    return;
  }

  const order = wrapper.order;
  const payment = wrapper.payment;
  const user = wrapper.user;

  if (!order) {
    showError('Данные заказа не найдены');
    return;
  }

  const itemsHtml = (order.orderItems || []).map(item => {
    const product = item.productDTO;
    const price = product.price ? parseFloat(product.price).toFixed(2) : '0.00';
    const total = item.quantity * parseFloat(product.price || 0);

    return `
      <div class="order-item-detail">
        <div class="order-item-header">
          <img src="${product?.photoPath || 'https://via.placeholder.com/50'}" 
               alt="${product?.name || 'Товар'}" 
               class="order-item-image">
          <div class="order-item-info">
            <div class="order-item-name">${product?.name || 'Товар'}</div>
            <div class="order-item-category">${product?.category?.name || 'Категория не указана'}</div>
          </div>
        </div>
        <div class="order-item-details">
          <div class="detail-row">
            <span>Цена:</span>
            <strong>${price} BYN</strong>
          </div>
          <div class="detail-row">
            <span>Количество:</span>
            <strong>${item.quantity || 1} шт.</strong>
          </div>
          <div class="detail-row">
            <span>Итого:</span>
            <strong>${total.toFixed(2)} BYN</strong>
          </div>
        </div>
      </div>
    `;
  }).join('') || '<div class="no-items">Товары не найдены</div>';

  const userInfo = user ? `
    <div class="detail-section">
      <h4><i class="fas fa-user"></i> Пользователь</h4>
      <div class="details-grid">
        <div class="detail-row">
          <span>ID:</span>
          <span>${user.id || '-'}</span>
        </div>
        <div class="detail-row">
          <span>Логин:</span>
          <span>${user.login || user.username || '-'}</span>
        </div>
        <div class="detail-row">
          <span>Email:</span>
          <span>${user.email || '-'}</span>
        </div>
        <div class="detail-row">
          <span>Телефон:</span>
          <span>${user.phone || '-'}</span>
        </div>
        <div class="detail-row">
          <span>Баланс:</span>
          <span>${(user.wallet || 0).toFixed(2)} BYN</span>
        </div>
        <div class="detail-row">
          <span>Бонусы:</span>
          <span>${user.bonusCoins || 0}</span>
        </div>
      </div>
    </div>
  ` : '<div class="detail-section"><p>Информация о пользователе недоступна</p></div>';

  const paymentInfo = payment ? `
    <div class="detail-section">
      <h4><i class="fas fa-credit-card"></i> Платеж</h4>
      <div class="details-grid">
        <div class="detail-row">
          <span>Сумма:</span>
          <strong>${(payment.amount || 0).toFixed(2)} BYN</strong>
        </div>
        <div class="detail-row">
          <span>Тип платежа:</span>
          <span>${payment.paymentType || '-'}</span>
        </div>
        <div class="detail-row">
          <span>Статус платежа:</span>
          ${getPaymentStatusBadge(payment.status)}
        </div>
        <div class="detail-row">
          <span>Дата платежа:</span>
          <span>${payment.createdAt ? formatDateTime(payment.createdAt) : '-'}</span>
        </div>
      </div>
    </div>
  ` : '<div class="detail-section"><p>Информация о платеже недоступна</p></div>';

  body.innerHTML = `
    <div class="order-details-container">
      <div class="detail-section">
        <h4><i class="fas fa-receipt"></i> Информация о заказе</h4>
        <div class="details-grid">
          <div class="detail-row">
            <span>ID заказа:</span>
            <strong>#${order.id}</strong>
          </div>
          <div class="detail-row">
            <span>Дата создания:</span>
            <span>${order.createdAt ? formatDateTime(order.createdAt) : '-'}</span>
          </div>
          <div class="detail-row">
            <span>Статус заказа:</span>
            ${getOrderStatusBadge(order.status)}
          </div>
          <div class="detail-row">
            <span>Общая стоимость:</span>
            <strong class="order-total">${order.totalCost ? parseFloat(order.totalCost).toFixed(2) : '0.00'} BYN</strong>
          </div>
        </div>
      </div>

      ${userInfo}

      ${paymentInfo}

      <div class="detail-section">
        <h4><i class="fas fa-shopping-cart"></i> Товары в заказе</h4>
        <div class="order-items-list">
          ${itemsHtml}
        </div>
      </div>

      <div class="detail-actions">
        <button class="btn btn--primary" onclick="markOrderAsDelivered('${order.id}')"
                ${order.status === 'DELIVERED' || order.status === 'CANCELLED' ? 'disabled' : ''}>
          <i class="fas fa-check"></i> Отметить как доставленный
        </button>
        <button class="btn btn--secondary" onclick="closeOrderDetailsModal()">
          <i class="fas fa-times"></i> Закрыть
        </button>
      </div>
    </div>
  `;

  modal.classList.add('active');
}

function closeOrderDetailsModal() {
  const modal = qs('#orderDetailsModal');
  if (modal) modal.classList.remove('active');
}

function addOrderStyles() {
  const style = document.createElement('style');
  style.textContent = `
    .user-info-cell {
      display: flex;
      align-items: center;
      gap: 0.75rem;
    }
    
    .user-avatar {
      width: 30px;
      height: 30px;
      border-radius: 50%;
      object-fit: cover;
      border: 2px solid var(--primary);
    }
    
    .order-details-container {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }
    
    .detail-section {
      background: var(--card-bg);
      border-radius: 8px;
      padding: 1.5rem;
      border: 1px solid var(--border);
    }
    
    .detail-section h4 {
      margin: 0 0 1rem 0;
      color: var(--text);
      font-size: 1.1rem;
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }
    
    .detail-section h4 i {
      color: var(--primary);
    }
    
    .details-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1rem;
    }
    
    .detail-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0.5rem 0;
      border-bottom: 1px solid var(--border-light);
    }
    
    .detail-row:last-child {
      border-bottom: none;
    }
    
    .detail-row span:first-child {
      color: var(--gray);
      font-size: 0.9rem;
    }
    
    .order-total {
      font-size: 1.2rem;
      color: var(--success);
    }
    
    .order-items-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }
    
    .order-item-detail {
      background: var(--bg-secondary);
      border-radius: 6px;
      padding: 1rem;
      border: 1px solid var(--border);
    }
    
    .order-item-header {
      display: flex;
      align-items: center;
      gap: 1rem;
      margin-bottom: 1rem;
    }
    
    .order-item-image {
      width: 50px;
      height: 50px;
      border-radius: 6px;
      object-fit: cover;
    }
    
    .order-item-info {
      flex: 1;
    }
    
    .order-item-name {
      font-weight: 600;
      color: var(--text);
    }
    
    .order-item-category {
      font-size: 0.85rem;
      color: var(--gray);
    }
    
    .order-item-details {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 1rem;
      background: var(--card-bg);
      padding: 0.75rem;
      border-radius: 4px;
    }
    
    .no-items {
      text-align: center;
      padding: 2rem;
      color: var(--gray);
      font-style: italic;
    }
    
    .detail-actions {
      display: flex;
      gap: 1rem;
      justify-content: flex-end;
      padding-top: 1.5rem;
      border-top: 1px solid var(--border);
    }
    
    /* Стили для модалки заказа */
    .modal-content-large {
      max-width: 800px;
    }
    
    .status-badge {
      display: inline-flex;
      align-items: center;
      gap: 0.25rem;
      padding: 0.25rem 0.75rem;
      border-radius: 20px;
      font-size: 0.85rem;
      font-weight: 500;
    }
    
    .status-success {
      background: var(--success-light);
      color: var(--success-dark);
      border: 1px solid var(--success);
    }
    
    .status-danger {
      background: var(--danger-light);
      color: var(--danger-dark);
      border: 1px solid var(--danger);
    }
    
    .status-warning {
      background: var(--warning-light);
      color: var(--warning-dark);
      border: 1px solid var(--warning);
    }
    
    .status-info {
      background: var(--info-light);
      color: var(--info-dark);
      border: 1px solid var(--info);
    }
    
    .status-secondary {
      background: var(--gray-light);
      color: var(--gray-dark);
      border: 1px solid var(--gray);
    }
    
    .status-active {
      background: var(--primary-light);
      color: var(--primary-dark);
      border: 1px solid var(--primary);
    }
    
    .status-banned {
      background: var(--danger-light);
      color: var(--danger-dark);
      border: 1px solid var(--danger);
    }
  `;
  document.head.appendChild(style);
}

async function initOrdersSection() {
  await fetchAllOrders();

  const filterBtns = qsa('#orders .filter-btn');
  filterBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      filterBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      const filter = btn.getAttribute('data-filter');

      let filtered = allOrders;
      if (filter !== 'all') {
        filtered = allOrders.filter(o => o.order?.status === filter);
      }

      renderOrders(filtered);
    });
  });
}

async function init() {
  initNavigation();
  initModals();
  initSearch();
  initLogout();
  initCategoryFilter();
  initTariffPreview();
  initComputerSearch();
  initReviewFilters();
  addOrderStyles();

  await fetchAllUsers();
  await fetchAllComputers();
  await initOrdersSection();
}

document.addEventListener('DOMContentLoaded', init);