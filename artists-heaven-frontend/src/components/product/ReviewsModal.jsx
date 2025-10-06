import React, { useState, useMemo } from "react";
import { useTranslation } from "react-i18next";
import defaultUserImg from "../../util-image/defaultUser.png";
import SlidingPanel from "../../utils/SlidingPanel";

const ReviewsModal = ({ isOpen, onClose, reviews }) => {
  const { t } = useTranslation();
  const [filter, setFilter] = useState("date-desc"); // valor inicial: más recientes primero

  // Reseñas ordenadas según el filtro
  const filteredReviews = useMemo(() => {
    if (!reviews) return [];
    const sorted = [...reviews];

    switch (filter) {
      case "date-asc":
        return sorted.sort(
          (a, b) => new Date(a.createdAt) - new Date(b.createdAt)
        );
      case "date-desc":
        return sorted.sort(
          (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
        );
      case "score-asc":
        return sorted.sort((a, b) => a.score - b.score);
      case "score-desc":
        return sorted.sort((a, b) => b.score - a.score);
      default:
        return sorted;
    }
  }, [reviews, filter]);

  return (
    <SlidingPanel
      isOpen={isOpen}
      onClose={onClose}
      title={t("review.form.title")}
      position="right"
      maxWidth="400px"
    >
      {reviews.length > 0 ? (
        <div className="space-y-4">
          {/* Selector de filtro */}
          <div className="flex justify-end mb-2">
            <select
              value={filter}
              onChange={(e) => setFilter(e.target.value)}
              className="border rounded-md text-sm p-1"
            >
              <option value="date-desc">{t("Más recientes")}</option>
              <option value="date-asc">{t("Más antiguas")}</option>
              <option value="score-desc">{t("Mejor puntuación")}</option>
              <option value="score-asc">{t("Peor puntuación")}</option>
            </select>
          </div>

          {filteredReviews.map((review, index) => (
            <div key={index} className="border-b pb-2">
              {/* Imagen + username */}
              <div className="flex items-center gap-3">
                <img
                  src={defaultUserImg}
                  alt="User avatar"
                  className="w-8 h-8 rounded-full object-cover"
                />
                <p className="inter-400 text-sm text-gray-500">
                  {review.username || t("Anonymous")}
                </p>
              </div>

              {/* Estrellas */}
              <div className="flex items-center text-yellow-400 text-sm mt-1">
                {"★".repeat(review.score) + "☆".repeat(5 - review.score)}
              </div>

              {/* Comentario */}
              <p className="text-sm text-gray-600 mt-1 inter-400">
                {review.comment}
              </p>

              {/* Fecha */}
              <p className="text-xs text-gray-400 mt-1 inter-400">
                {new Date(review.createdAt).toLocaleDateString()}
              </p>
            </div>
          ))}
        </div>
      ) : (
        <p className="text-sm text-gray-500">{t("No reviews")}</p>
      )}
    </SlidingPanel>
  );
};

export default ReviewsModal;
