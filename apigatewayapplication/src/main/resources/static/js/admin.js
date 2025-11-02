const qs = (s, r = document) => r.querySelector(s);
const qsa = (s, r = document) => Array.from(r.querySelectorAll(s));

let allUsers = [];
let allProducts = [];
let currentUser = null;
let currentProduct = null;
let currentCategoryFilter = 'all';

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
        ? `http://localhost:8082${product.photoPath}`
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
      }
    });
  });
}

function initModals() {
  const modals = {
    bonus: qs('#bonusModal'),
    product: qs('#productModal'),
    editProduct: qs('#editProductModal'),
    tariff: qs('#tariffModal')
  };

  qs('#addProductBtn')?.addEventListener('click', openAddProductModal);

  qsa('.modal-close').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const modal = e.target.closest('.modal');
      modal?.classList.remove('active');
    });
  });

  qsa('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', (e) => {
      const modal = e.target.closest('.modal');
      modal?.classList.remove('active');
    });
  });

  qsa('.modal-footer .btn--secondary').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const modal = e.target.closest('.modal');
      modal?.classList.remove('active');
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

  qs('#addTariffBtn')?.addEventListener('click', () => {
    modals.tariff?.classList.add('active');
  });
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

async function init() {
  initNavigation();
  initModals();
  initSearch();
  initLogout();
  initCategoryFilter();

  await fetchAllUsers();
}

document.addEventListener('DOMContentLoaded', init);
