// ========== PROFILE DATA ==========

async function fetchAndFillProfile() {
  try {
    const response = await fetch("/api/v1/profile/getAllProfile", {
      method: "GET",
      credentials: "include",
    });

    if (!response.ok) {
      throw new Error("Не удалось получить информацию о профиле");
    }
    const profile = await response.json();
    setInfo(profile);
  } catch (err) {
    console.error(err);
    alert("Ошибка загрузки профиля");
  }
}

function parseDate(dateValue) {
  if (!dateValue) return null;

  if (Array.isArray(dateValue) && dateValue.length === 2 && dateValue[0] === "java.sql.Timestamp") {
    dateValue = dateValue[1];
  }

  if (typeof dateValue === 'string' && dateValue.includes(' ')) {
    dateValue = dateValue.replace(' ', 'T');
  }

  const date = new Date(dateValue);
  return isNaN(date.getTime()) ? null : date;
}

function setInfo(profile) {
  console.log("Full profile data:", JSON.stringify(profile, null, 2));

  const safe = (v) => (v === null || v === undefined ? "" : v);

  // Аватары
  const avatar = document.getElementById("profileAvatar");
  if (avatar) avatar.src = safe(profile.photoPath) || "https://i.pravatar.cc/160";

  const userAvatar = document.getElementById("userAvatar");
  if (userAvatar) userAvatar.src = safe(profile.photoPath) || "https://i.pravatar.cc/80";

  const previewAvatar = document.getElementById("previewAvatar");
  if (previewAvatar) previewAvatar.src = safe(profile.photoPath) || "https://i.pravatar.cc/160";

  // Имя и логин
  const profileName = document.getElementById("profileName");
  if (profileName) profileName.textContent = safe(profile.login);

  const userName = document.getElementById("userName");
  if (userName) userName.textContent = safe(profile.login);

  // Email
  const profileEmail = document.getElementById("profileEmail");
  if (profileEmail) profileEmail.textContent = safe(profile.email);

  // Баланс
  const balanceAmount = document.getElementById("balanceAmount");
  if (balanceAmount) balanceAmount.textContent = `${safe(profile.wallet)} BYN`;

  const userBalance = document.getElementById("userBalance");
  if (userBalance) userBalance.textContent = `${safe(profile.wallet)} BYN`;

  // Настройки
  const fullName = document.getElementById("fullName");
  if (fullName) fullName.value = safe(profile.fullName);

  const email = document.getElementById("email");
  if (email) email.value = safe(profile.email);

  const phone = document.getElementById("phone");
  if (phone) phone.value = safe(profile.phone);

  // Дата рождения
  const birthDateInput = document.getElementById("birthDate");
  if (birthDateInput) {
    const birthDate = parseDate(profile.birthDate);
    if (birthDate) {
      birthDateInput.value = birthDate.toISOString().split('T')[0];
      console.log("Birth date set:", birthDateInput.value);
    } else {
      console.warn("Invalid birthDate:", profile.birthDate);
    }
    birthDateInput.disabled = true;
  }

  // Статистика
  const statOrders = document.getElementById("stat-number-orders");
  if (statOrders) {
    const sessions = profile.sessionStats?.totalSessions || 0;
    statOrders.textContent = sessions;
    console.log("Total sessions:", sessions);
  }

  const statHours = document.getElementById("stat-number-hours");
  if (statHours) {
    const hours = profile.sessionStats?.totalGameHour || 0;
    statHours.textContent = Math.round(hours);
    console.log("Total hours:", hours);
  }

  const statDays = document.getElementById("stat-number-days");
  if (statDays) {
    const days = calculateDaysSinceRegistration(profile.registrationDate);
    statDays.textContent = days;
    console.log("Days since registration:", days, "from", profile.registrationDate);
  }

  const statBonus = document.getElementById("stat-number-bonus");
  if (statBonus) {
    const bonusCoins = profile.bonusCoins || 0;
    statBonus.textContent = bonusCoins;
    console.log("Bonus coins:", bonusCoins);
  }
}

