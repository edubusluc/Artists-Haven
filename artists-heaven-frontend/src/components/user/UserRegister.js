import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Select from 'react-select';
import { useTranslation } from 'react-i18next';
import { Eye, EyeOff } from "lucide-react";

const inputFields = [
  { labelKey: 'userForm.label.firstName', name: 'firstName', type: 'text' },
  { labelKey: 'userForm.label.lastName', name: 'lastName', type: 'text' },
  { labelKey: 'userForm.label.email', name: 'email', type: 'email' },
  { labelKey: 'userForm.label.username', name: 'username', type: 'text' },
  { labelKey: 'userForm.label.password', name: 'password', type: 'password' }, // 👈 aquí va el toggle
  { labelKey: 'userForm.label.phone', name: 'phone', type: 'tel' },
  { labelKey: 'userForm.label.country', name: 'country' },
  { labelKey: 'userForm.label.postalCode', name: 'postalCode', type: 'text' },
  { labelKey: 'userForm.label.city', name: 'city', type: 'text' },
  { labelKey: 'userForm.label.address', name: 'address', type: 'text' },
];

const sections = [
  { titleKey: "userForm.title.personalData", fields: ['firstName', 'lastName'] },
  { titleKey: "userForm.title.contactData", fields: ['email', 'phone'] },
  { titleKey: "userForm.title.location", fields: ['city', 'postalCode', 'address'] },
  { titleKey: "userForm.title.credentials", fields: ['username', 'password'] }
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
  const [validationErrors, setValidationErrors] = useState({});
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setValidationErrors({});
  }, [currentLang]);

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
    let errors = {};
    setLoading(true);

    if (!user.firstName.trim()) errors.firstName = t('form.error.requiredFirstName');
    if (!user.lastName.trim()) errors.lastName = t('form.error.requiredLastName');
    if (!user.email.trim()) errors.email = t('form.error.requiredEmail');
    if (!user.username.trim()) errors.username = t('userForm.error.requiredUsername');
    if (!user.password.trim()) errors.password = t('form.error.requiredPassword');
    if (!user.postalCode.trim()) errors.postalCode = t('userForm.error.requiredPostalCode');
    if (!user.address.trim()) errors.address = t('userForm.error.requiredAddress');
    if (!user.city.trim()) errors.city = t('userForm.error.requiredCity');
    if (!user.phone.trim()) errors.phone = t('userForm.error.requiredPhone');
    if (!user.country.trim()) errors.country = t('userForm.error.requiredCountry');

    if (Object.keys(errors).length > 0) {
      setValidationErrors(errors);
      setLoading(false);
      return;
    }

    try {
      setValidationErrors({});
      const response = await fetch(`/api/users/register?lang=${currentLang}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(user),
      });

      const result = await response.json();
      const errorMessage = result.message;

      if (!response.ok) {
        throw new Error(errorMessage);
      }

      setUser(inputFields.reduce((acc, field) => ({ ...acc, [field.name]: '' }), {}));
      alert(t('userForm.success'));
      navigate('/auth/login');
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setLoading(false);
    }
  };

  const customSingleValue = ({ data }) => (
    <div className="flex items-center gap-2">
      <img
        src={data.flag}
        alt=""
        className="w-5 h-4 object-contain rounded-sm"
      />
      <span className="leading-none">{data.label}</span>
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
        <img
          src={data.flag}
          alt=""
          className="w-5 h-4 object-contain rounded-sm"
        />
        <span className="leading-none">{data.label}</span>
      </div>
    );
  };


  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-blue-100 py-12 px-4 mt-10">
      <div className="max-w-5xl mx-auto bg-white rounded-3xl shadow-xl p-10">
        <h2 className="text-4xl font-bold text-center text-blue-700 mb-10">
          {t('userForm.title.register')}
        </h2>

        <form onSubmit={handleSubmit} className="grid md:grid-cols-2 gap-8">
          {sections.map((section, index) => (
            <div key={index}>
              <h3 className="text-lg font-semibold text-blue-600 mb-4 border-b pb-2">
                {t(section.titleKey)}
              </h3>
              <div className="grid grid-cols-1 gap-5">
                {section.fields.map((name) => {
                  const { labelKey, type } = inputFields.find((f) => f.name === name);

                  if (name === "password") {
                    return (
                      <div key={name} className="relative">
                        <label
                          htmlFor={name}
                          className="block text-sm font-medium text-gray-700 mb-1"
                        >
                          {t(labelKey)} <span className="text-red-500">*</span>
                        </label>
                        <input
                          id={name}
                          name={name}
                          type={showPassword ? "text" : "password"}
                          value={user[name]}
                          onChange={handleChange}
                          className="w-full px-4 py-2 pr-12 border border-gray-300 rounded-xl shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-400 transition duration-200"
                        />
                        <button
                          type="button"
                          onClick={() => setShowPassword(!showPassword)}
                          className="absolute right-3 top-[38px] text-gray-500 hover:text-gray-700"
                        >
                          {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                        </button>
                        {validationErrors[name] && (
                          <p className="text-red-600 text-sm">{validationErrors[name]}</p>
                        )}
                      </div>
                    );
                  }

                  return (
                    <div key={name}>
                      <label
                        htmlFor={name}
                        className="block text-sm font-medium text-gray-700 mb-1"
                      >
                        {t(labelKey)}
                      </label>
                      <input
                        id={name}
                        name={name}
                        type={type}
                        value={user[name]}
                        onChange={handleChange}
                        className="w-full px-4 py-2 border border-gray-300 rounded-xl shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-400 transition duration-200"
                      />
                      {validationErrors[name] && (
                        <p className="text-red-600 text-sm">{validationErrors[name]}</p>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          ))}

          {/* Selector de país */}
          <div className="md:col-span-2">
            <h3 className="text-lg font-semibold text-blue-600 mb-4 border-b pb-2">
              {t('userForm.label.country')}
            </h3>
            <div>
              <label
                htmlFor="country"
                className="block text-sm font-medium text-gray-700 mb-1"
              >
                {t('userForm.label.country')} <span className="text-red-500">*</span>
              </label>
              <Select
                id="country"
                name="country"
                options={countries}
                value={countries.find((c) => c.value === user.country) || null}
                onChange={handleCountryChange}
                placeholder={t('userForm.placeholder.selectCountry')}
                isClearable
                components={{ Option: customOption, SingleValue: customSingleValue }}
                styles={{
                  control: (base, state) => ({
                    ...base,
                    minHeight: "44px", // igual que tus inputs
                    borderRadius: "0.75rem", // rounded-xl
                    borderColor: state.isFocused ? "#60a5fa" : "#d1d5db",
                    boxShadow: state.isFocused ? "0 0 0 2px #93c5fd" : "none", // focus:ring
                    "&:hover": { borderColor: "#60a5fa" },
                    display: "flex",
                    alignItems: "center",
                  }),
                  valueContainer: (base) => ({
                    ...base,
                    padding: "0 0.75rem",
                    display: "flex",
                    alignItems: "center",
                  }),
                  singleValue: (base) => ({
                    ...base,
                    display: "flex",
                    alignItems: "center",
                    gap: "0.5rem",
                  }),
                  input: (base) => ({
                    ...base,
                    margin: 0,
                    padding: 0,
                    minWidth: "1px", // evita que agrande el contenedor
                  }),
                }}
              />

              {validationErrors.country && (
                <p className="text-red-600 text-sm">{validationErrors.country}</p>
              )}
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
              disabled={loading}
              className={`w-full max-w-sm py-3 rounded-xl shadow-md font-semibold transition transform 
              ${loading ? 'bg-blue-300 text-gray-700 cursor-not-allowed' : 'bg-blue-600 hover:bg-blue-700 text-white hover:scale-105'}`}
            >
              {loading ? (
                <div className="flex items-center justify-center gap-2">
                  <div className="inline-block animate-spin border-t-2 border-b-2 border-black rounded-full w-5 h-5"></div>
                  {t('userForm.button.registering')}
                </div>
              ) : (
                t('userForm.button.register')
              )}
            </button>
          </div>
        </form>

        {/* Botón de volver */}
        <button
          onClick={() => navigate('/auth/login')}
          className="mt-8 text-sm text-blue-600 hover:underline block text-center"
        >
          {t('userForm.button.back')}
        </button>
      </div>
    </div>
  );
};

export default UserRegister;
