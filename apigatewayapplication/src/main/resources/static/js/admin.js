const qs = (s, r = document) => r.querySelector(s);
const qsa = (s, r = document) => Array.from(r.querySelectorAll(s));

let allUsers = [];
let allProducts = [];
let allTariffs = [];
let allComputers = [];
let allSessions = [];
let allOrders = [];
let allPayments = [];
let allReviews = [];
let currentUser = null;
let currentProduct = null;
let currentTariff = null;
let currentComputer = null;
let currentCategoryFilter = 'all';
let currentOrderFilter = 'all';
let currentPaymentFilter = 'all';
let currentReviewFilter = 'all';

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
    isEnabled: status
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
      } else       if (sectionId === 'sessions') {
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

  // Закрытие модальных окон деталей
  qs('#sessionDetailsModal .modal-close')?.addEventListener('click', closeSessionDetailsModal);
  qs('#orderDetailsModal .modal-close')?.addEventListener('click', closeOrderDetailsModal);
  qs('#paymentDetailsModal .modal-close')?.addEventListener('click', closePaymentDetailsModal);
  
  qs('#sessionDetailsModal .modal-overlay')?.addEventListener('click', closeSessionDetailsModal);
  qs('#orderDetailsModal .modal-overlay')?.addEventListener('click', closeOrderDetailsModal);
  qs('#paymentDetailsModal .modal-overlay')?.addEventListener('click', closePaymentDetailsModal);

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

// ========== СЕССИИ ==========
async function fetchAllSessions() {
  const tbody = qs('#sessionsTableBody');
  if (tbody) {
    tbody.innerHTML = `
      <tr>
        <td colspan="9" style="text-align: center; padding: 2rem;">
          <i class="fas fa-spinner fa-spin" style="font-size: 2rem; color: var(--primary);"></i>
          <p style="margin-top: 1rem; color: var(--gray);">Загрузка сессий...</p>
        </td>
      </tr>
    `;
  }

  try {
    const response = await fetch('/api/v1/admin/sessions/getAllSessions', {
      method: 'GET',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to fetch sessions');
    }

    const data = await response.json();
    allSessions = Array.isArray(data) ? data : [];
    console.log('Sessions loaded:', allSessions);
    renderSessions(allSessions);
  } catch (error) {
    console.error('Error fetching sessions:', error);
    // Показываем заглушку при ошибке
    if (tbody) {
      tbody.innerHTML = `
        <tr>
          <td colspan="9" style="text-align: center; padding: 2rem; color: var(--gray);">
            <i class="fas fa-exclamation-triangle" style="font-size: 2rem; margin-bottom: 1rem; color: var(--warning);"></i>
            <p style="margin-top: 1rem;">Не удалось загрузить список сессий</p>
            <p style="margin-top: 0.5rem; font-size: 0.9rem; opacity: 0.8;">Проверьте подключение к серверу</p>
          </td>
        </tr>
      `;
    }
    allSessions = [];
    showError('Не удалось загрузить список сессий');
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

  tbody.innerHTML = sessions.map(session => {
    const sessionId = session.sessionId || session.id || '-';
    const userId = session.userId || '-';
    const pcName = session.pcdto?.name || session.pc?.name || 'Не указано';
    const tariffName = session.tariff?.name || 'Не указан';
    const startTime = session.startTime ? formatDateTime(session.startTime) : '-';
    const endTime = session.endTime ? formatDateTime(session.endTime) : 'Не завершена';
    const totalCost = session.totalCost ? parseFloat(session.totalCost).toFixed(2) + ' BYN' : '-';
    const status = session.status || 'UNKNOWN';
    const statusText = getSessionStatusText(status);
    const statusClass = getSessionStatusClass(status);

    return `
      <tr data-session-id="${sessionId}">
        <td>#${sessionId}</td>
        <td>ID: ${userId}</td>
        <td>${pcName}</td>
        <td>${tariffName}</td>
        <td>${startTime}</td>
        <td>${endTime}</td>
        <td>${totalCost}</td>
        <td><span class="status-badge ${statusClass}">${statusText}</span></td>
        <td>
          <button class="btn-icon btn-primary" title="Просмотреть" onclick="viewSessionDetails(${sessionId})">
            <i class="fas fa-eye"></i>
          </button>
        </td>
      </tr>
    `;
  }).join('');
}

function getSessionStatusText(status) {
  const statusMap = {
    'PENDING': 'Ожидает',
    'PAID': 'Оплачена',
    'IN_PROGRESS': 'В процессе',
    'COMPLETED': 'Завершена',
    'NOT_SHOW': 'Не явился',
    'CANCELLED': 'Отменена',
    'REFUNDED': 'Возвращена',
    'ERROR': 'Ошибка'
  };
  return statusMap[status] || status;
}

function getSessionStatusClass(status) {
  const classMap = {
    'PENDING': 'status-pending',
    'PAID': 'status-active',
    'IN_PROGRESS': 'status-active',
    'COMPLETED': 'status-completed',
    'NOT_SHOW': 'status-inactive',
    'CANCELLED': 'status-inactive',
    'REFUNDED': 'status-inactive',
    'ERROR': 'status-banned'
  };
  return classMap[status] || 'status-inactive';
}

async function viewSessionDetails(sessionId) {
  const session = allSessions.find(s => (s.sessionId || s.id) === sessionId);
  if (!session) {
    showError('Сессия не найдена');
    return;
  }

  // Если данных нет, показываем заглушку
  if (allSessions.length === 0) {
    showError('Данные сессий не загружены');
    return;
  }

  const modal = qs('#sessionDetailsModal');
  const body = qs('#sessionDetailsBody');
  if (!modal || !body) return;

  modal.classList.add('active');

  // Формируем детальную информацию
  const pc = session.pcdto || session.pc || {};
  const room = pc.roomDTO || pc.room || {};
  const tariff = session.tariff || {};

  body.innerHTML = `
    <div class="detail-section">
      <h4><i class="fas fa-info-circle"></i> Основная информация</h4>
      <div class="detail-row">
        <div class="detail-label">ID сессии:</div>
        <div class="detail-value">#${session.sessionId || session.id || '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">ID пользователя:</div>
        <div class="detail-value">${session.userId || '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Статус:</div>
        <div class="detail-value">
          <span class="status-badge ${getSessionStatusClass(session.status)}">
            ${getSessionStatusText(session.status)}
          </span>
        </div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Стоимость:</div>
        <div class="detail-value">${session.totalCost ? parseFloat(session.totalCost).toFixed(2) + ' BYN' : '-'}</div>
      </div>
    </div>

    <div class="detail-section">
      <h4><i class="fas fa-clock"></i> Временные рамки</h4>
      <div class="detail-row">
        <div class="detail-label">Начало:</div>
        <div class="detail-value">${session.startTime ? formatDateTime(session.startTime) : '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Конец:</div>
        <div class="detail-value">${session.endTime ? formatDateTime(session.endTime) : 'Не завершена'}</div>
      </div>
      ${session.startTime && session.endTime ? `
      <div class="detail-row">
        <div class="detail-label">Длительность:</div>
        <div class="detail-value">${calculateDuration(session.startTime, session.endTime)}</div>
      </div>
      ` : ''}
    </div>

    <div class="detail-section">
      <h4><i class="fas fa-desktop"></i> Компьютер</h4>
      <div class="detail-row">
        <div class="detail-label">ID компьютера:</div>
        <div class="detail-value">${pc.id || '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Название:</div>
        <div class="detail-value">${pc.name || '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">CPU:</div>
        <div class="detail-value">${pc.cpu || '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">GPU:</div>
        <div class="detail-value">${pc.gpu || '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">RAM:</div>
        <div class="detail-value">${pc.ram || '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Монитор:</div>
        <div class="detail-value">${pc.monitor || '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Статус:</div>
        <div class="detail-value">
          <span class="status-badge ${pc.isEnabled ? 'status-active' : 'status-inactive'}">
            ${pc.isEnabled ? 'Активен' : 'Неактивен'}
          </span>
        </div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Комната:</div>
        <div class="detail-value">
          ${room.name || '-'}
          ${room.isVip ? '<span class="status-badge status-active" style="margin-left: 0.5rem;">VIP</span>' : ''}
        </div>
      </div>
    </div>

    <div class="detail-section">
      <h4><i class="fas fa-ticket-alt"></i> Тариф</h4>
      <div class="detail-row">
        <div class="detail-label">ID тарифа:</div>
        <div class="detail-value">${tariff.tariffId || tariff.id || '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Название:</div>
        <div class="detail-value">${tariff.name || '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Цена:</div>
        <div class="detail-value">${tariff.price ? parseFloat(tariff.price).toFixed(2) + ' BYN' : '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Часы:</div>
        <div class="detail-value">${tariff.hours || '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">VIP:</div>
        <div class="detail-value">
          <span class="status-badge ${tariff.isVip || tariff.vip ? 'status-active' : 'status-inactive'}">
            ${tariff.isVip || tariff.vip ? 'Да' : 'Нет'}
          </span>
        </div>
      </div>
    </div>
  `;
}

function closeSessionDetailsModal() {
  const modal = qs('#sessionDetailsModal');
  if (modal) modal.classList.remove('active');
}

// ========== ЗАКАЗЫ ==========
async function fetchAllOrders() {
  const tbody = qs('#ordersTableBody');
  if (tbody) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7" style="text-align: center; padding: 2rem;">
          <i class="fas fa-spinner fa-spin" style="font-size: 2rem; color: var(--primary);"></i>
          <p style="margin-top: 1rem; color: var(--gray);">Загрузка заказов...</p>
        </td>
      </tr>
    `;
  }

  try {
    const response = await fetch('/api/v1/admin/orders/getAllOrders', {
      method: 'GET',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to fetch orders');
    }

    const data = await response.json();
    allOrders = Array.isArray(data) ? data : [];
    console.log('Orders loaded:', allOrders);
    renderOrders(allOrders);
  } catch (error) {
    console.error('Error fetching orders:', error);
    // Показываем заглушку при ошибке
    if (tbody) {
      tbody.innerHTML = `
        <tr>
          <td colspan="7" style="text-align: center; padding: 2rem; color: var(--gray);">
            <i class="fas fa-exclamation-triangle" style="font-size: 2rem; margin-bottom: 1rem; color: var(--warning);"></i>
            <p style="margin-top: 1rem;">Не удалось загрузить список заказов</p>
            <p style="margin-top: 0.5rem; font-size: 0.9rem; opacity: 0.8;">Проверьте подключение к серверу</p>
          </td>
        </tr>
      `;
    }
    allOrders = [];
    showError('Не удалось загрузить список заказов');
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

  let filtered = orders;
  if (currentOrderFilter !== 'all') {
    filtered = orders.filter(order => {
      const status = order.order?.status || order.status;
      return status === currentOrderFilter;
    });
  }

  if (filtered.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7" style="text-align: center; padding: 2rem; color: var(--gray);">
          Заказы с выбранным фильтром не найдены
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = filtered.map(order => {
    const orderData = order.order || order;
    const orderId = orderData.id || '-';
    const user = order.user || {};
    const userName = user.login || user.username || `ID: ${orderData.userId || '-'}`;
    const orderItems = orderData.orderItems || [];
    const itemsPreview = orderItems.length > 0 
      ? orderItems.slice(0, 2).map(item => {
          const product = item.productDTO || item.product || {};
          const productName = product.name || 'Неизвестный товар';
          const quantity = item.quantity || 0;
          return `${productName} x${quantity}`;
        }).join(', ') + (orderItems.length > 2 ? ` и ещё ${orderItems.length - 2}` : '')
      : 'Нет товаров';
    const totalCost = orderData.totalCost ? parseFloat(orderData.totalCost).toFixed(2) + ' BYN' : '-';
    const status = orderData.status || 'UNKNOWN';
    const statusText = getOrderStatusText(status);
    const statusClass = getOrderStatusClass(status);
    const createdAt = orderData.createdAt ? formatDateTime(orderData.createdAt) : '-';

    return `
      <tr data-order-id="${orderId}">
        <td>#${orderId}</td>
        <td>${userName}</td>
        <td>${itemsPreview}</td>
        <td>${totalCost}</td>
        <td><span class="status-badge ${statusClass}">${statusText}</span></td>
        <td>${createdAt}</td>
        <td>
          <button class="btn-icon btn-primary" title="Просмотреть" onclick="viewOrderDetails(${orderId})">
            <i class="fas fa-eye"></i>
          </button>
        </td>
      </tr>
    `;
  }).join('');
}

function getOrderStatusText(status) {
  const statusMap = {
    'CREATED': 'Создан',
    'PROCESSING': 'В обработке',
    'PAID': 'Оплачен',
    'ERROR_IN_PAID': 'Ошибка оплаты',
    'SHIPPED': 'Отправлен',
    'DELIVERED': 'Доставлен',
    'CANCELLED': 'Отменён'
  };
  return statusMap[status] || status;
}

function getOrderStatusClass(status) {
  const classMap = {
    'CREATED': 'status-pending',
    'PROCESSING': 'status-pending',
    'PAID': 'status-active',
    'ERROR_IN_PAID': 'status-banned',
    'SHIPPED': 'status-active',
    'DELIVERED': 'status-completed',
    'CANCELLED': 'status-inactive'
  };
  return classMap[status] || 'status-inactive';
}

async function viewOrderDetails(orderId) {
  const order = allOrders.find(o => {
    const oData = o.order || o;
    return (oData.id || oData.orderId) === orderId;
  });
  if (!order) {
    showError('Заказ не найден');
    return;
  }

  // Если данных нет, показываем заглушку
  if (allOrders.length === 0) {
    showError('Данные заказов не загружены');
    return;
  }

  const modal = qs('#orderDetailsModal');
  const body = qs('#orderDetailsBody');
  if (!modal || !body) return;

  modal.classList.add('active');

  const orderData = order.order || order;
  const user = order.user || {};
  const payment = order.payment || {};
  const orderItems = orderData.orderItems || [];

  body.innerHTML = `
    <div class="detail-section">
      <h4><i class="fas fa-info-circle"></i> Основная информация</h4>
      <div class="detail-row">
        <div class="detail-label">ID заказа:</div>
        <div class="detail-value">#${orderData.id || orderData.orderId || '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">ID пользователя:</div>
        <div class="detail-value">${orderData.userId || '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Статус:</div>
        <div class="detail-value">
          <span class="status-badge ${getOrderStatusClass(orderData.status)}">
            ${getOrderStatusText(orderData.status)}
          </span>
        </div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Общая стоимость:</div>
        <div class="detail-value">${orderData.totalCost ? parseFloat(orderData.totalCost).toFixed(2) + ' BYN' : '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Дата создания:</div>
        <div class="detail-value">${orderData.createdAt ? formatDateTime(orderData.createdAt) : '-'}</div>
      </div>
    </div>

    <div class="detail-section">
      <h4><i class="fas fa-user"></i> Пользователь</h4>
      <div class="detail-row">
        <div class="detail-label">ID:</div>
        <div class="detail-value">${user.id || '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Логин:</div>
        <div class="detail-value">${user.login || user.username || '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Email:</div>
        <div class="detail-value">${user.email || '-'}</div>
      </div>
    </div>

    <div class="detail-section">
      <h4><i class="fas fa-credit-card"></i> Платеж</h4>
      <div class="detail-row">
        <div class="detail-label">ID платежа:</div>
        <div class="detail-value">${orderData.paymentId || payment.id || '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Сумма:</div>
        <div class="detail-value">${payment.amount ? parseFloat(payment.amount).toFixed(2) + ' BYN' : '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Тип платежа:</div>
        <div class="detail-value">${payment.paymentType || '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Статус платежа:</div>
        <div class="detail-value">
          ${payment.status ? `
            <span class="status-badge ${getPaymentStatusClass(payment.status)}">
              ${getPaymentStatusText(payment.status)}
            </span>
          ` : '-'}
        </div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Дата платежа:</div>
        <div class="detail-value">${payment.createdAt ? formatDateTime(payment.createdAt) : '-'}</div>
      </div>
    </div>

    <div class="detail-section">
      <h4><i class="fas fa-shopping-cart"></i> Товары (${orderItems.length})</h4>
      ${orderItems.length > 0 ? `
        <ul class="detail-list">
          ${orderItems.map(item => {
            const product = item.productDTO || item.product || {};
            const productName = product.name || 'Неизвестный товар';
            const quantity = item.quantity || 0;
            const price = item.productPrice ? parseFloat(item.productPrice).toFixed(2) : '0.00';
            const total = (parseFloat(price) * quantity).toFixed(2);
            return `
              <li class="detail-list-item">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                  <div>
                    <strong>${productName}</strong>
                    <div style="margin-top: 0.5rem; color: var(--gray); font-size: 0.9rem;">
                      Количество: ${quantity} × ${price} BYN = ${total} BYN
                    </div>
                  </div>
                </div>
              </li>
            `;
          }).join('')}
        </ul>
      ` : '<p style="color: var(--gray);">Товары не найдены</p>'}
    </div>
  `;
}

function getPaymentStatusText(status) {
  const statusMap = {
    'CREATED': 'Создан',
    'PAID': 'Оплачен',
    'FAILED': 'Ошибка',
    'CANCELLED': 'Отменён',
    'REFUNDED': 'Возвращён'
  };
  return statusMap[status] || status;
}

function getPaymentStatusClass(status) {
  const classMap = {
    'CREATED': 'status-pending',
    'PAID': 'status-completed',
    'FAILED': 'status-banned',
    'CANCELLED': 'status-inactive',
    'REFUNDED': 'status-inactive'
  };
  return classMap[status] || 'status-inactive';
}

function closeOrderDetailsModal() {
  const modal = qs('#orderDetailsModal');
  if (modal) modal.classList.remove('active');
}

function initOrderFilters() {
  const filterButtons = qsa('#orders .filter-btn');
  filterButtons.forEach(btn => {
    btn.addEventListener('click', (e) => {
      filterButtons.forEach(b => b.classList.remove('active'));
      e.target.classList.add('active');
      currentOrderFilter = e.target.getAttribute('data-filter');
      renderOrders(allOrders);
    });
  });
}

// ========== УТИЛИТЫ ==========
function formatDateTime(dateTimeString) {
  if (!dateTimeString) return '-';
  try {
    const date = new Date(dateTimeString);
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${day}.${month}.${year} ${hours}:${minutes}`;
  } catch (e) {
    return dateTimeString;
  }
}

function calculateDuration(startTime, endTime) {
  if (!startTime || !endTime) return '-';
  try {
    const start = new Date(startTime);
    const end = new Date(endTime);
    const diffMs = end - start;
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));
    if (diffHours > 0) {
      return `${diffHours} ч ${diffMinutes} мин`;
    }
    return `${diffMinutes} мин`;
  } catch (e) {
    return '-';
  }
}

// ========== ОТЗЫВЫ ==========
async function fetchAllReviews() {
  const container = qs('#reviewsContainer');
  if (container) {
    container.innerHTML = `
      <div style="text-align: center; padding: 2rem;">
        <i class="fas fa-spinner fa-spin" style="font-size: 2rem; color: var(--primary);"></i>
        <p style="margin-top: 1rem; color: var(--gray);">Загрузка отзывов...</p>
      </div>
    `;
  }

  try {
    const response = await fetch('/api/v1/reviews/getAllReviews', {
      method: 'GET',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to fetch reviews');
    }

    const data = await response.json();
    allReviews = Array.isArray(data) ? data : [];
    console.log('Reviews loaded:', allReviews);
    renderReviews(allReviews);
  } catch (error) {
    console.error('Error fetching reviews:', error);
    if (container) {
      container.innerHTML = `
        <div style="text-align: center; padding: 2rem; color: var(--gray);">
          <i class="fas fa-exclamation-triangle" style="font-size: 2rem; margin-bottom: 1rem; color: var(--warning);"></i>
          <p style="margin-top: 1rem;">Не удалось загрузить список отзывов</p>
          <p style="margin-top: 0.5rem; font-size: 0.9rem; opacity: 0.8;">Проверьте подключение к серверу</p>
        </div>
      `;
    }
    allReviews = [];
    showError('Не удалось загрузить список отзывов');
  }
}

function renderReviews(reviews) {
  const container = qs('#reviewsContainer');
  if (!container) return;

  let filtered = reviews;
  if (currentReviewFilter !== 'all') {
    if (currentReviewFilter === 'visible') {
      filtered = reviews.filter(r => r.isVisible !== false);
    } else if (currentReviewFilter === 'hidden') {
      filtered = reviews.filter(r => r.isVisible === false);
    } else {
      const stars = parseInt(currentReviewFilter);
      if (!isNaN(stars)) {
        filtered = reviews.filter(r => r.stars === stars);
      }
    }
  }

  if (!filtered || filtered.length === 0) {
    container.innerHTML = `
      <div style="text-align: center; padding: 3rem; color: var(--gray);">
        <i class="fas fa-star" style="font-size: 3rem; margin-bottom: 1rem; opacity: 0.5;"></i>
        <p>Отзывы не найдены</p>
      </div>
    `;
    return;
  }

  container.innerHTML = filtered.map(review => {
    const user = review.user || {};
    const userName = user.login || user.username || 'Неизвестный пользователь';
    const userPhoto = user.photoPath || `https://i.pravatar.cc/50?img=${review.reviewId || 1}`;
    const stars = review.stars || 0;
    const isVisible = review.isVisible !== false;
    const createdAt = review.createdAt ? formatDateTime(review.createdAt) : '-';
    const reviewText = review.reviewText || 'Нет текста';
    const reviewId = review.reviewId || review.id;

    const starsHTML = Array.from({ length: 5 }, (_, i) => {
      return i < stars 
        ? '<i class="fas fa-star"></i>'
        : '<i class="far fa-star"></i>';
    }).join('');

    return `
      <div class="review-card ${!isVisible ? 'review-hidden' : ''}">
        <div class="review-header">
          <div class="review-user">
            <img src="${userPhoto}" alt="${userName}">
            <div>
              <h4>${userName}</h4>
              <div class="review-rating">${starsHTML}</div>
            </div>
          </div>
          <span class="review-date">${createdAt}</span>
        </div>
        <p class="review-text">${reviewText}</p>
        <div class="review-actions">
          ${!isVisible ? `
            <button class="btn-icon btn-success" title="Сделать видимым" onclick="toggleReviewVisibility(${reviewId})">
              <i class="fas fa-eye"></i>
            </button>
          ` : `
            <button class="btn-icon btn-warning" title="Сделать невидимым" onclick="toggleReviewVisibility(${reviewId})">
              <i class="fas fa-eye-slash"></i>
            </button>
          `}
          ${!isVisible ? '<span class="status-badge status-inactive" style="margin-left: 0.5rem;">Скрыт</span>' : ''}
        </div>
      </div>
    `;
  }).join('');
}

async function toggleReviewVisibility(reviewId) {
  try {
    const response = await fetch(`/api/v1/reviews/editVisibility/${reviewId}`, {
      method: 'PUT',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to toggle review visibility');
    }

    showSuccess('Видимость отзыва изменена');
    await fetchAllReviews();
  } catch (error) {
    console.error('Error toggling review visibility:', error);
    showError('Не удалось изменить видимость отзыва');
  }
}

function initReviewFilters() {
  const filterButtons = qsa('#reviews .filter-btn');
  filterButtons.forEach(btn => {
    btn.addEventListener('click', (e) => {
      filterButtons.forEach(b => b.classList.remove('active'));
      e.target.classList.add('active');
      currentReviewFilter = e.target.getAttribute('data-filter');
      renderReviews(allReviews);
    });
  });
}

// ========== ПЛАТЕЖИ ==========
async function fetchAllPayments() {
  const tbody = qs('#paymentsTableBody');
  if (tbody) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7" style="text-align: center; padding: 2rem;">
          <i class="fas fa-spinner fa-spin" style="font-size: 2rem; color: var(--primary);"></i>
          <p style="margin-top: 1rem; color: var(--gray);">Загрузка платежей...</p>
        </td>
      </tr>
    `;
  }

  try {
    const response = await fetch('/api/v1/admin/payments/getAllPayments', {
      method: 'GET',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to fetch payments');
    }

    const data = await response.json();
    allPayments = Array.isArray(data) ? data : [];
    console.log('Payments loaded:', allPayments);
    renderPayments(allPayments);
  } catch (error) {
    console.error('Error fetching payments:', error);
    if (tbody) {
      tbody.innerHTML = `
        <tr>
          <td colspan="7" style="text-align: center; padding: 2rem; color: var(--gray);">
            <i class="fas fa-exclamation-triangle" style="font-size: 2rem; margin-bottom: 1rem; color: var(--warning);"></i>
            <p style="margin-top: 1rem;">Не удалось загрузить список платежей</p>
            <p style="margin-top: 0.5rem; font-size: 0.9rem; opacity: 0.8;">Проверьте подключение к серверу</p>
          </td>
        </tr>
      `;
    }
    allPayments = [];
    showError('Не удалось загрузить список платежей');
  }
}

function renderPayments(payments) {
  const tbody = qs('#paymentsTableBody');
  if (!tbody) return;

  let filtered = payments;
  if (currentPaymentFilter !== 'all') {
    filtered = payments.filter(payment => {
      const status = payment.payment?.status || payment.status;
      return status === currentPaymentFilter;
    });
  }

  if (!filtered || filtered.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7" style="text-align: center; padding: 2rem; color: var(--gray);">
          Платежи не найдены
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = filtered.map(payment => {
    const paymentData = payment.payment || payment;
    const user = payment.user || {};
    const order = payment.order || {};
    const paymentId = paymentData.paymentId || paymentData.id || '-';
    const userName = user.login || user.username || `ID: ${paymentData.userId || '-'}`;
    const paymentType = paymentData.paymentType || '-';
    const amount = paymentData.amount ? parseFloat(paymentData.amount).toFixed(2) + ' BYN' : '-';
    const status = paymentData.status || 'UNKNOWN';
    const statusText = getPaymentStatusText(status);
    const statusClass = getPaymentStatusClass(status);
    const createdAt = paymentData.createdAt ? formatDateTime(paymentData.createdAt) : '-';

    return `
      <tr data-payment-id="${paymentId}">
        <td>#${paymentId}</td>
        <td>${userName}</td>
        <td>${paymentType}</td>
        <td>${amount}</td>
        <td><span class="status-badge ${statusClass}">${statusText}</span></td>
        <td>${createdAt}</td>
        <td>
          <button class="btn-icon btn-primary" title="Просмотреть" onclick="viewPaymentDetails(${paymentId})">
            <i class="fas fa-eye"></i>
          </button>
        </td>
      </tr>
    `;
  }).join('');
}

async function viewPaymentDetails(paymentId) {
  const payment = allPayments.find(p => {
    const pData = p.payment || p;
    return (pData.paymentId || pData.id) === paymentId;
  });
  if (!payment) {
    showError('Платеж не найден');
    return;
  }

  if (allPayments.length === 0) {
    showError('Данные платежей не загружены');
    return;
  }

  const modal = qs('#paymentDetailsModal');
  const body = qs('#paymentDetailsBody');
  if (!modal || !body) return;

  modal.classList.add('active');

  const paymentData = payment.payment || payment;
  const user = payment.user || {};
  const order = payment.order || {};
  const orderItems = order.orderItems || [];

  body.innerHTML = `
    <div class="detail-section">
      <h4><i class="fas fa-info-circle"></i> Основная информация о платеже</h4>
      <div class="detail-row">
        <div class="detail-label">ID платежа:</div>
        <div class="detail-value">#${paymentData.paymentId || paymentData.id || '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Сумма:</div>
        <div class="detail-value">${paymentData.amount ? parseFloat(paymentData.amount).toFixed(2) + ' BYN' : '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Тип платежа:</div>
        <div class="detail-value">${paymentData.paymentType || '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Статус:</div>
        <div class="detail-value">
          <span class="status-badge ${getPaymentStatusClass(paymentData.status)}">
            ${getPaymentStatusText(paymentData.status)}
          </span>
        </div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Дата создания:</div>
        <div class="detail-value">${paymentData.createdAt ? formatDateTime(paymentData.createdAt) : '-'}</div>
      </div>
    </div>

    <div class="detail-section">
      <h4><i class="fas fa-user"></i> Пользователь</h4>
      <div class="detail-row">
        <div class="detail-label">ID:</div>
        <div class="detail-value">${user.id || paymentData.userId || '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Логин:</div>
        <div class="detail-value">${user.login || user.username || '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Email:</div>
        <div class="detail-value">${user.email || '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Баланс:</div>
        <div class="detail-value">${user.wallet ? parseFloat(user.wallet).toFixed(2) + ' BYN' : '-'}</div>
      </div>
    </div>

    ${order.id ? `
    <div class="detail-section">
      <h4><i class="fas fa-shopping-cart"></i> Заказ</h4>
      <div class="detail-row">
        <div class="detail-label">ID заказа:</div>
        <div class="detail-value">#${order.id || '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Статус заказа:</div>
        <div class="detail-value">
          <span class="status-badge ${getOrderStatusClass(order.status)}">
            ${getOrderStatusText(order.status)}
          </span>
        </div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Общая стоимость:</div>
        <div class="detail-value">${order.totalCost ? parseFloat(order.totalCost).toFixed(2) + ' BYN' : '-'}</div>
      </div>
      <div class="detail-row">
        <div class="detail-label">Дата создания:</div>
        <div class="detail-value">${order.createdAt ? formatDateTime(order.createdAt) : '-'}</div>
      </div>
    </div>

    <div class="detail-section">
      <h4><i class="fas fa-box"></i> Товары в заказе (${orderItems.length})</h4>
      ${orderItems.length > 0 ? `
        <ul class="detail-list">
          ${orderItems.map(item => {
            const product = item.productDTO || item.product || {};
            const productName = product.name || 'Неизвестный товар';
            const quantity = item.quantity || 0;
            const price = item.productPrice ? parseFloat(item.productPrice).toFixed(2) : '0.00';
            const total = (parseFloat(price) * quantity).toFixed(2);
            return `
              <li class="detail-list-item">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                  <div>
                    <strong>${productName}</strong>
                    <div style="margin-top: 0.5rem; color: var(--gray); font-size: 0.9rem;">
                      Количество: ${quantity} × ${price} BYN = ${total} BYN
                    </div>
                  </div>
                </div>
              </li>
            `;
          }).join('')}
        </ul>
      ` : '<p style="color: var(--gray);">Товары не найдены</p>'}
    </div>
    ` : `
    <div class="detail-section">
      <h4><i class="fas fa-shopping-cart"></i> Заказ</h4>
      <p style="color: var(--gray);">Заказ не связан с этим платежом</p>
    </div>
    `}
  `;
}

function closePaymentDetailsModal() {
  const modal = qs('#paymentDetailsModal');
  if (modal) modal.classList.remove('active');
}

function initPaymentFilters() {
  const filterButtons = qsa('#payments .filter-btn');
  filterButtons.forEach(btn => {
    btn.addEventListener('click', (e) => {
      filterButtons.forEach(b => b.classList.remove('active'));
      e.target.classList.add('active');
      currentPaymentFilter = e.target.getAttribute('data-filter');
      renderPayments(allPayments);
    });
  });
}

// ========== ИНИЦИАЛИЗАЦИЯ ==========
async function init() {
  initNavigation();
  initModals();
  initSearch();
  initLogout();
  initCategoryFilter();
  initTariffPreview();
  initComputerSearch();
  initOrderFilters();
  initPaymentFilters();
  initReviewFilters();

  await fetchAllUsers();
  await fetchAllComputers();
}

document.addEventListener('DOMContentLoaded', init);