function calculateDaysSinceRegistration(registrationDate) {
  if (!registrationDate) {
    console.warn("No registration date provided");
    return 0;
  }

  const regDate = parseDate(registrationDate);
  if (!regDate) {
    console.warn("Invalid registration date:", registrationDate);
    return 0;
  }

  const currentDate = new Date();
  const timeDiff = currentDate - regDate;
  const days = Math.floor(timeDiff / (1000 * 60 * 60 * 24));

  console.log("Registration date:", regDate, "Current date:", currentDate, "Days:", days);

  return days >= 0 ? days : 0;
}

// ========== USER PROFILE UPDATES ==========

async function saveProfileSettings() {
  const fullName = document.getElementById("fullName").value;
  const email = document.getElementById("email").value;
  const phone = document.getElementById("phone").value;

  const data = {
    fullName,
    email,
    phone
  };

  try {
    const response = await fetch("/api/v1/profile/update", {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify(data)
    });

    if (!response.ok) {
      throw new Error("Не удалось сохранить изменения");
    }

    const updatedUser = await response.json();
    updateUIWithUserData(updatedUser);

    alert("Настройки успешно сохранены!");
  } catch (err) {
    console.error(err);
    alert("Ошибка при сохранении настроек");
  }
}

function updateUIWithUserData(user) {
  const safe = (v) => (v === null || v === undefined ? "" : v);

  const fullName = document.getElementById("fullName");
  if (fullName) fullName.value = safe(user.fullName);

  const email = document.getElementById("email");
  if (email) email.value = safe(user.email);

  const profileEmail = document.getElementById("profileEmail");
  if (profileEmail) profileEmail.textContent = safe(user.email);

  const phone = document.getElementById("phone");
  if (phone) phone.value = safe(user.phone);

  console.log("UI updated with new user data");
}

async function uploadAvatar(file) {
  const formData = new FormData();
  formData.append("avatar", file);

  try {
    const response = await fetch("/api/v1/profile/upload-avatar", {
      method: "POST",
      credentials: "include",
      body: formData
    });

    if (!response.ok) {
      throw new Error("Не удалось загрузить фото");
    }

    const result = await response.json();
    return result.photoPath;
  } catch (err) {
    console.error("Ошибка загрузки фото:", err);
    throw err;
  }
}

// ========== SESSIONS ==========

async function fetchUserSessions() {
  try {
    const response = await fetch("/api/v1/sessions/mySessions", {
      method: "GET",
      credentials: "include",
      headers: {
        "Content-Type": "application/json"
      }
    });

    if (!response.ok) {
      throw new Error("Failed to fetch sessions");
    }

    const sessions = await response.json();
    console.log("Sessions loaded:", sessions);

    const bookings = sessions.filter(s =>
        ["PENDING", "PAID", "IN_PROGRESS"].includes(s.status)
    );

    const history = sessions.filter(s =>
        ["COMPLETED", "NOT_SHOW", "CANCELLED", "REFUNDED"].includes(s.status)
    );

    bookings.sort((a, b) => {
      if (a.status === "IN_PROGRESS" && b.status !== "IN_PROGRESS") return -1;
      if (a.status !== "IN_PROGRESS" && b.status === "IN_PROGRESS") return 1;
      return new Date(a.startTime) - new Date(b.startTime);
    });

    history.sort((a, b) => new Date(b.endTime) - new Date(a.endTime));

    renderBookings(bookings);
    renderHistory(history);

  } catch (error) {
    console.error("Error fetching sessions:", error);
    showError("Не удалось загрузить брони");
  }
}

