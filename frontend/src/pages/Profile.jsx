import React from 'react'
import '../styles/profile.css'

export default function Profile({ user = { login: 'User', fullName: 'Имя Фамилия', phone: '+375 00 000-00-00', email: 'user@example.com', wallet: 25, photo: '/Images/profile.jpg' } }) {
  return (
    <>
      <header className="main-header">
        <div className="container">
          <div className="logo">
            <a href="/home">
              <i className="fas fa-gamepad"></i><span className="logo-accent">Кибер Арена</span>
            </a>
          </div>
          <div className="main-nav-and-profile">
            <nav className="main-nav">
              <ul>
                <li><a href="/home#about">О нас</a></li>
                <li><a href="/home#services">Услуги</a></li>
                <li><a href="/home#equipment">Оборудование</a></li>
                <li><a href="/home#pricing">Цены</a></li>
                <li><a href="/home#contact">Контакты</a></li>
              </ul>
            </nav>
          </div>
          <div className="menu-toggle">
            <i className="fas fa-bars"></i>
          </div>
        </div>
      </header>

      <main className="profile-wrapper">
        <section className="profile-page">
          <div className="profile-photo-wrapper">
            <img className="profile-photo" src={user.photo} alt="Фото профиля" />
          </div>
          <div className="profile-title">
            <h1 className="profile-name">{user.login}</h1>
            <div className="profile-balance">Баланс: {user.wallet} BYN</div>
          </div>

          <div className="profile-info">
            <div className="profile-info-item">
              <label><i className="fa-solid fa-user"></i> Имя пользователя</label>
              <div className="value">{user.fullName}</div>
            </div>
            <div className="profile-info-item">
              <label><i className="fa-solid fa-phone"></i> Номер телефона</label>
              <div className="value">{user.phone}</div>
            </div>
            <div className="profile-info-item">
              <label><i className="fa-solid fa-envelope"></i> Почта</label>
              <div className="value">{user.email}</div>
            </div>
          </div>

          <div className="profile-actions">
            <a className="btn-edit" href="/profile/edit">
              <i className="fa-solid fa-pen"></i> Редактировать профиль
            </a>
            <a className="btn-topup" href="/profile/replenishment">
              <i className="fa-solid fa-wallet"></i> Пополнить кошелёк
            </a>
          </div>
        </section>
      </main>
    </>
  )
}


