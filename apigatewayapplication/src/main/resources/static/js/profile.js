document.addEventListener("DOMContentLoaded", () => {
  fetchAndFillProfile();
  initInteractions();
});

async function fetchAndFillProfile() {
  try {
    const response = await fetch("/profile/getAllProfileInfo", {
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
  }
}

function setInfo(profile) {
  const safe = (v) => (v === null || v === undefined ? "" : v);

  const avatar = document.getElementById("profileAvatar");
  if (avatar) avatar.src = safe(profile.photoPath) || "https://i.pravatar.cc/160";

  const profileName = document.getElementById("profileName");
  if (profileName) profileName.textContent = safe(profile.login);

  const profileEmail = document.getElementById("profileEmail");
  if (profileEmail) profileEmail.textContent = safe(profile.email);

  const balanceAmount = document.getElementById("balanceAmount");
  if (balanceAmount) balanceAmount.textContent = `${safe(profile.wallet)} BYN`;

  const userBalance = document.getElementById("userBalance");
  if (userBalance) userBalance.textContent = `${safe(profile.wallet)} BYN`;

  const userName = document.getElementById("userName");
  if (userName) userName.textContent = safe(profile.login);

  const userAvatar = document.getElementById("userAvatar");
  if (userAvatar) userAvatar.src = safe(profile.photoPath) || "https://i.pravatar.cc/80";

  const firstName = document.getElementById("firstName");
  if (firstName) firstName.value = safe(profile.firstName);

  const lastName = document.getElementById("lastName");
  if (lastName) lastName.value = safe(profile.lastName);

  const email = document.getElementById("email");
  if (email) email.value = safe(profile.email);

  const phone = document.getElementById("phone");
  if (phone) phone.value = safe(profile.phone);

  const address = document.getElementById("address");
  if (address) address.value = safe(profile.deliveryAddress);

  const statOrders = document.getElementById("stat-number-orders");
  if (statOrders) statOrders.textContent = (profile.orders || []).length;

  const statDays = document.getElementById("stat-number-days");
  if (statDays) statDays.textContent = calculateDaysSinceRegistration(profile.registrationDate);

  const statHours = document.getElementById("stat-number-hours");
  if (statHours) {
    const hours = profile.totalPlayedHours || profile.playedHours || 0;
    statHours.textContent = String(hours);
  }

  renderActivities(profile.activities || []);
}

function calculateDaysSinceRegistration(registrationDate) {
  if (!registrationDate) return 0;
  const regDate = new Date(registrationDate);
  const currentDate = new Date();
  const timeDiff = currentDate - regDate;
  return Math.floor(timeDiff / (1000 * 60 * 60 * 24));
}

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

  // Responsive menu toggle (if used)
  const menuToggle = document.querySelector(".menu-toggle");
  const navMenu = document.querySelector(".nav-menu");
  if (menuToggle && navMenu) {
    menuToggle.addEventListener("click", () => {
      navMenu.classList.toggle("active");
    });
  }

  // Modal functionality
  const topupBtn = document.getElementById("topupBtn");
  const modal = document.getElementById("topupModal");
  const modalClose = document.getElementById("modalClose");
  const cancelTopup = document.getElementById("cancelTopup");

  if (topupBtn && modal) {
    topupBtn.addEventListener("click", function () {
      modal.style.display = "flex";
    });
  }
  if (modalClose && modal) {
    modalClose.addEventListener("click", function () {
      modal.style.display = "none";
    });
  }
  if (cancelTopup && modal) {
    cancelTopup.addEventListener("click", function () {
      modal.style.display = "none";
    });
  }
  if (modal) {
    modal.addEventListener("click", function (e) {
      if (e.target === modal) modal.style.display = "none";
    });
  }

  // Amount selection
  const amountBtns = document.querySelectorAll(".amount-btn");
  amountBtns.forEach((btn) => {
    btn.addEventListener("click", function () {
      amountBtns.forEach((b) => b.classList.remove("active"));
      this.classList.add("active");
    });
  });

  // cart interactions removed

  // History filters
  const filterBtns = document.querySelectorAll(".filter-btn");
  const historyItems = document.querySelectorAll(".history-item");
  filterBtns.forEach((btn) => {
    btn.addEventListener("click", function () {
      filterBtns.forEach((b) => b.classList.remove("active"));
      this.classList.add("active");
      const filter = this.getAttribute("data-filter");
      historyItems.forEach((item) => {
        if (filter === "all" || item.getAttribute("data-category") === filter) {
          item.style.display = "flex";
        } else {
          item.style.display = "none";
        }
      });
    });
  });
}

function renderActivities(activities) {
  const activitiesContainer = document.getElementById("activity-list");
  if (!activitiesContainer) return;
  if (!activities || activities.length === 0) {
    activitiesContainer.innerHTML = '<p class="no-activities">Активности не найдены</p>';
    return;
  }
  const activitiesHTML = activities
    .map((activity) => {
      const iconClass = getActivityIcon(activity.type);
      const title = getActivityTitle(activity.type);
      return `
            <div class="activity-item">
                <div class="activity-icon">
                    <i class="${iconClass}"></i>
                </div>
                <div class="activity-content">
                    <h4 class="activity-title">${title}</h4>
                    <p class="activity-description">${activity.description || ''}</p>
                    <span class="activity-time">${activity.timeAgo || ''}</span>
                </div>
            </div>
        `;
    })
    .join("");
  activitiesContainer.innerHTML = activitiesHTML;
}

function getActivityIcon(activityType) {
  const iconMap = {
    BOOK_ADDED_TO_CART: "fas fa-shopping-cart",
    REVIEW_ADDED: "fas fa-star",
    BALANCE_TOP_UP: "fas fa-credit-card",
    BOOK_PURCHASED: "fas fa-ticket",
    PROFILE_UPDATED: "fas fa-user-edit",
    SESSION_BOOKED: "fas fa-chair",
    SESSION_COMPLETED: "fas fa-check-circle",
  };
  return iconMap[activityType] || "fas fa-bell";
}

function getActivityTitle(activityType) {
  const titleMap = {
    BOOK_ADDED_TO_CART: "Добавлено в корзину",
    REVIEW_ADDED: "Оставлен отзыв",
    BALANCE_TOP_UP: "Пополнен баланс",
    BOOK_PURCHASED: "Покупка оформлена",
    PROFILE_UPDATED: "Профиль обновлен",
    SESSION_BOOKED: "Игровая сессия забронирована",
    SESSION_COMPLETED: "Сессия завершена",
  };
  return titleMap[activityType] || "Новая активность";
}