function renderBookings(bookings) {
  const ordersList = document.querySelector(".orders-list");

  if (!ordersList) return;

  if (bookings.length === 0) {
    ordersList.innerHTML = `
      <div class="empty-state">
        <i class="fas fa-inbox"></i>
        <h3>У вас пока нет активных броней</h3>
        <p>Забронируйте место в нашем клубе</p>
        <a href="/static/html/booking.html" class="btn btn-primary">Забронировать</a>
      </div>
    `;
    return;
  }

  ordersList.innerHTML = bookings.map(session => {
    const statusInfo = getStatusInfo(session.status);
    const canCancel = ["PENDING", "PAID"].includes(session.status);
    const isInProgress = session.status === "IN_PROGRESS";
    const duration = calculateDuration(session.startTime, session.endTime);

    return `
      <div class="order-card ${isInProgress ? 'active-session' : ''}">
        <div class="order-header">
          <div class="order-info">
            <h3 class="order-number">Бронь #${session.sessionId}</h3>
            <span class="order-date">${formatSessionDate(session.startTime, session.endTime)}</span>
          </div>
          <div class="order-status ${statusInfo.class}">
            <i class="${statusInfo.icon}"></i>
            ${statusInfo.text}
          </div>
        </div>
        
        <div class="order-items">
          <div class="order-item">
            <div class="item-image">
              <i class="fas fa-desktop"></i>
            </div>
            <div class="item-info">
              <h4 class="item-name">${session.pc.name} — ${session.pc.roomName}</h4>
              <p class="item-description">
                ${session.pc.cpu} • ${session.pc.gpu} • ${session.pc.ram}
              </p>
              <p class="item-tariff">
                <i class="fas fa-ticket-alt"></i>
                ${session.tariff.name} — ${duration}
              </p>
            </div>
            <div class="item-price">${session.totalCost} BYN</div>
          </div>
        </div>
        
        <div class="order-footer">
          <div class="order-total">
            <span class="total-label">Итого:</span>
            <span class="total-amount">${session.totalCost} BYN</span>
          </div>
          <div class="order-actions">
            ${canCancel || isInProgress ? `
              ${canCancel ? `
                <button class="btn btn-secondary" onclick="cancelSession(${session.sessionId})">
                  <i class="fas fa-times"></i>
                  Отменить
                </button>
              ` : ''}
              <button class="btn btn-primary" onclick="makeOrder(${session.sessionId}, ${session.pc.id})">
                <i class="fas fa-cocktail"></i>
                Заказ из бара
              </button>
            ` : ''}
          </div>
        </div>
      </div>
    `;
  }).join('');
}

function renderHistory(history) {
  const historyList = document.querySelector(".history-list");

  if (!historyList) return;

  if (history.length === 0) {
    historyList.innerHTML = `
      <div class="empty-state">
        <i class="fas fa-history"></i>
        <h3>История пуста</h3>
        <p>Ваша игровая история появится здесь</p>
      </div>
    `;
    return;
  }

  const completed = history.filter(s => s.status === 'COMPLETED');
  const cancelled = history.filter(s => s.status === 'CANCELLED');
  const noShow = history.filter(s => s.status === 'NOT_SHOW');
  const refunded = history.filter(s => s.status === 'REFUNDED');

  let html = '';

  if (completed.length > 0) {
    html += `<div class="history-group">
      <h3 class="history-group-title">
        <i class="fas fa-check-circle"></i>
        Завершённые (${completed.length})
      </h3>
      ${renderHistoryItems(completed)}
    </div>`;
  }

  if (cancelled.length > 0) {
    html += `<div class="history-group">
      <h3 class="history-group-title">
        <i class="fas fa-times-circle"></i>
        Отменённые (${cancelled.length})
      </h3>
      ${renderHistoryItems(cancelled)}
    </div>`;
  }

  if (noShow.length > 0) {
    html += `<div class="history-group">
      <h3 class="history-group-title">
        <i class="fas fa-user-times"></i>
        Неявки (${noShow.length})
      </h3>
      ${renderHistoryItems(noShow)}
    </div>`;
  }

  if (refunded.length > 0) {
    html += `<div class="history-group">
      <h3 class="history-group-title">
        <i class="fas fa-undo"></i>
        Возвраты (${refunded.length})
      </h3>
      ${renderHistoryItems(refunded)}
    </div>`;
  }

  historyList.innerHTML = html;
}

