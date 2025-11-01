const qs = (s, r = document) => r.querySelector(s);
const qsa = (s, r = document) => Array.from(r.querySelectorAll(s));

let allUsers = [];
let currentUser = null;

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
    const response = await fetch(`/api/v1/users/blockUser/${userId}`, {  // ← Твой путь
      method: 'PUT',  // ← Твой метод
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
      method: 'PUT',  // ← Твой метод
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


// Delete user
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
    await fetchAllUsers(); // Refresh list
  } catch (error) {
    console.error('Error deleting user:', error);
    showError('Не удалось удалить пользователя');
  }
}

// Add bonus
function handleAddBonus(userId, username) {
  currentUser = { id: userId, login: username };
  const modal = qs('#bonusModal');
  const usernameInput = modal?.querySelector('.form-input[readonly]');
  if (usernameInput) {
    usernameInput.value = username;
  }
  if (modal) modal.classList.add('active');
}


// Search users
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

        return login.includes(query) ||
            email.includes(query) ||
            fullName.includes(query);
      });

      renderUsers(filtered);
    });
  }
}

// Show notifications
function showSuccess(message) {
  // Simple alert for now, можно заменить на toast notification
  alert(message);
}

function showError(message) {
  alert('Ошибка: ' + message);
}

// Navigation
function initNavigation() {
  const navItems = qsa('.nav-item');
  const sections = qsa('.section');

  navItems.forEach(item => {
    item.addEventListener('click', () => {
      const sectionId = item.getAttribute('data-section');

      // Update nav
      navItems.forEach(nav => nav.classList.remove('active'));
      item.classList.add('active');

      // Update sections
      sections.forEach(section => section.classList.remove('active'));
      const targetSection = qs(`#${sectionId}`);
      if (targetSection) targetSection.classList.add('active');
    });
  });
}

// Modals
function initModals() {
  const modals = {
    bonus: qs('#bonusModal'),
    product: qs('#productModal'),
    tariff: qs('#tariffModal')
  };

  const openModal = (modalId) => {
    const modal = modals[modalId];
    if (modal) modal.classList.add('active');
  };

  const closeModal = (modal) => {
    modal.classList.remove('active');
  };

  // Add Product Button
  qs('#addProductBtn')?.addEventListener('click', () => openModal('product'));

  // Add Tariff Button
  qs('#addTariffBtn')?.addEventListener('click', () => openModal('tariff'));

  // Close buttons
  qsa('.modal-close').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const modal = e.target.closest('.modal');
      closeModal(modal);
    });
  });

  // Overlay click
  qsa('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', (e) => {
      const modal = e.target.closest('.modal');
      closeModal(modal);
    });
  });

  // Cancel buttons
  qsa('.modal-footer .btn--secondary').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const modal = e.target.closest('.modal');
      closeModal(modal);
    });
  });

  // Bonus modal submit
  const bonusModal = qs('#bonusModal');
  const bonusSubmitBtn = bonusModal?.querySelector('.modal-footer .btn--primary');
  if (bonusSubmitBtn) {
    bonusSubmitBtn.addEventListener('click', submitBonus);
  }
}

// Product Actions
function initProductActions() {
  // Toggle active/inactive
  qsa('.btn-warning[title="В неактивные"]').forEach(btn => {
    btn.addEventListener('click', () => {
      // TODO: API call
      alert('Товар переведён в неактивные');
    });
  });

  qsa('.btn-success[title="В активные"]').forEach(btn => {
    btn.addEventListener('click', () => {
      // TODO: API call
      alert('Товар переведён в активные');
    });
  });

  // Edit
  qsa('.product-card .btn-primary[title="Редактировать"]').forEach(btn => {
    btn.addEventListener('click', () => {
      const modal = qs('#productModal');
      if (modal) modal.classList.add('active');
    });
  });

  // Delete
  qsa('.product-card .btn-danger[title="Удалить"]').forEach(btn => {
    btn.addEventListener('click', () => {
      if (confirm('Вы уверены, что хотите удалить товар?')) {
        // TODO: API call
        alert('Товар удалён');
      }
    });
  });
}

// Tariff Actions
function initTariffActions() {
  // Edit
  qsa('.tariff-card .btn-primary[title="Редактировать"]').forEach(btn => {
    btn.addEventListener('click', () => {
      const modal = qs('#tariffModal');
      if (modal) modal.classList.add('active');
    });
  });

  // Delete
  qsa('.tariff-card .btn-danger[title="Удалить"]').forEach(btn => {
    btn.addEventListener('click', () => {
      if (confirm('Вы уверены, что хотите удалить тариф?')) {
        // TODO: API call
        alert('Тариф удалён');
      }
    });
  });
}

// Order Actions
function initOrderActions() {
  // Complete order
  qsa('.btn-success[title="Выполнен"]').forEach(btn => {
    btn.addEventListener('click', () => {
      if (confirm('Отметить заказ как выполненный?')) {
        // TODO: API call
        alert('Заказ выполнен');
      }
    });
  });
}

// Review Actions
function initReviewActions() {
  // Delete review
  qsa('.review-card .btn-danger[title="Удалить отзыв"]').forEach(btn => {
    btn.addEventListener('click', () => {
      if (confirm('Вы уверены, что хотите удалить отзыв?')) {
        // TODO: API call
        alert('Отзыв удалён');
      }
    });
  });
}

// Filters
function initFilters() {
  qsa('.filter-btn').forEach(btn => {
    btn.addEventListener('click', function() {
      const container = this.parentElement;
      const filterBtns = container.querySelectorAll('.filter-btn');

      filterBtns.forEach(b => b.classList.remove('active'));
      this.classList.add('active');

      // TODO: Filter logic for orders/reviews
    });
  });
}

/**
 * Logout user and redirect to home page.
 * Clears authentication cookie on the server.
 */
async function initLogout() {
  qs('#logoutBtn')?.addEventListener('click', async () => {
    try {
      const response = await fetch('/api/v1/auth/logout', {  // ← Изменил путь
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


async function init() {
  initNavigation();
  initModals();
  initProductActions();
  initTariffActions();
  initOrderActions();
  initReviewActions();
  initFilters();
  initSearch();
  initLogout();

  await fetchAllUsers();
}

document.addEventListener('DOMContentLoaded', init);
