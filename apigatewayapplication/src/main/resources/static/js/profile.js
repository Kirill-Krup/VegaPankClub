document.addEventListener("DOMContentLoaded", () => {
  fetchAndFillProfile();
  initInteractions();
});

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

  // Загрузка фото
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
        // Показываем превью
        const reader = new FileReader();
        reader.onload = (event) => {
          previewAvatar.src = event.target.result;
        };
        reader.readAsDataURL(file);

        // Загружаем на сервер
        try {
          const photoPath = await uploadAvatar(file);

          // Обновляем аватары на странице
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

  // Сохранение настроек
  const saveSettingsBtn = document.getElementById("saveSettingsBtn");
  if (saveSettingsBtn) {
    saveSettingsBtn.addEventListener("click", async () => {
      await saveProfileSettings();
    });
  }

  // Отмена изменений
  const cancelSettingsBtn = document.getElementById("cancelSettingsBtn");
  if (cancelSettingsBtn) {
    cancelSettingsBtn.addEventListener("click", () => {
      fetchAndFillProfile();
    });
  }

  // Responsive menu toggle
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

  // Amount selection
  const amountBtns = document.querySelectorAll(".amount-btn");
  amountBtns.forEach((btn) => {
    btn.addEventListener("click", function () {
      amountBtns.forEach((b) => b.classList.remove("active"));
      this.classList.add("active");
    });
  });

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

  // Logout
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

// Обновление профиля (возвращает UserDTO)
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

    // Получаем обновлённые данные (UserDTO)
    const updatedUser = await response.json();

    // Обновляем UI с новыми данными
    updateUIWithUserData(updatedUser);

    alert("Настройки успешно сохранены!");
  } catch (err) {
    console.error(err);
    alert("Ошибка при сохранении настроек");
  }
}

// Обновление UI после изменения профиля
function updateUIWithUserData(user) {
  const safe = (v) => (v === null || v === undefined ? "" : v);

  // Обновляем fullName в форме
  const fullName = document.getElementById("fullName");
  if (fullName) fullName.value = safe(user.fullName);

  // Обновляем email в форме и в sidebar
  const email = document.getElementById("email");
  if (email) email.value = safe(user.email);

  const profileEmail = document.getElementById("profileEmail");
  if (profileEmail) profileEmail.textContent = safe(user.email);

  // Обновляем телефон
  const phone = document.getElementById("phone");
  if (phone) phone.value = safe(user.phone);

  console.log("UI updated with new user data");
}

// Загрузка аватара (возвращает photoPath)
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
