import React, { Suspense, useState, useEffect } from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { GoogleOAuthProvider } from '@react-oauth/google';

// Contexts
import { CartProvider } from './context/CartContext';

// Styles and i18n
import './style.css';
import './i18n/i18n';

// Components - General
import Header from './components/Header';
import HomePage from './components/HomePage';
import FAQ from './components/FAQ';
import Success from './components/Success';

// Components - User
import UserList from './components/UserList';
import UserRegister from './components/UserRegister';
import UserLogin from './components/UserLogin';
import UserProfile from './components/UserProfile';
import EditProfile from './components/EditProfile';
import EmailForm from './components/EmailForm';

// Components - Artists
import ArtistsRegister from './components/ArtistsRegister';
import ArtistDashboard from './components/artist/ArtistDashboard';
import ArtistHeader from './components/artist/ArtistHeader';


// Components - Verification
import VerificationForm from './components/artist/VerificationForm';
import VerificationList from './components/VerificationList';

// Components - Products
import CreateProductForm from './components/product/CreateProductForm';
import ProductsList from './components/product/ProductsList';
import EditProduct from './components/product/EditProduct';
import ProductDetails from './components/product/ProductDetails';
import PromoteProductForm from './components/product/PromoteProductForm';
import CreateRatingForm from './components/CreateRatingForm';

// Components - Events
import CreateEventForm from './components/Event/CreateEventForm';
import AllEvents from './components/AllEvents';
import EditMyEvent from './components/EditMyEvent';
import AllMyEvents from './components/Event/AllMyEvents';

// Components - Orders
import MyOrders from './components/MyOrders';


// Components - Admin
import AdminHeader from './components/admin/AdminHeader';
import AdminDashboard from './components/admin/AdminDashboard';
import AdminProductList from './components/admin/AdminProductList';
import AdminClient from './components/admin/AdmiClients';
import AdminOrder from './components/admin/AdminOrder';
import AdminOrderDetails from './components/admin/AdminOrderDetails';

const AppRoutes = ({ role }) => (
  <Routes>
    <Route
      path="/"
      element={
        role === 'ADMIN' ? (
          <AdminDashboard />
        ) : role === 'ARTIST' ? (
          <ArtistDashboard />
        ) : (
          <HomePage />
        )
      }
    />
    <Route path="/users" element={<UserList />} />
    <Route path="/user/register" element={<UserRegister />} />
    <Route path="/artists/register" element={<ArtistsRegister />} />
    <Route path="/auth/login" element={<UserLogin />} />
    <Route path="/users/profile" element={<UserProfile />} />
    <Route path="/profile/edit" element={<EditProfile />} />
    <Route path="/email" element={<EmailForm />} />
    <Route path="/verification" element={<VerificationForm />} />
    <Route path="/admin/verification/pending" element={<VerificationList />} />
    <Route path="/product/new" element={<CreateProductForm />} />
    <Route path="/product/all" element={<ProductsList />} />
    <Route path="/product/edit/:id" element={<EditProduct />} />
    <Route path="/product/details/:id" element={<ProductDetails />} />
    <Route path="/product/newReview/:productId" element={<CreateRatingForm />} />
    <Route path="/product/promote/:id" element={<PromoteProductForm />} />
    <Route path="/event/new" element={<CreateEventForm />} />
    <Route path="/event/all-my-events" element={<AllMyEvents />} />
    <Route path="/event/all-events" element={<AllEvents />} />
    <Route path="/event/edit/:id" element={<EditMyEvent />} />
    <Route path="/orders/myOrders" element={<MyOrders />} />
    <Route path="/FAQ" element={<FAQ />} />
    <Route path="/success" element={<Success />} />
    <Route path="/admin/products/store" element={<AdminProductList />} />
    <Route path="/admin/dashboard" element={<AdminDashboard />} />
    <Route path="/admin/clients" element={<AdminClient />} />
    <Route path="/admin/orders" element={<AdminOrder />} />
    <Route path="/admin/orderDetails/:id" element={<AdminOrderDetails />} />
    <Route path="/artist/dashboard" element={<ArtistDashboard />} />
  </Routes>
);

const App = () => {
  const [role, setRole] = useState(localStorage.getItem("role"));

  useEffect(() => {
    const handleStorageChange = () => {
      setRole(localStorage.getItem("role"));
    };
    window.addEventListener("storage", handleStorageChange);
    return () => window.removeEventListener("storage", handleStorageChange);
  }, []);

  return (
    <GoogleOAuthProvider clientId="1048927197271-g7tartu6gacs0jv8fgoa5braq8b2ck7p.apps.googleusercontent.com">
      <CartProvider>
        <Suspense fallback={<div className="text-white">Cargando traducciones...</div>}>
          <Router>
            {role === 'ADMIN' ? (
              <AdminHeader />
            ) : role === 'ARTIST' ? (
              <ArtistHeader />
            ) : (
              <Header />
            )}
            <AppRoutes role={role} />
          </Router>
        </Suspense>
      </CartProvider>
    </GoogleOAuthProvider>
  );
};

export default App;
