import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { GoogleOAuthProvider } from '@react-oauth/google';
import Header from './components/Header';
import HomePage from './components/HomePage';
import UserList from './components/UserList';
import UserRegister from './components/UserRegister';
import ArtistsRegister from './components/ArtistsRegister';
import UserLogin from './components/UserLogin';
import UserProfile from './components/UserProfile';
import EditProfile from './components/EditProfile';
import EmailForm from './components/EmailForm';
import VerificationForm from './components/VerificationForm';
import VerificationList from './components/VerificationList';
import CreateProductForm from './components/CreateProductForm';
import ProductsList from './components/ProductsList';
import EditProduct from './components/EditProduct';
import CreateEventForm from './components/CreateEventForm';
import AllMyEvents from './components/AllMyEvents';
import FAQ from './components/FAQ';
import EditMyEvent from './components/EditMyEvent';
import ProductDetails from './components/ProductDetails';
import { CartProvider } from './context/CartContext';
import './style.css';
import AllEvents from './components/AllEvents';
import CreateRatingForm from './components/CreateRatingForm';
import MyOrders from './components/MyOrders';
import PromoteProductForm from './components/PromoteProductForm';

const App = () => (
  <GoogleOAuthProvider clientId="1048927197271-g7tartu6gacs0jv8fgoa5braq8b2ck7p.apps.googleusercontent.com">
    <CartProvider>
      <Router>
        <Header />
        <Routes>
          <Route path="/" element={<HomePage />} />
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
          <Route path="/event/new" element={<CreateEventForm />} />
          <Route path="/event/all-my-events" element={<AllMyEvents />} />
          <Route path="/event/allEvents" element={<AllEvents />} />
          <Route path="/FAQ" element={<FAQ />} />
          <Route path="/event/edit/:id" element={<EditMyEvent />} />
          <Route path="/product/details/:id" element={<ProductDetails />} />
          <Route path="/event/all-events" element={<AllEvents />} />
          <Route path="/product/newReview/:productId" element={<CreateRatingForm />} />
          <Route path="/orders/myOrders" element={<MyOrders/>} />
          <Route path="/product/promote/:id" element={<PromoteProductForm/>} />
        </Routes>
      </Router>
    </CartProvider>
  </GoogleOAuthProvider>
);

export default App;