function renderHistoryItems(sessions) {
  return sessions.map(session => {
    const statusInfo = getStatusInfo(session.status);
    const duration = calculateDuration(session.startTime, session.endTime);

    return `
      <div class="history-item">
        <div class="history-image">
          <i class="fas fa-desktop"></i>
        </div>
        <div class="history-info">
          <h4 class="history-name">${session.pc.name} — ${duration}</h4>
          <p class="history-description">
            ${session.pc.roomName} • ${session.tariff.name}
          </p>
          <span class="history-date">${formatDate(session.endTime)}</span>
        </div>
        <div class="history-price">${session.totalCost} BYN</div>
        <div class="history-status ${statusInfo.class}">
          <i class="${statusInfo.icon}"></i>
          ${statusInfo.text}
        </div>
      </div>
    `;
  }).join('');
}

async function cancelSession(sessionId) {
  try {
    const response = await fetch(`/api/v1/sessions/cancelSession/${sessionId}`, {
      method: 'PUT',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Failed to cancel session');
    }

    alert('Бронирование успешно отменено');
    await fetchUserSessions();

  } catch (error) {
    console.error('Error cancelling session:', error);
    alert('Ошибка при отмене бронирования: ' + error.message);
  }
}

// ========== BAR ORDER ==========

let currentSessionId = null;
let currentPcId = null;
let products = [];
let cart = [];

function makeOrder(sessionId, pcId) {
  currentSessionId = sessionId;
  currentPcId = pcId;

  const modal = document.getElementById('barOrderModal');
  const sessionIdSpan = document.getElementById('currentSessionId');

  if (modal && sessionIdSpan) {
    sessionIdSpan.textContent = sessionId;
    modal.style.display = 'flex';
    loadProducts();
  }
}

async function loadProducts() {
  const productsGrid = document.getElementById('productsGrid');

  if (!productsGrid) return;

  productsGrid.innerHTML = `
        <div class="loading-products">
            <i class="fas fa-spinner"></i>
            <p>Загрузка товаров...</p>
        </div>
    `;

  try {
    const response = await fetch('/api/v1/products/getAllProducts', {
      method: 'GET',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to load products');
    }

    products = await response.json();
    renderProducts(products);
    setupCategoryFilters();

  } catch (error) {
    console.error('Error loading products:', error);
    productsGrid.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-exclamation-circle"></i>
                <h3>Ошибка загрузки товаров</h3>
                <p>Попробуйте позже</p>
            </div>
        `;
  }
}

function renderProducts(productList) {
  const productsGrid = document.getElementById('productsGrid');

  if (!productsGrid) return;

  if (productList.length === 0) {
    productsGrid.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-box-open"></i>
                <h3>Товары не найдены</h3>
            </div>
        `;
    return;
  }

  productsGrid.innerHTML = productList.map(product => {
    const isOutOfStock = !product.active || product.stock === 0;

    return `
            <div class="product-card ${isOutOfStock ? 'out-of-stock' : ''}" 
                 onclick="${!isOutOfStock ? `addToCart(${product.id})` : ''}"
                 data-category="${product.category?.name || 'Другое'}">
                <div class="product-image">
                    ${product.photoPath ?
        `<img src="${product.photoPath}" alt="${product.name}">` :
        `<i class="fas fa-box"></i>`
    }
                </div>
                <div class="product-info">
                    <h4 class="product-name">${product.name}</h4>
                    <span class="product-category">${product.category?.name || 'Другое'}</span>
                </div>
                <div class="product-footer">
                    <span class="product-price">${product.price} BYN</span>
                    ${isOutOfStock ?
        '<span class="out-of-stock-label">Нет в наличии</span>' :
        `<span class="product-stock">В наличии: ${product.stock}</span>`
    }
                </div>
            </div>
        `;
  }).join('');
}

function setupCategoryFilters() {
  const filterBtns = document.querySelectorAll('.category-btn');

  filterBtns.forEach(btn => {
    btn.addEventListener('click', function() {
      filterBtns.forEach(b => b.classList.remove('active'));
      this.classList.add('active');

      const category = this.getAttribute('data-category');
      filterProducts(category);
    });
  });
}

function filterProducts(category) {
  if (category === 'all') {
    renderProducts(products);
  } else {
    const filtered = products.filter(p => p.category?.name === category);
    renderProducts(filtered);
  }
}

function addToCart(productId) {
  const product = products.find(p => p.id === productId);

  if (!product || !product.active || product.stock === 0) {
    return;
  }

  const existingItem = cart.find(item => item.productId === productId);

  if (existingItem) {
    if (existingItem.quantity < product.stock) {
      existingItem.quantity++;
    } else {
      alert(`Максимальное количество: ${product.stock}`);
      return;
    }
  } else {
    cart.push({
      productId: product.id,
      name: product.name,
      price: product.price,
      quantity: 1,
      maxStock: product.stock
    });
  }

  updateCart();
}

function updateCart() {
  const cartSection = document.getElementById('cartSection');
  const cartItems = document.getElementById('cartItems');
  const cartCount = document.getElementById('cartCount');
  const cartTotal = document.getElementById('cartTotal');
  const confirmBtn = document.getElementById('confirmBarOrder');

  if (!cartSection || !cartItems) return;

  if (cart.length === 0) {
    cartSection.style.display = 'none';
    confirmBtn.disabled = true;
    return;
  }

  cartSection.style.display = 'block';
  confirmBtn.disabled = false;

  const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
  cartCount.textContent = totalItems;

  cartItems.innerHTML = cart.map(item => `
        <div class="cart-item">
            <div class="cart-item-info">
                <h5 class="cart-item-name">${item.name}</h5>
                <span class="cart-item-price">${item.price} BYN × ${item.quantity}</span>
            </div>
            <div class="cart-item-controls">
                <div class="quantity-control">
                    <button class="quantity-btn" onclick="updateQuantity(${item.productId}, -1)">
                        <i class="fas fa-minus"></i>
                    </button>
                    <span class="quantity-value">${item.quantity}</span>
                    <button class="quantity-btn" onclick="updateQuantity(${item.productId}, 1)">
                        <i class="fas fa-plus"></i>
                    </button>
                </div>
                <button class="btn-remove-item" onclick="removeFromCart(${item.productId})">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        </div>
    `).join('');

  const total = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  cartTotal.textContent = `${total.toFixed(2)} BYN`;
}

function updateQuantity(productId, delta) {
  const item = cart.find(i => i.productId === productId);

  if (!item) return;

  const newQuantity = item.quantity + delta;

  if (newQuantity <= 0) {
    removeFromCart(productId);
  } else if (newQuantity <= item.maxStock) {
    item.quantity = newQuantity;
    updateCart();
  } else {
    alert(`Максимальное количество: ${item.maxStock}`);
  }
}

function removeFromCart(productId) {
  cart = cart.filter(item => item.productId !== productId);
  updateCart();
}

function clearCart() {
  cart = [];
  updateCart();
}

async function submitBarOrder() {
  if (cart.length === 0) {
    alert('Корзина пуста');
    return;
  }

  const orderData = {
    sessionId: currentSessionId,
    pcId: currentPcId,
    items: cart.map(item => ({
      productId: item.productId,
      quantity: item.quantity
    }))
  };

  try {
    const response = await fetch('/api/v1/orders/createOrder', {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(orderData)
    });

    if (!response.ok) {
      throw new Error('Failed to create order');
    }

    alert('Заказ успешно оформлен! Ожидайте доставку к вашему месту.');
    closeBarOrderModal();

  } catch (error) {
    console.error('Error creating order:', error);
    alert('Ошибка при оформлении заказа. Попробуйте позже.');
  }
}

function closeBarOrderModal() {
  const modal = document.getElementById('barOrderModal');
  if (modal) {
    modal.style.display = 'none';
    cart = [];
    updateCart();
  }
}

// ========== REVIEWS ==========

function initReviews() {
  const stars = document.querySelectorAll('.stars i');
  let selectedRating = 0;

  stars.forEach(star => {
    star.addEventListener('click', function() {
      selectedRating = parseInt(this.getAttribute('data-rating'));
      updateStars(selectedRating);
    });

    star.addEventListener('mouseenter', function() {
      const hoverRating = parseInt(this.getAttribute('data-rating'));
      updateStars(hoverRating);
    });
  });

  const starsContainer = document.querySelector('.stars');
  if (starsContainer) {
    starsContainer.addEventListener('mouseleave', () => {
      updateStars(selectedRating);
    });
  }

  function updateStars(rating) {
    stars.forEach((star, index) => {
      if (index < rating) {
        star.classList.add('active');
      } else {
        star.classList.remove('active');
      }
    });
  }

  const reviewText = document.getElementById('reviewText');
  const charCount = document.querySelector('.char-count');

  if (reviewText && charCount) {
    reviewText.addEventListener('input', () => {
      const length = reviewText.value.length;
      charCount.textContent = `${length}/500`;
    });
  }

  const submitBtn = document.getElementById('submitReviewBtn');
  if (submitBtn) {
    submitBtn.addEventListener('click', async () => {
      if (selectedRating === 0) {
        alert('Пожалуйста, выберите оценку');
        return;
      }

      const text = reviewText.value.trim();
      if (!text) {
        alert('Пожалуйста, напишите отзыв');
        return;
      }

      try {
        await submitReview(selectedRating, text);
        alert('Спасибо за ваш отзыв!');
        reviewText.value = '';
        selectedRating = 0;
        updateStars(0);
        charCount.textContent = '0/500';
        fetchReviews();
      } catch (error) {
        console.error('Error submitting review:', error);
        alert('Ошибка при отправке отзыва');
      }
    });
  }

  fetchReviews();
}

async function submitReview(rating, text) {
  const response = await fetch('/api/v1/reviews/create', {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ rating, text })
  });

  if (!response.ok) {
    throw new Error('Failed to submit review');
  }

  return response.json();
}

