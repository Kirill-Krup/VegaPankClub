import React from 'react'
import '../styles/editProf.css'

export default function EditProfile() {
  return (
    <>
      <header className="main-header">
        <div className="container">
          <div className="logo">
            <a href="/profile">
              <i className="fas fa-gamepad"></i>
              <span className="logo-accent">Кибер Арена</span>
            </a>
          </div>
          <div className="main-nav-and-profile">
            <nav className="main-nav">
              <ul>
                <li><a href="/">Главная</a></li>
                <li><a href="/arena">Арена</a></li>
                <li><a href="/pricing">Тарифы</a></li>
                <li><a href="/contacts">Контакты</a></li>
              </ul>
            </nav>
            <div className="profile-mini">
              <a className="btn-outline" href="/profile"><i className="fa-regular fa-user"></i> Профиль</a>
              <form action="/logout" method="post" className="logout-inline">
                <button type="submit" className="btn-outline danger"><i className="fa-solid fa-right-from-bracket"></i> Выход</button>
              </form>
            </div>
          </div>
        </div>
      </header>

      <main className="profile-edit">
        <div className="container">
          <div className="form-card">
            <div className="form-card__head">
              <h1><i className="fa-regular fa-pen-to-square"></i> Редактирование профиля</h1>
              <p className="muted">Измени логин, имя и телефон. Поля с <span className="req">*</span> обязательны.</p>
            </div>

            <form className="edit-form" autoComplete="on" onSubmit={(e)=>e.preventDefault()}>
              <div className="form-grid">
                <div className="form-group">
                  <label htmlFor="username">Логин <span className="req">*</span></label>
                  <div className="input-wrap">
                    <i className="fa-regular fa-at input-icon"></i>
                    <input type="text" id="username" name="username" placeholder="Ваш логин" minLength={3} maxLength={32} required />
                  </div>
                  <small className="hint">3–32 символа, латиница/цифры/._-</small>
                </div>

                <div className="form-group">
                  <label htmlFor="fullName">Имя <span className="req">*</span></label>
                  <div className="input-wrap">
                    <i className="fa-regular fa-user input-icon"></i>
                    <input type="text" id="fullName" name="fullName" placeholder="Ваше имя" minLength={2} maxLength={64} required />
                  </div>
                  <small className="hint">Можно указывать имя и фамилию.</small>
                </div>

                <div className="form-group">
                  <label htmlFor="phone">Телефон <span className="req">*</span></label>
                  <div className="input-wrap">
                    <i className="fa-solid fa-phone input-icon"></i>
                    <input type="tel" id="phone" name="phone" placeholder="+375 ** ***-**-**" inputMode="tel" pattern="^\+?[0-9 ()\-]{7,20}$" required />
                  </div>
                  <small className="hint">Формат: +375 ** ***-**-**.</small>
                </div>
              </div>

              <div className="form-actions">
                <button type="submit" className="btn-primary"><i className="fa-regular fa-floppy-disk"></i> Сохранить изменения</button>
                <a href="/profile" className="btn-ghost"><i className="fa-regular fa-circle-left"></i> Отмена</a>
              </div>
            </form>
          </div>
        </div>
      </main>
    </>
  )
}


