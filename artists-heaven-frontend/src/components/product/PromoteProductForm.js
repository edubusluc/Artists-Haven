import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useTranslation } from 'react-i18next';
import NonAuthorise from "../NonAuthorise";
import SessionExpired from "../SessionExpired";
import { checkTokenExpiration } from "../../utils/authUtils";

const PromoteProductForm = () => {
    const [discount, setDiscount] = useState(""); // Iniciar con una cadena vacía en lugar de 0
    const [authToken] = useState(localStorage.getItem("authToken"));
    const { id } = useParams();
    const navigate = useNavigate();
    const [errorMessage, setErrorMessage] = useState("");
    const { t, i18n } = useTranslation();
    const language = i18n.language;
    const [product, setProduct] = useState({});
    const rol = localStorage.getItem("role");

    useEffect(() => {
        fetch(`/api/product/details/${id}`)
            .then(res => res.json())
            .then(response => setProduct(response.data));
    }, [id]);

    useEffect(() => {
        setErrorMessage("")
    }, [language]);

    const discountedPrice = product.price - (product.price * (discount ? discount : 0) / 100); // Usar 0 si el descuento está vacío



    const handleSubmit = async (e) => {
        e.preventDefault();

        // Validar el descuento
        if (discount === "" || discount < 0 || discount > 100) {
            setErrorMessage(t('promoted.error.discount'));
            return;
        }
        setErrorMessage("");

        const promoData = {
            id: Number(id),
            discount: discount,
        };

        try {
            const response = await fetch(`/api/product/promote/${id}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    'Authorization': `Bearer ${authToken}`,
                },
                body: JSON.stringify(promoData),
            });

            const message = await response.text();
            if (response.ok) {
                alert("Promoción añadida con éxito!");
                setDiscount("");
                navigate(`/admin/products/store`);
            } else {
                alert("Error: " + message);
            }
        } catch (error) {
            console.error("Error:", error);
            alert("Ocurrió un error al añadir la promoción.");
        }
    };

    if (!rol || rol !== 'ADMIN') {
        return <NonAuthorise />;
    } else if (!checkTokenExpiration()) {
        return <SessionExpired />;
    }

    return (
        <div className="container mx-auto p-6 bg-white rounded-lg shadow-md">
            <h2 className="text-2xl font-bold mb-4 text-center text-black">{t('promoteProductForm.addDiscount')}</h2>
            <form onSubmit={handleSubmit} className="flex flex-col gap-6">
                {/* Descuento */}
                <div>
                    <label className="text-sm font-medium text-gray-700">{t('promoteProductForm.discount')} (%)</label>
                    <input
                        type="number"
                        placeholder="0"
                        value={discount}
                        onChange={(e) => setDiscount(e.target.value)}
                        className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-yellow-400"
                    />
                    {errorMessage && (
                        <p className="text-red-600 text-sm mt-2">{errorMessage}</p>
                    )}
                </div>

                {/* Desglose del precio */}
                <div className="mt-4">
                    <p className="text-sm text-gray-700">
                        {t('promoteProductForm.originalPrice')}: <span className="font-semibold">{product.price?.toFixed(2) || "0.00"}€</span>
                    </p>
                    <p className="text-sm text-gray-700">
                        {t('promoteProductForm.discount')}: <span className="font-semibold">{discount || 0}%</span>
                    </p>
                    <p className="text-sm text-gray-700">
                        {t('promoteProductForm.finalPrice')}: <span className="font-semibold">{discountedPrice ? discountedPrice.toFixed(2) : "0.00"}€</span>
                    </p>
                </div>

                {/* Botón Enviar */}
                <div className="flex justify-center">
                    <button
                        type="submit"
                        className="w-2/3 py-3 mt-4 bg-yellow-400 text-black font-semibold rounded-lg hover:bg-yellow-500 focus:outline-none"
                    >
                        {t('promoteProductForm.applyDiscount')}
                    </button>
                </div>
            </form>
        </div>
    );
};

export default PromoteProductForm;