async function fetchReviews() {
  try {
    const response = await fetch('/api/v1/reviews/my', {
      method: 'GET',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to fetch reviews');
    }

    const reviews = await response.json();
    renderReviews(reviews);
  } catch (error) {
    console.error('Error fetching reviews:', error);
  }
}

function renderReviews(reviews) {
  const reviewsList = document.getElementById('reviewsList');
  if (!reviewsList) return;

  if (reviews.length === 0) {
    reviewsList.innerHTML = `
      <div class="empty-state">
        <i class="fas fa-comment-dots"></i>
        <h3>У вас пока нет отзывов</h3>
        <p>Оставьте первый отзыв о нашем клубе</p>
      </div>
    `;
    return;
  }

  reviewsList.innerHTML = reviews.map(review => `
    <div class="review-card">
      <div class="review-header">
        <div class="review-rating">
          ${generateStars(review.rating)}
        </div>
        <span class="review-date">${formatDate(review.createdAt)}</span>
      </div>
      <p class="review-text">${review.text}</p>
      ${review.response ? `
        <div class="admin-response">
          <div class="response-header">
            <i class="fas fa-reply"></i>
            <span>Ответ администратора</span>
          </div>
          <p class="response-text">${review.response}</p>
        </div>
      ` : ''}
    </div>
  `).join('');
}

function generateStars(rating) {
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

// ========== UTILITIES ==========

function getStatusInfo(status) {
  const statusMap = {
    PENDING: {
      text: "Ожидает оплаты",
      class: "status-pending",
      icon: "fas fa-clock"
    },
    PAID: {
      text: "Оплачено",
      class: "status-paid",
      icon: "fas fa-check-circle"
    },
    IN_PROGRESS: {
      text: "В процессе",
      class: "status-in-progress",
      icon: "fas fa-gamepad"
    },
    COMPLETED: {
      text: "Завершено",
      class: "status-delivered",
      icon: "fas fa-check-circle"
    },
    NOT_SHOW: {
      text: "Неявка",
      class: "status-no-show",
      icon: "fas fa-user-times"
    },
    CANCELLED: {
      text: "Отменено",
      class: "status-cancelled",
      icon: "fas fa-times-circle"
    },
    REFUNDED: {
      text: "Возвращено",
      class: "status-refunded",
      icon: "fas fa-undo"
    }
  };

  return statusMap[status] || {
    text: status,
    class: "status-unknown",
    icon: "fas fa-question-circle"
  };
}

function formatSessionDate(startTime, endTime) {
  const start = new Date(startTime);
  const end = new Date(endTime);

  const dateOptions = { day: 'numeric', month: 'long', year: 'numeric' };
  const timeOptions = { hour: '2-digit', minute: '2-digit' };

  const isSameDay = start.toDateString() === end.toDateString();

  if (isSameDay) {
    const dateStr = start.toLocaleDateString('ru-RU', dateOptions);
    const startTimeStr = start.toLocaleTimeString('ru-RU', timeOptions);
    const endTimeStr = end.toLocaleTimeString('ru-RU', timeOptions);
    return `${dateStr}, ${startTimeStr}–${endTimeStr}`;
  } else {
    const startDateStr = start.toLocaleDateString('ru-RU', { day: 'numeric', month: 'long' });
    const endDateStr = end.toLocaleDateString('ru-RU', { day: 'numeric', month: 'long', year: 'numeric' });
    const startTimeStr = start.toLocaleTimeString('ru-RU', timeOptions);
    const endTimeStr = end.toLocaleTimeString('ru-RU', timeOptions);
    return `${startDateStr} ${startTimeStr} – ${endDateStr} ${endTimeStr}`;
  }
}

function calculateDuration(startTime, endTime) {
  const start = new Date(startTime);
  const end = new Date(endTime);

  if (isNaN(start.getTime()) || isNaN(end.getTime())) {
    console.error('Invalid dates:', startTime, endTime);
    return 'Неизвестно';
  }

  const diffMs = end - start;

  if (diffMs < 0) {
    console.error('End time is before start time:', startTime, endTime);
    return 'Ошибка';
  }

  const totalMinutes = Math.floor(diffMs / (1000 * 60));
  const days = Math.floor(totalMinutes / (24 * 60));
  const hours = Math.floor((totalMinutes % (24 * 60)) / 60);
  const minutes = totalMinutes % 60;

  const parts = [];

  if (days > 0) {
    parts.push(`${days} ${getDaysLabel(days)}`);
  }

  if (hours > 0) {
    parts.push(`${hours} ${getHoursLabel(hours)}`);
  }

  if (minutes > 0 && days === 0) {
    parts.push(`${minutes} мин`);
  }

  return parts.join(' ') || '0 мин';
}

function getDaysLabel(days) {
  const lastDigit = days % 10;
  const lastTwoDigits = days % 100;

  if (lastTwoDigits >= 11 && lastTwoDigits <= 14) {
    return 'дней';
  }
  if (lastDigit === 1) {
    return 'день';
  }
  if (lastDigit >= 2 && lastDigit <= 4) {
    return 'дня';
  }
  return 'дней';
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

function formatDate(dateValue) {
  const date = new Date(dateValue);
  return date.toLocaleDateString('ru-RU', {
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  });
}

function showError(message) {
  alert(message);
}

// ========== INTERACTIONS ==========

function initInteractions() {
  const navItems = document.querySelectorAll(".nav-item");
  const sections = document.querySelectorAll(".profile-section");

  navItems.forEach((item) => {
    item.addEventListener("click", function () {
      navItems.forEach((nav) => nav.classList.remove("active"));
      sections.forEach((section) => section.classList.remove("active"));
      this.classList.add("active");
      const targetSection = this.getAttribute("data-section");
      const targetEl = document.getElementById(targetSection);
      if (targetEl) targetEl.classList.add("active");
    });
  });

  const uploadAvatarBtn = document.getElementById("uploadAvatarBtn");
  const avatarInput = document.getElementById("avatarInput");
  const previewAvatar = document.getElementById("previewAvatar");

  if (uploadAvatarBtn && avatarInput) {
    uploadAvatarBtn.addEventListener("click", () => {
      avatarInput.click();
    });

    avatarInput.addEventListener("change", async (e) => {
      const file = e.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (event) => {
          previewAvatar.src = event.target.result;
        };
        reader.readAsDataURL(file);

        try {
          const photoPath = await uploadAvatar(file);

          const avatar = document.getElementById("profileAvatar");
          if (avatar) avatar.src = photoPath;

          const userAvatar = document.getElementById("userAvatar");
          if (userAvatar) userAvatar.src = photoPath;

          alert("Фото успешно загружено!");
        } catch (err) {
          console.error(err);
          alert("Ошибка при загрузке фото");
        }
      }
    });
  }

  const saveSettingsBtn = document.getElementById("saveSettingsBtn");
  if (saveSettingsBtn) {
    saveSettingsBtn.addEventListener("click", async () => {
      await saveProfileSettings();
    });
  }

  const cancelSettingsBtn = document.getElementById("cancelSettingsBtn");
  if (cancelSettingsBtn) {
    cancelSettingsBtn.addEventListener("click", () => {
      fetchAndFillProfile();
    });
  }

  const menuToggle = document.querySelector(".menu-toggle");
  const navMenu = document.querySelector(".nav-menu");
  if (menuToggle && navMenu) {
    menuToggle.addEventListener("click", () => {
      navMenu.classList.toggle("active");
    });
  }

  const topupBtn = document.getElementById("topupBtn");
  const modal = document.getElementById("topupModal");
  const modalClose = document.getElementById("modalClose");
  const cancelTopup = document.getElementById("cancelTopup");

  if (topupBtn && modal) {
    topupBtn.addEventListener("click", () => {
      modal.style.display = "flex";
    });
  }
  if (modalClose && modal) {
    modalClose.addEventListener("click", () => {
      modal.style.display = "none";
    });
  }
  if (cancelTopup && modal) {
    cancelTopup.addEventListener("click", () => {
      modal.style.display = "none";
    });
  }
  if (modal) {
    modal.addEventListener("click", (e) => {
      if (e.target === modal) modal.style.display = "none";
    });
  }

  const amountBtns = document.querySelectorAll(".amount-btn");
  amountBtns.forEach((btn) => {
    btn.addEventListener("click", function () {
      amountBtns.forEach((b) => b.classList.remove("active"));
      this.classList.add("active");
    });
  });

  const logoutBtn = document.getElementById("logoutBtn");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", async () => {
      try {
        await fetch("/api/v1/auth/logout", {
          method: "POST",
          credentials: "include"
        });
        window.location.href = "/static/html/index.html";
      } catch (err) {
        console.error("Logout error:", err);
        window.location.href = "/static/html/index.html";
      }
    });
  }
}

function initBarOrderModal() {
  const modal = document.getElementById('barOrderModal');
  const closeBtn = document.getElementById('barOrderModalClose');
  const cancelBtn = document.getElementById('cancelBarOrder');
  const confirmBtn = document.getElementById('confirmBarOrder');
  const clearBtn = document.getElementById('clearCartBtn');

  if (closeBtn) {
    closeBtn.addEventListener('click', closeBarOrderModal);
  }

  if (cancelBtn) {
    cancelBtn.addEventListener('click', closeBarOrderModal);
  }

  if (confirmBtn) {
    confirmBtn.addEventListener('click', submitBarOrder);
  }

  if (clearBtn) {
    clearBtn.addEventListener('click', clearCart);
  }

  if (modal) {
    modal.addEventListener('click', (e) => {
      if (e.target === modal) closeBarOrderModal();
    });
  }
}

// ========== INIT ==========

document.addEventListener("DOMContentLoaded", () => {
  fetchAndFillProfile();
  fetchUserSessions();
  initInteractions();
  initReviews();
  initBarOrderModal();
});
