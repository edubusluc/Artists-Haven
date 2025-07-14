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
import UserRegister from './components/user/UserRegister';
import UserLogin from './components/user/UserLogin';
import UserProfile from './components/user/UserProfile';
import EditProfile from './components/user/EditProfile';
import EmailForm from './components/EmailForm';
import MyOrders from './components/user/MyOrders';
import MySpace from './components/user/MySpace';

// Components - Artists
import ArtistsRegister from './components/artist/ArtistsRegister';
import ArtistDashboard from './components/artist/ArtistDashboard';
import ArtistHeader from './components/artist/ArtistHeader';
import ArtistView from './components/ArtistView';

// Components - Orders
import OrderAnonymous from './components/order/OrderAnonymous';


// Components - Verification
import VerificationForm from './components/artist/VerificationForm';
import VerificationList from './components/VerificationList';

// Components - Products
import CreateProductForm from './components/product/CreateProductForm';
import ProductsList from './components/product/ProductsList';
import EditProduct from './components/product/EditProduct';
import ProductDetails from './components/product/ProductDetails';
import PromoteProductForm from './components/product/PromoteProductForm';
import TshirtList from './components/product/TshirtList';
import PantsList from './components/product/PantsList';
import AccessoriesList from './components/product/AccessoriesList';

// Components - Events
import CreateEventForm from './components/Event/CreateEventForm';
import AllEvents from './components/AllEvents';
import EditMyEvent from './components/Event/EditMyEvent';
import AllMyEvents from './components/Event/AllMyEvents';

// Components - Admin
import AdminHeader from './components/admin/AdminHeader';
import AdminDashboard from './components/admin/AdminDashboard';
import AdminProductList from './components/admin/AdminProductList';
import AdminClient from './components/admin/AdminClients';
import AdminOrder from './components/admin/AdminOrder';
import AdminOrderDetails from './components/admin/AdminOrderDetails';


import Chatbot from './components/ChatBot';
import ScrollToTop from './components/ScrollToTop';


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
    <Route path="/user/register" element={<UserRegister />} />
    <Route path="/artists/register" element={<ArtistsRegister />} />
    <Route path="/auth/login" element={<UserLogin />} />
    <Route path="/users/profile" element={<UserProfile />} />
    <Route path="/users/mySpace" element={<MySpace />} />
    <Route path="/profile/edit" element={<EditProfile />} />
    <Route path="/email" element={<EmailForm />} />
    <Route path="/verification" element={<VerificationForm />} />
    <Route path="/admin/verification/pending" element={<VerificationList />} />
    <Route path="/product/new" element={<CreateProductForm />} />
    <Route path="/shop" element={<ProductsList />} />
    <Route path="/product/edit/:id" element={<EditProduct />} />
    <Route path="/product/details/:id" element={<ProductDetails />} />
    <Route path="/product/promote/:id" element={<PromoteProductForm />} />
    <Route path="/event/new" element={<CreateEventForm />} />
    <Route path="/event/all-my-events" element={<AllMyEvents />} />
    <Route path="/event/all-events" element={<AllEvents />} />
    <Route path="/event/edit/:id" element={<EditMyEvent />} />
    <Route path="/orders/myOrders" element={<MyOrders />} />
    <Route path="/orders/by-identifier" element={<OrderAnonymous />} />
    <Route path="/FAQ" element={<FAQ />} />
    <Route path="/success" element={<Success />} />
    <Route path="/admin/products/store" element={<AdminProductList />} />
    <Route path="/admin/dashboard" element={<AdminDashboard />} />
    <Route path="/admin/clients" element={<AdminClient />} />
    <Route path="/admin/orders" element={<AdminOrder />} />
    <Route path="/admin/orderDetails/:id" element={<AdminOrderDetails />} />
    <Route path="/artist/dashboard" element={<ArtistDashboard />} />
    <Route path="/artist/:name" element={<ArtistView />} />
    <Route path="/shop/camisetas" element={<TshirtList />} />
    <Route path="/shop/pantalones" element={<PantsList />} />
    <Route path="/shop/accesorios" element={<AccessoriesList />} />
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

  const getHeader = () => {
    switch (role) {
      case 'ADMIN':
        return <AdminHeader />;
      case 'ARTIST':
        return <ArtistHeader />;
      default:
        return <Header />;
    }
  };

  const backgroundClass = role === 'ARTIST' || role === 'ADMIN' ? 'from-gray-300 to-white' : 'bg-white';

  return (
    <GoogleOAuthProvider clientId="1048927197271-g7tartu6gacs0jv8fgoa5braq8b2ck7p.apps.googleusercontent.com">
      <CartProvider>
        <Suspense fallback={<div className="text-white">Cargando traducciones...</div>}>
          <Router>
            <ScrollToTop />
            {getHeader()}
            <AppRoutes role={role} />
            <Chatbot />
          </Router>
        </Suspense>
      </CartProvider>
    </GoogleOAuthProvider>
  );
};

export default App;
