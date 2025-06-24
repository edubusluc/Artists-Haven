import { useState, useEffect } from 'react';
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

  useEffect(() => {
    fetch('https://restcountries.com/v3.1/independent?status=true')
      .then((res) => res.json())
      .then((data) => {
        const options = data
          .map((country) => ({
            value: country.translations?.spa?.common,
            label:
              currentLang === 'es'
                ? country.translations?.spa?.common
                : country.name.common,
            flag: country.flags.png,
          }))
          .sort((a, b) => a.label.localeCompare(b.label));
        setCountries(options);
      })
      .catch(() => setCountries([]));
  }, [currentLang]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setUser((prev) => ({ ...prev, [name]: value }));
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
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Error al registrar el usuario');
      }

      setUser(inputFields.reduce((acc, field) => ({ ...acc, [field.name]: '' }), {}));
      navigate('/auth/login');
    } catch (error) {
      setErrorMessage(error.message);
    }
  };

  const customSingleValue = ({ data }) => (
    <div className="flex items-center gap-2">
      <img src={data.flag} alt="" className="w-5 h-4 rounded-sm" />
      <span>{data.label}</span>
    </div>
  );

  const customOption = (props) => {
    const { data, innerRef, innerProps } = props;
    return (
      <div
        ref={innerRef}
        {...innerProps}
        className="flex items-center gap-2 px-3 py-2 hover:bg-gray-100 cursor-pointer"
      >
        <img src={data.flag} alt="" className="w-5 h-4 rounded-sm" />
        <span>{data.label}</span>
      </div>
    );
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-blue-100 py-12 px-4">
      <div className="max-w-5xl mx-auto bg-white rounded-3xl shadow-xl p-10">
        <h2 className="text-4xl font-bold text-center text-blue-700 mb-10">Registro de Usuario</h2>

        <form onSubmit={handleSubmit} className="grid md:grid-cols-2 gap-8">
          {[
            { title: "Datos personales", fields: ['firstName', 'lastName'] },
            { title: "Datos de contacto", fields: ['email', 'phone'] },
            { title: "Ubicación", fields: ['city', 'postalCode', 'address'] },
            { title: "Credenciales", fields: ['username', 'password'] }
          ].map((section, index) => (
            <div key={index}>
              <h3 className="text-lg font-semibold text-blue-600 mb-4 border-b pb-2">{section.title}</h3>
              <div className="grid grid-cols-1 gap-5">
                {section.fields.map((name) => {
                  const { label, type, required } = inputFields.find((f) => f.name === name);
                  return (
                    <div key={name}>
                      <label htmlFor={name} className="block text-sm font-medium text-gray-700 mb-1">
                        {label} {required && <span className="text-red-500">*</span>}
                      </label>
                      <input
                        id={name}
                        name={name}
                        type={type}
                        value={user[name]}
                        onChange={handleChange}
                        required={required}
                        className="w-full px-4 py-2 border border-gray-300 rounded-xl shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-400 transition duration-200"
                      />
                    </div>
                  );
                })}
              </div>
            </div>
          ))}

          {/* Selector de país */}
          <div className="md:col-span-2">
            <h3 className="text-lg font-semibold text-blue-600 mb-4 border-b pb-2">País</h3>
            <div>
              <label htmlFor="country" className="block text-sm font-medium text-gray-700 mb-1">
                País <span className="text-red-500">*</span>
              </label>
              <Select
                id="country"
                name="country"
                options={countries}
                value={countries.find((c) => c.value === user.country) || null}
                onChange={handleCountryChange}
                placeholder="Selecciona un país"
                isClearable
                components={{ Option: customOption, SingleValue: customSingleValue }}
                className="react-select-container"
                classNamePrefix="react-select"
                styles={{
                  control: (base) => ({
                    ...base,
                    borderRadius: '0.75rem',
                    padding: '2px',
                    borderColor: '#cbd5e0',
                    boxShadow: 'none',
                    '&:hover': { borderColor: '#93c5fd' },
                  }),
                  menu: (base) => ({
                    ...base,
                    borderRadius: '0.75rem',
                    marginTop: 4,
                  }),
                }}
              />
            </div>
          </div>

          {/* Mensaje de error */}
          {errorMessage && (
            <div className="md:col-span-2 text-center">
              <p className="text-red-600 text-sm font-medium mt-2">{errorMessage}</p>
            </div>
          )}

          {/* Botón de registro */}
          <div className="md:col-span-2 flex justify-center mt-4">
            <button
              type="submit"
              className="bg-blue-600 hover:bg-blue-700 text-white font-semibold w-full max-w-sm py-3 rounded-xl shadow-md transition transform hover:scale-105"
            >
              Registrar Usuario
            </button>
          </div>
        </form>

        {/* Botón de volver */}
        <button
          onClick={() => navigate('/auth/login')}
          className="mt-8 text-sm text-blue-600 hover:underline block text-center"
        >
          Volver al inicio
        </button>
      </div>
    </div>

  );
};

export default UserRegister;
