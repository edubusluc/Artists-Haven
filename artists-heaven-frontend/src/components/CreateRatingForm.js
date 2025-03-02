import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";

const ReviewForm = () => {
    const { productId } = useParams(); // Obtener el id del producto desde la URL
    const [score, setScore] = useState(0); // Estado para la puntuación
    const [comment, setComment] = useState(""); // Estado para el comentario
    const [authToken] = useState(localStorage.getItem("authToken"));   
    const navigate = useNavigate();

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
                alert("Reseña añadida con éxito!");
                setScore(0);
                setComment("");
                navigate(`/product/details/${productId}`);
            } else {
                alert("Error al añadir la reseña");
            }
        } catch (error) {
            console.error("Error:", error);
            alert("Ocurrió un error al enviar la reseña.");
        }
    };

    return (
        <div className="container mt-4">
            <h2>Añadir Reseña</h2>
            <form onSubmit={handleSubmit}>
                {/* Estrellas */}
                <div className="mb-3">
                    <label className="form-label">Valoración:</label>
                    <div>
                        {[1, 2, 3, 4, 5].map((star) => (
                            <span
                                key={star}
                                onClick={() => setScore(star)}
                                style={{
                                    fontSize: "24px",
                                    cursor: "pointer",
                                    color: star <= score ? "#ffc107" : "#e4e5e9",
                                }}
                            >
                                ★
                            </span>
                        ))}
                    </div>
                </div>

                {/* Comentario */}
                <div className="mb-3">
                    <label className="form-label">Comentario:</label>
                    <textarea
                        className="form-control"
                        rows="3"
                        maxLength="255"
                        value={comment}
                        onChange={(e) => setComment(e.target.value)}
                    ></textarea>
                </div>

                <button type="submit" className="btn btn-primary">
                    Enviar Reseña
                </button>
            </form>
        </div>
    );
};

export default ReviewForm;
