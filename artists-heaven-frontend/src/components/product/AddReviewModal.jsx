import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import SlidingPanel from "../../utils/SlidingPanel";

const AddReviewModal = ({ isOpen, onClose, productId, authToken }) => {
    const [score, setScore] = useState(0);
    const [hoverScore, setHoverScore] = useState(0);
    const [comment, setComment] = useState("");
    const navigate = useNavigate();
    const { t, i18n } = useTranslation();
    const language = i18n.language;

    const handleClose = () => {
        setScore(0);
        setHoverScore(0);
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

        if (score <= 0) {
            alert(t("review.add.select_score"));
            return;
        }

        if (!comment.trim()) {
            alert(t("review.add.enter_comment"));
            return;
        }

        if (!authToken) {
            alert(t("review.add.login_required"));
            handleClose();
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/api/rating/new?lang=${language}`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${authToken}`
                },
                body: JSON.stringify(reviewData),
            });

            const result = await response.json();

            if (response.status === 201) {
                alert(t("review.add.success"));
                handleClose();
                window.location.reload();
            } else if (response.status === 401) {
                alert(t("review.add.login_required"));
                handleClose();
            } else if (response.status === 403) {
                alert(t("review.add.not_allowed"));
                handleClose();
            } else if (response.status === 409) {
                alert(t("review.add.duplicate"));
                handleClose();
            } else if (response.status === 404) {
                alert(t("review.add.not_found"));
                handleClose();
            } else {
                alert(result.message);
                handleClose();
            }
        } catch (error) {
            console.error("Error:", error);
            alert(t("review.add.network_error"));
        }
    };

    return (
        <SlidingPanel
            isOpen={isOpen}
            onClose={onClose}
            title={t("review.form.title")}
            position="right"
            maxWidth="400px"
        >
            <form onSubmit={handleSubmit} className="space-y-6">
                {/* Estrellas */}
                <div>
                    <label className="block text-black inter-400 text-sm mb-1">{t("review.form.rating")}:</label>
                    <div className="flex gap-1 text-2xl justify-center">
                        {[1, 2, 3, 4, 5].map((star) => (
                            <span
                                key={star}
                                onClick={() => setScore(star)}
                                onMouseEnter={() => setHoverScore(star)}
                                onMouseLeave={() => setHoverScore(0)}
                                style={{
                                    cursor: "pointer",
                                    color:
                                        star <= (hoverScore || score)
                                            ? "#facc15" // amarillo
                                            : "#e5e7eb", // gris
                                    transition: "color 0.2s",
                                }}
                            >
                                ★
                            </span>
                        ))}
                    </div>
                    {score > 0 && (
                        <p className="text-center text-sm text-gray-600 mt-1">
                            {t("review.form.you_rated")} {score} / 5
                        </p>
                    )}
                </div>

                {/* Comentario */}
                <div>
                    <label className="block text-black inter-400 text-sm mb-2">{t("review.form.comment")}:</label>
                    <textarea
                        className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-yellow-400"
                        rows="4"
                        maxLength="255"
                        value={comment}
                        onChange={(e) => setComment(e.target.value)}
                        placeholder={t("review.form.placeholder")}
                    ></textarea>
                </div>

                {/* Botón enviar */}
                <div className="flex justify-center">
                    <button
                        type="submit"
                        className="w-full py-3 px-3 md:w-2/3 inter-400 text-sm button-custom">
                        {t("review.form.submit")}
                    </button>
                </div>
            </form>
        </SlidingPanel>
    );
};

export default AddReviewModal;
