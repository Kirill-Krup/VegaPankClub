import './App.css'
import { Routes, Route, Link } from 'react-router-dom'
import Home from './pages/Home.jsx'
import Booking from './pages/Booking.jsx'
import Profile from './pages/Profile.jsx'
import EditProfile from './pages/EditProfile.jsx'
import Replenishment from './pages/Replenishment.jsx'

function App() {
  return (
    <>
      <Routes>
        <Route path="/" element={<Home isAuthenticated={false} />} />
        <Route path="/home" element={<Home isAuthenticated={false} />} />
        <Route path="/booking" element={<Booking />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/profile/edit" element={<EditProfile />} />
        <Route path="/profile/replenishment" element={<Replenishment />} />
        <Route path="*" element={<Home isAuthenticated={false} />} />
      </Routes>
    </>
  )
}

export default App
