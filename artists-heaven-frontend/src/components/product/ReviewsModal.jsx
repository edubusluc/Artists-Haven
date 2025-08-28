import React from "react";
import { useTranslation } from "react-i18next";
import defaultUserImg from "../../util-image/defaultUser.png";
import SlidingPanel from "../../utils/SlidingPanel";

const ReviewsModal = ({ isOpen, onClose, reviews }) => {
  const { t } = useTranslation();

  return (
    <SlidingPanel
      isOpen={isOpen}
      onClose={onClose}
      title={t("Product reviews")}
      position="right"
      maxWidth="400px"
    >
      {reviews.length > 0 ? (
        <div className="space-y-4">
          {reviews.map((review, index) => (
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
