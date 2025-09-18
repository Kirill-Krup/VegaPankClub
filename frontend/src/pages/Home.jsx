import React, { useState } from 'react'
import '../styles/home.css'

export default function Home({ isAuthenticated = false, userLogin = 'Гость', userBalance = 0, userPhoto = '/Images/profile.jpg' }) {
  const [isMenuOpen, setIsMenuOpen] = useState(false)
  const defaultAvatar =
    'data:image/svg+xml;utf8,' +
    encodeURIComponent(`
      <svg xmlns="http://www.w3.org/2000/svg" width="128" height="128" viewBox="0 0 128 128">
        <defs>
          <linearGradient id="g" x1="0" x2="1" y1="0" y2="1">
            <stop offset="0%" stop-color="#e94560"/>
            <stop offset="100%" stop-color="#533483"/>
          </linearGradient>
        </defs>
        <circle cx="64" cy="64" r="62" fill="url(#g)"/>
        <circle cx="64" cy="48" r="22" fill="#ffffff" opacity="0.9"/>
        <path d="M18 112a46 30 0 0 1 92 0" fill="#ffffff" opacity="0.9"/>
      </svg>
    `)
  const handleAvatarError = (e) => {
    if (e.currentTarget.src !== defaultAvatar) {
      e.currentTarget.src = defaultAvatar
    }
  }

  return (
    <>
      <header className="main-header">
        <div className="container">
          <div className="header-left">
            <div className="logo">
              <i className="fas fa-gamepad"></i><span className="logo-accent">Кибер Арена</span>
            </div>
            <nav className={"main-nav" + (isMenuOpen ? " active" : "")}> 
              <ul>
                <li><a href="#about">О нас</a></li>
                <li><a href="#services">Услуги</a></li>
                <li><a href="#equipment">Оборудование</a></li>
                <li><a href="#pricing">Цены</a></li>
                <li><a href="#contact">Контакты</a></li>
              </ul>
            </nav>
          </div>

          <div className="header-right">
            <button
              aria-label="Открыть меню"
              className={"menu-toggle" + (isMenuOpen ? " active" : "")}
              onClick={() => setIsMenuOpen(v => !v)}
            >
              <i className="fas fa-bars"></i>
            </button>
            {isAuthenticated ? (
              <>
                <div className="profile-container" title="Профиль пользователя">
                  <div className="profile-text">
                    <div className="profile-name">{userLogin}</div>
                    <div className="profile-balance">{`${userBalance} BYN`}</div>
                  </div>
                  <a href="/profile">
                    <img src={userPhoto} onError={handleAvatarError} alt="Фото профиля" className="profile-photo" />
                  </a>
                </div>
                <form action="/logout" method="post" className="logout-form">
                  <button type="submit" className="logout-btn">Выйти</button>
                </form>
              </>
            ) : (
              <>
                <a className="btn btn-secondary" href="/login">Войти</a>
                <a className="btn btn-primary" href="/register">Зарегистрироваться</a>
              </>
            )}
          </div>
        </div>
      </header>

      <section className="hero">
        <div className="hero-overlay"></div>
        <div className="container hero-content">
          <h1>Погрузитесь в мир <span className="hero-accent">безграничных игр</span></h1>
          <p>Ваша новая игровая реальность начинается здесь. Мощные ПК, лучшие игры и идеальная атмосфера.</p>
          <div className="hero-buttons">
            <a href="#pricing" className="btn btn-primary btn-large">Наши тарифы</a>
            <a href="/booking" className="btn btn-secondary btn-large">Забронировать</a>
          </div>
        </div>
      </section>

      <section id="about" className="about-us section-padding">
        <div className="container">
          <h2 className="section-title">Добро пожаловать в <span className="accent">Кибер Арену</span></h2>
          <p className="section-subtitle">Мы создали идеальное место для геймеров и киберспортсменов.</p>
          <div className="about-grid">
            <div className="about-item">
              <i className="fas fa-desktop"></i>
              <h3>Топовое железо</h3>
              <p>Наши ПК оснащены последними видеокартами и процессорами для максимальной производительности.</p>
            </div>
            <div className="about-item">
              <i className="fas fa-headset"></i>
              <h3>Комфортные зоны</h3>
              <p>Удобные кресла, шумоизоляция и качественная периферия для полного погружения.</p>
            </div>
            <div className="about-item">
              <i className="fas fa-trophy"></i>
              <h3>Турниры и Ивенты</h3>
              <p>Регулярно проводим захватывающие турниры с ценными призами и весёлые игровые вечера.</p>
            </div>
          </div>
        </div>
      </section>

      <section id="services" className="services section-padding section-dark">
        <div className="container">
          <h2 className="section-title">Что мы <span className="accent">предлагаем</span></h2>
          <div className="service-grid">
            <div className="service-card">
              <i className="fas fa-gamepad"></i>
              <h3>Игровые сессии</h3>
              <p>Почасовая аренда мощных игровых станций для соло или командной игры.</p>
            </div>
            <div className="service-card">
              <i className="fas fa-users-cog"></i>
              <h3>Командные комнаты</h3>
              <p>Приватные комнаты для тренировок вашей команды с кастомизированными настройками.</p>
            </div>
            <div className="service-card">
              <i className="fas fa-beer-mug-empty"></i>
              <h3>Бар и Кафе</h3>
              <p>Широкий выбор напитков и закусок, чтобы пополнить энергию во время игры.</p>
            </div>
            <div className="service-card">
              <i className="fas fa-calendar-alt"></i>
              <h3>Аренда для мероприятий</h3>
              <p>Возможность аренды всего клуба для проведения частных вечеринок или корпоративов.</p>
            </div>
          </div>
        </div>
      </section>

      <section id="equipment" className="equipment section-padding">
        <div className="container">
          <h2 className="section-title">Наше <span className="accent">Оборудование</span></h2>
          <div className="equipment-layout">
            <div className="equipment-text">
              <p>Мы гордимся тем, что предоставляем нашим клиентам только лучшее. Каждая игровая станция в "Кибер Арене" тщательно продумана и оснащена компонентами от ведущих производителей.</p>
              <ul>
                <li><i className="fas fa-check-circle"></i> Процессоры Intel Core i9 / AMD Ryzen 9 последнего поколения</li>
                <li><i className="fas fa-check-circle"></i> Видеокарты NVIDIA GeForce RTX 40-й серии</li>
                <li><i className="fas fa-check-circle"></i> Быстрые SSD-накопители NVMe</li>
                <li><i className="fas fa-check-circle"></i> Мониторы с высокой частотой обновления (144 Гц - 240 Гц)</li>
                <li><i className="fas fa-check-circle"></i> Механические клавиатуры и высокоточные мыши</li>
              </ul>
              <a href="/booking" className="btn btn-primary">Забронировать ПК</a>
            </div>
            <div className="equipment-image">
              <img src="/Images/PC.jpg" alt="Мощный игровой ПК" />
            </div>
          </div>
        </div>
      </section>

      <section id="pricing" className="pricing section-padding section-dark">
        <div className="container">
          <h2 className="section-title">Наши <span className="accent">Тарифы</span></h2>
          <p className="section-subtitle">Выбирайте оптимальный вариант для вашего игрового опыта.</p>
          <div className="pricing-grid">
            <div className="pricing-card">
              <h3>Пакет "Час Игры"</h3>
              <div className="price"><span>4</span> BYN/час</div>
              <ul>
                <li><i className="fas fa-check"></i> Доступ к стандартным ПК</li>
                <li><i className="fas fa-check"></i> Базовая периферия</li>
                <li><i className="fas fa-check"></i> Широкий выбор игр</li>
                <li className="unavailable"><i className="fas fa-times"></i> Приватная комната</li>
                <li className="unavailable"><i className="fas fa-times"></i> Приоритетная поддержка</li>
              </ul>
              <a href="/booking" className="btn btn-primary">Выбрать</a>
            </div>
            <div className="pricing-card featured">
              <h3>Пакет "Кибер Атлет"</h3>
              <div className="price"><span>10</span> BYN/3 часа</div>
              <ul>
                <li><i className="fas fa-check"></i> Доступ к Premium ПК</li>
                <li><i className="fas fa-check"></i> Pro периферия</li>
                <li><i className="fas fa-check"></i> Приоритетная бронь</li>
                <li><i className="fas fa-check"></i> Скидки в баре</li>
                <li className="unavailable"><i className="fas fa-times"></i> Приватная комната</li>
              </ul>
              <a href="/booking" className="btn btn-primary">Выбрать</a>
            </div>
            <div className="pricing-card">
              <h3>Пакет "Командная Битва"</h3>
              <div className="price"><span>40</span> BYN/3 часа</div>
              <ul>
                <li><i className="fas fa-check"></i> Приватная комната (5 ПК)</li>
                <li><i className="fas fa-check"></i> Premium ПК и периферия</li>
                <li><i className="fas fa-check"></i> Персональный менеджер</li>
                <li><i className="fas fa-check"></i> 10% скидка на еду/напитки</li>
                <li><i className="fas fa-check"></i> Приоритет в турнирах</li>
              </ul>
              <a href="/booking" className="btn btn-primary">Выбрать</a>
            </div>
          </div>
        </div>
      </section>

      <section className="cta-banner section-padding">
        <div className="container">
          <h2>Готовы к новому уровню игры?</h2>
          <p>Не упустите шанс стать частью нашего игрового сообщества!</p>
          <a href="/booking" className="btn btn-primary btn-large">Забронировать сейчас</a>
        </div>
      </section>

      <section id="contact" className="contact section-padding section-dark">
        <div className="container">
          <h2 className="section-title">Свяжитесь <span className="accent">с нами</span></h2>
          <div className="contact-grid">
            <div className="contact-info">
              <h3>Наши Контакты</h3>
              <p><i className="fas fa-map-marker-alt"></i> г. Минск, ул. Слободская, 15</p>
              <p><i className="fas fa-phone"></i> +375 44 579-75-X15</p>
              <p><i className="fas fa-envelope"></i> info@cyberarena.com</p>
              <div className="social-icons">
                <a href="https://vk.com/dinooozaaauric"><i className="fab fa-vk"></i></a>
                <a href="#" target="_blank" rel="noreferrer"><i className="fab fa-telegram-plane"></i></a>
                <a href="https://www.instagram.com/malinovvvvskaya"><i className="fab fa-instagram"></i></a>
              </div>
            </div>
            <div className="contact-form">
              <h3>Отправьте нам сообщение</h3>
              <form action="#" method="POST" onSubmit={(e) => e.preventDefault()}>
                <div className="form-group">
                  <input type="text" id="name" name="name" placeholder="Ваше Имя" required />
                </div>
                <div className="form-group">
                  <input type="email" id="email" name="email" placeholder="Ваш Email" required />
                </div>
                <div className="form-group">
                  <textarea id="message" name="message" rows={5} placeholder="Ваше сообщение" required></textarea>
                </div>
                <button type="submit" className="btn btn-primary">Отправить</button>
              </form>
            </div>
          </div>
        </div>
      </section>

      <footer className="main-footer">
        <div className="container">
          <p>&copy; 2025 ПК Клуб "Кибер Арена". Все права защищены.</p>
        </div>
      </footer>
    </>
  )
}


