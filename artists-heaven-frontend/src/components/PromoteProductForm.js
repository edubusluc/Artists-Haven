import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";

const PromoteProductForm = () => {
    const [discount, setDiscount] = useState(0);
    const [authToken] = useState(localStorage.getItem("authToken"));
    const { id } = useParams();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();

        const reviewData = {
            id: Number(id),
            discount: discount,
        };

        try {
            const response = await fetch(`/api/product/promote/${id}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    'Authorization': `Bearer ${authToken}`
                },
                body: JSON.stringify(reviewData),

            });

            const message = await response.text();
            if (response.ok) {
                alert("Promoción añadida con éxito!");
                setDiscount(0);
                navigate(`/product/all`);
            } else {
                alert("Error: " + message);
            }
        } catch (error) {
            console.error("Error:", error);
            alert("Ocurrió un error al enviar la reseña.");
        }
    };

    return (
        <div className="container mt-4">
            <h2>Añadir Descuento</h2>
            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                <label className="text-sm font-medium">Discount (%)</label>
                <textarea
                    type="number"
                    min="0"
                    max="100"
                    value={discount}
                    onChange={(e) => setDiscount(e.target.value)}
                    required
                />
                <button type="submit" className="mt-4">Apply Discount</button>
            </form>
        </div>
    );
};

export default PromoteProductForm;
