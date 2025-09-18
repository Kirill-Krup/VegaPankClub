import React from 'react'
import '../styles/booking.css'

export default function Booking() {
  return (
    <>
      <header className="main-header">
        <div className="container">
          <div className="logo">
            <a href="/">
              <i className="fas fa-gamepad"></i>
              <span className="logo-accent">Кибер Арена</span>
            </a>
          </div>
          <div className="main-nav-and-profile">
            <nav className="main-nav">
              <ul>
                <li><a href="/home">Главная</a></li>
                <li><a href="/home#arena">Арена</a></li>
                <li><a href="/home#pricing">Тарифы</a></li>
                <li><a href="/home#contacts">Контакты</a></li>
              </ul>
            </nav>
          </div>
        </div>
      </header>

      <main className="booking">
        <div className="container">
          <div className="form-card">
            <div className="form-card__head">
              <h1><i className="fa-solid fa-chair"></i> Забронировать место</h1>
              <p className="muted">Выберите день, время, ПК и тариф</p>
            </div>

            <form className="booking-form" onSubmit={(e)=>e.preventDefault()}>
              <input type="hidden" id="selectedPc" name="selectedPc" />
              <input type="hidden" id="selectedPlan" name="selectedPlan" />

              <section className="step">
                <h2><i className="fa-regular fa-calendar"></i> День и время</h2>
                <div className="form-grid">
                  <div className="form-group">
                    <label htmlFor="day">Дата начала <span className="req">*</span></label>
                    <input type="date" id="day" className="small-input" required />
                  </div>
                  <div className="form-group">
                    <label htmlFor="time">Время начала <span className="req">*</span></label>
                    <input type="time" id="time" className="small-input" step="1800" required />
                  </div>
                  <div className="form-group">
                    <label htmlFor="endDay">Дата окончания <span className="req">*</span></label>
                    <input type="date" id="endDay" className="small-input" required />
                  </div>
                  <div className="form-group">
                    <label htmlFor="endTime">Время окончания <span className="req">*</span></label>
                    <input type="time" id="endTime" className="small-input" step="1800" required />
                  </div>
                </div>
              </section>

              <section className="step">
                <h2><i className="fa-solid fa-desktop"></i> Выберите место</h2>
                <div className="floor-segment">
                  <div className="floor-segment__track" role="tablist" aria-label="Этаж">
                    <button type="button" className="floor-segment__btn">Этаж 0</button>
                    <button type="button" className="floor-segment__btn is-active">Этаж 1</button>
                    <button type="button" className="floor-segment__btn">Этаж 2</button>
                    <span className="floor-segment__indicator" aria-hidden="true"></span>
                  </div>
                </div>

                <div className="floor active">
                  <div className="layout-container">
                    <div className="left-side">
                      <div className="left-top">
                        <div className="pc-row">
                          <button type="button" className="pc-seat">ПК 1</button>
                          <button type="button" className="pc-seat">ПК 2</button>
                          <button type="button" className="pc-seat">ПК 3</button>
                          <button type="button" className="pc-seat">ПК 4</button>
                          <button type="button" className="pc-seat">ПК 5</button>
                          <button type="button" className="pc-seat">ПК 6</button>
                        </div>
                      </div>
                      <div className="left-bottom">
                        <div className="pc-column-row">
                          <button type="button" className="pc-seat">ПК 7</button>
                          <button type="button" className="pc-seat">ПК 8</button>
                          <button type="button" className="pc-seat">ПК 9</button>
                          <button type="button" className="pc-seat">ПК 10</button>
                        </div>
                      </div>
                    </div>
                    <div className="right-side">
                      <div className="enter">Вход</div>
                    </div>
                  </div>
                </div>
              </section>

              <section className="step">
                <div id="pricing">
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
                        </ul>
                        <a className="btn-primary">Выбрать</a>
                      </div>
                      <div className="pricing-card featured">
                        <h3>Пакет "Кибер Атлет"</h3>
                        <div className="price"><span>10</span> BYN/3 часа</div>
                        <a className="btn-primary">Выбрать</a>
                      </div>
                    </div>
                  </div>
                </div>
              </section>

              <div className="form-actions">
                <button type="submit" className="btn-primary"><i className="fa-solid fa-check"></i> Забронировать</button>
                <a href="/" className="btn-ghost"><i className="fa-regular fa-circle-left"></i> Назад</a>
              </div>
            </form>
          </div>
        </div>
      </main>
    </>
  )
}


