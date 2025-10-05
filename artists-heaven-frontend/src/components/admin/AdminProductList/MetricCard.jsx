import React from "react";
import { useTranslation } from "react-i18next";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

export const MetricCard = React.memo(
  ({ icon, value, title, iconColor, bgColor }) => (
    <div className="flex-1 bg-white shadow-lg rounded-lg p-4 m-2 flex items-center">
      <div
        className={`mr-4 w-12 h-12 rounded-full flex items-center justify-center ${bgColor}`}
      >
        <FontAwesomeIcon icon={icon} className={`${iconColor} text-xl`} />
      </div>
      <div>
        <p className="text-3xl font-bold text-indigo-600 truncate">{value}</p>
        <p className="text-sm font-semibold text-gray-400 truncate">{title}</p>
      </div>
    </div>
  )
);

export const Pagination = React.memo(({ page, totalPages, onPrev, onNext }) => {
  const prevDisabled = page === 0;
  const nextDisabled = page >= totalPages - 1;

  const baseBtn = "px-4 py-2 rounded w-full sm:w-auto font-medium transition-colors";
  const disabledClass = "bg-gray-300 text-gray-600 cursor-not-allowed opacity-50";
  const activeClass = "bg-gray-300 hover:bg-gray-400 text-gray-800";
  const { t } = useTranslation();

  return (
    <div className="flex flex-col sm:flex-row justify-center items-center mt-4 gap-2 sm:gap-4">
      <button
        onClick={onPrev}
        disabled={prevDisabled}
        aria-label="Página anterior"
        className={`${baseBtn} ${prevDisabled ? disabledClass : activeClass}`}
      >
        {t('adminProductList.previous')}
      </button>

      <span className="font-semibold text-gray-700 whitespace-nowrap">
        {t('adminProductList.page')} {page + 1} {t('adminProductList.of')} {totalPages}
      </span>

      <button
        onClick={onNext}
        disabled={nextDisabled}
        aria-label="Página siguiente"
        className={`${baseBtn} ${nextDisabled ? disabledClass : activeClass}`}
      >
        {t('adminProductList.next')}
      </button>
    </div>
  );
});