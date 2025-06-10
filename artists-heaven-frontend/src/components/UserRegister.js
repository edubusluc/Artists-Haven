import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Select from 'react-select';
import { useTranslation } from 'react-i18next';

const inputFields = [
  { label: 'Nombre', name: 'firstName', type: 'text', required: true },
  { label: 'Apellido', name: 'lastName', type: 'text', required: true },
  { label: 'Correo Electrónico', name: 'email', type: 'email', required: true },
  { label: 'Usuario', name: 'username', type: 'text', required: true },
  { label: 'Contraseña', name: 'password', type: 'password', required: true },
  { label: 'Teléfono', name: 'phone', type: 'tel', required: true },
  { label: 'País', name: 'country', required: true },
  { label: 'Código Postal', name: 'postalCode', type: 'text', required: true },
  { label: 'Ciudad', name: 'city', type: 'text', required: true },
  { label: 'Dirección', name: 'address', type: 'text', required: true },
];

const UserRegister = () => {
  const [user, setUser] = useState(() =>
    inputFields.reduce((acc, field) => ({ ...acc, [field.name]: '' }), {})
  );
  const [errorMessage, setErrorMessage] = useState('');
  const [countries, setCountries] = useState([]);
  const navigate = useNavigate();
  const { t, i18n } = useTranslation();
  const currentLang = i18n.language;
  console.log(currentLang);


  useEffect(() => {
    fetch('https://restcountries.com/v3.1/independent?status=true')
      .then((res) => res.json())
      .then((data) => {
        const options = data
          .map((country) => ({
            value: country.translations?.spa?.common,
            label: currentLang === 'es'
              ? country.translations?.spa?.common: country.name.common,
            flag: country.flags.png,
          }))
          .sort((a, b) => a.label.localeCompare(b.label));
        setCountries(options);
      })
      .catch(() => setCountries([]));
  }, [currentLang]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setUser((prevUser) => ({ ...prevUser, [name]: value }));
  };



  const handleCountryChange = (selectedOption) => {
    setUser((prevUser) => ({
      ...prevUser,
      country: selectedOption ? selectedOption.value : '',
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');

    for (const field of inputFields) {
      if (field.required && !user[field.name].trim()) {
        setErrorMessage(`Por favor, completa el campo "${field.label}".`);
        return;
      }
    }

    try {
      const response = await fetch('/api/users/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(user),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Error al registrar el usuario');
      }

      await response.json();

      setUser(inputFields.reduce((acc, field) => ({ ...acc, [field.name]: '' }), {}));
      navigate('/users');
    } catch (error) {
      setErrorMessage(error.message);
    }
  };

  // Personalizar cómo se renderizan las opciones del select (con bandera y texto)
  const customSingleValue = ({ data }) => (
    <div style={{ display: 'flex', alignItems: 'center' }}>
      <img
        src={data.flag}
        alt={`Bandera de ${data.label}`}
        style={{ width: 24, height: 16, marginRight: 8 }}
      />
      <span>{data.label}</span>
    </div>
  );

  const customOption = (props) => {
    const { data, innerRef, innerProps } = props;
    return (
      <div
        ref={innerRef}
        {...innerProps}
        style={{ display: 'flex', alignItems: 'center', padding: '8px', cursor: 'pointer' }}
      >
        <img
          src={data.flag}
          alt={`Bandera de ${data.label}`}
          style={{ width: 24, height: 16, marginRight: 8 }}
        />
        <span>{data.label}</span>
      </div>
    );
  };

  return (
    <div>
      <h2>Registro de Usuario</h2>
      <form onSubmit={handleSubmit} noValidate>
        {inputFields.map(({ label, name, type, required }) => (
          <div key={name} style={{ marginBottom: '1rem' }}>
            <label htmlFor={name} style={{ display: 'block', fontWeight: 'bold' }}>
              {label} {required && '*'}
            </label>

            {name === 'country' ? (
              <Select
                id={name}
                name={name}
                options={countries}
                value={countries.find((c) => c.value === user.country) || null}
                onChange={handleCountryChange}
                placeholder="Seleccione un país"
                isClearable
                components={{ Option: customOption, SingleValue: customSingleValue }}
              />
            ) : (
              <input
                id={name}
                type={type}
                name={name}
                value={user[name]}
                onChange={handleChange}
                required={required}
                style={{ width: '100%', padding: '0.5rem', fontSize: '1rem' }}
              />
            )}
          </div>
        ))}

        <button type="submit" style={{ padding: '0.5rem 1rem' }}>
          Registrar Usuario
        </button>
      </form>

      <button
        onClick={() => navigate('/')}
        className="btn btn-primary"
        style={{ marginTop: '1rem' }}
      >
        Volver al inicio
      </button>

      {errorMessage && (
        <p style={{ color: 'red', marginTop: '1rem' }} role="alert">
          {errorMessage}
        </p>
      )}
    </div>
  );
};

export default UserRegister;
