import React, { useState } from 'react'
import '../styles/replenishment.css'

export default function Replenishment({ wallet = 0 }) {
  const [method, setMethod] = useState('')
  const [amount, setAmount] = useState('')
  return (
    <>
      <header className="main-header">
        <div className="container">
          <div className="logo">
            <a href="/profile">
              <i className="fas fa-gamepad"></i><span className="logo-accent">Кибер Арена</span>
            </a>
          </div>
        </div>
      </header>

      <main className="wallet-container">
        <div className="wallet-card">
          <h2>Пополнение кошелька</h2>
          <div className="current-balance">Ваш баланс: <span>{wallet} BYN</span></div>

          <form className="wallet-form" onSubmit={(e)=>e.preventDefault()}>
            <label htmlFor="amount">Сумма пополнения (BYN)</label>
            <input id="amount" type="number" min="1" step="0.01" value={amount} onChange={e=>setAmount(e.target.value)} required />

            <label htmlFor="payment-method">Способ оплаты</label>
            <select id="payment-method" value={method} onChange={e=>setMethod(e.target.value)} required>
              <option value="">Выберите метод</option>
              <option value="card">Банковская карта</option>
              <option value="webpay">WebPay</option>
              <option value="paypal">PayPal</option>
            </select>

            {method === 'card' && (
              <div id="card-number-field">
                <label htmlFor="card-number">Номер карты</label>
                <input id="card-number" type="text" placeholder="0000 0000 0000 0000" maxLength={19}
                  onInput={(e)=>{ let v=e.currentTarget.value.replace(/\D/g,''); v=v.replace(/(\d{4})(?=\d)/g,'$1 '); e.currentTarget.value=v; }} />
              </div>
            )}

            <button type="submit" className="top-up-btn">Пополнить</button>
          </form>
        </div>
      </main>
    </>
  )
}


