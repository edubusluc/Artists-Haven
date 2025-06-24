// src/components/AddReviewModal.js

import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";

const AddReviewModal = ({ isOpen, onClose, productId, authToken }) => {
    const [score, setScore] = useState(0);
    const [comment, setComment] = useState("");
    const { t } = useTranslation();
    const navigate = useNavigate();

    const handleClose = () => {
        setScore(0);
        setComment('');
        onClose();
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        const reviewData = {
            productId: Number(productId),
            score,
            comment,
        };

        try {
            const response = await fetch(`/api/rating/new`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    'Authorization': `Bearer ${authToken}`
                },
                body: JSON.stringify(reviewData),
            });

            if (response.ok) {
                alert("¡Reseña añadida con éxito!");
                setScore(0);
                setComment("");
                navigate(`/product/details/${productId}`);
                onClose(); // Cerrar modal después de enviar la reseña
            } else if (response.status === 401) {
                alert("Por favor, inicia sesión para poder dejar una reseña.");
                setScore(0);
                setComment("");
            } else {
                alert("Error al añadir la reseña");
            }
        } catch (error) {
            console.error("Error:", error);
            alert("Ocurrió un error al enviar la reseña.");
        }
    };

    if (!isOpen) return null;

    return (
        <>
            <div
                style={{
                    position: "fixed",
                    top: 0,
                    left: 0,
                    width: "100%",
                    height: "100%",
                    backgroundColor: "rgba(0, 0, 0, 0.5)",
                    zIndex: 998,
                    backdropFilter: "blur(10px)",
                    WebkitBackdropFilter: "blur(10px)",
                }}
            />
            <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50" style={{ zIndex: 999 }}>
                <div className="bg-white rounded-lg p-6 w-full max-w-lg">
                    <h2 className="text-xl font-bold mb-4 text-center">{t("AddReview")}</h2>

                    {/* Formulario de reseña */}
                    <form onSubmit={handleSubmit} className="space-y-6">
                        {/* Estrellas */}
                        <div>
                            <label className="block text-black font-medium">
                                {t("Rating")}:
                            </label>
                            <div>
                                {[1, 2, 3, 4, 5].map((star) => (
                                    <span
                                        key={star}
                                        onClick={() => setScore(star)}
                                        onMouseEnter={() => setScore(star)}
                                        style={{
                                            fontSize: "32px",
                                            cursor: "pointer",
                                            color: star <= score ? "#fbbf24" : "#e5e7eb",
                                            transition: "color 0.2s",
                                            textShadow: "0 1px 2px rgba(0,0,0,0.25)",
                                        }}
                                    >
                                        ★
                                    </span>
                                ))}
                            </div>
                        </div>

                        {/* Comentario */}
                        <div>
                            <label className="block text-black font-medium mb-2">
                                {t("Comment")}:
                            </label>
                            <textarea
                                className="arial w-full p-4 border border-gray-300 rounded-lg focus:ring-1"
                                rows="4"
                                maxLength="255"
                                value={comment}
                                onChange={(e) => setComment(e.target.value)}
                                placeholder={t("Deja aquí tu reseña!")}
                            ></textarea>
                        </div>

                        {/* Botón */}
                        <div className="flex justify-center inter-400 text-sm">
                            <button
                                type="submit"
                                className="w-full md:w-2/3 bg-yellow-400 text-black py-3 px-6 rounded-lg shadow-md transform transition duration-300 ease-in-out hover:bg-yellow-500 hover:scale-105 focus:outline-none focus:ring-2 focus:ring-yellow-300"
                            >
                                Enviar Reseña
                            </button>
                        </div>
                    </form>
                    <div className="flex justify-center inter-400 text-sm">
                        <button
                            onClick={handleClose}
                            setScore={0}
                            setComment={""}
                            className="mt-4 w-full md:w-2/3 text-center text-blue-500 inter-400 text-sm py-3 px-6 rounded-lg transform transition duration-300 ease-in-out hover:bg-blue-100 hover:text-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-300"
                        >
                            Cerrar
                        </button>
                    </div>
                </div>
            </div>
        </>
    );
};

export default AddReviewModal;
