import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useLocation } from 'react-router-dom';

const PromoteProductForm = () => {
    const [discount, setDiscount] = useState(""); // Iniciar con una cadena vacía en lugar de 0
    const [authToken] = useState(localStorage.getItem("authToken"));
    const { id } = useParams();
    const navigate = useNavigate();
    const [errorMessage, setErrorMessage] = useState("");
    const location = useLocation();
    const queryParams = new URLSearchParams(location.search);  

    const price = parseFloat(queryParams.get('price') || 0); // Obtener el precio original del producto

    // Calcular el precio final con el descuento
    const discountedPrice = price - (price * (discount ? discount : 0) / 100); // Usar 0 si el descuento está vacío

    const handleSubmit = async (e) => {
        e.preventDefault();

        // Validar el descuento
        if (discount === "" || discount < 0 || discount > 100) {
            setErrorMessage("El descuento debe estar entre 0 y 100.");
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

    return (
        <div className="container mx-auto p-6 bg-white rounded-lg shadow-md">
            <h2 className="text-2xl font-bold mb-4 text-center text-black">Añadir Descuento al Producto</h2>
            <form onSubmit={handleSubmit} className="flex flex-col gap-6">
                {/* Descuento */}
                <div>
                    <label className="text-sm font-medium text-gray-700">Descuento (%)</label>
                    <input
                        type="number"
                        placeholder="0"
                        min="0"
                        max="100"
                        value={discount}
                        onChange={(e) => setDiscount(e.target.value)} // Actualiza con el valor ingresado
                        className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-yellow-400"
                    />
                    {errorMessage && (
                        <p className="text-red-600 text-sm mt-2">{errorMessage}</p>
                    )}
                </div>

                {/* Desglose del precio */}
                <div className="mt-4">
                    <p className="text-sm text-gray-700">Precio Original: <span className="font-semibold">{price.toFixed(2)}€</span></p>
                    <p className="text-sm text-gray-700">Descuento: <span className="font-semibold">{discount || 0}%</span></p>
                    <p className="text-sm text-gray-700">Precio Final: <span className="font-semibold">{discountedPrice.toFixed(2)}€</span></p>
                </div>

                {/* Botón Enviar */}
                <div className="flex justify-center">
                    <button
                        type="submit"
                        className="w-2/3 py-3 mt-4 bg-yellow-400 text-black font-semibold rounded-lg hover:bg-yellow-500 focus:outline-none"
                    >
                        Aplicar Descuento
                    </button>
                </div>
            </form>
        </div>
    );
};

export default PromoteProductForm;
