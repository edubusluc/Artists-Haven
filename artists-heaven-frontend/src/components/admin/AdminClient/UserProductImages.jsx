import React, { useState } from "react";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

export const UserProductImages = ({ images, productName }) => {
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [currentIndex, setCurrentIndex] = useState(0);

    if (!images || images.length === 0) return "No image";

    const openModal = (index) => {
        setCurrentIndex(index);
        setIsModalOpen(true);
    };

    const closeModal = () => setIsModalOpen(false);

    const nextImage = () => setCurrentIndex((prev) => (prev + 1) % images.length);
    const prevImage = () => setCurrentIndex((prev) => (prev - 1 + images.length) % images.length);

    return (
        <>
            <div className="flex gap-2">
                {images.map((img, index) => (
                    <img
                        key={index}
                        src={`http://localhost:8080/api/user-products${img}`}
                        alt={`${productName} ${index + 1}`}
                        className="w-16 h-16 object-cover rounded cursor-pointer hover:scale-105 transition"
                        onClick={() => openModal(index)}
                    />
                ))}
            </div>

            {isModalOpen && (
                <div className="fixed inset-0 bg-black bg-opacity-70 flex items-center justify-center z-50">
                    <div className="relative">
                        <img
                            src={`http://localhost:8080/api/user-products${images[currentIndex]}`}
                            alt={`${productName} ${currentIndex + 1}`}
                            className="max-w-[90vw] max-h-[80vh] object-contain rounded shadow-lg"
                        />
                        {images.length > 1 && (
                            <>
                                <button
                                    onClick={prevImage}
                                    className="absolute top-1/2 left-2 -translate-y-1/2 text-white text-2xl font-bold"
                                >
                                    ‹
                                </button>
                                <button
                                    onClick={nextImage}
                                    className="absolute top-1/2 right-2 -translate-y-1/2 text-white text-2xl font-bold"
                                >
                                    ›
                                </button>
                            </>
                        )}

                    </div>
                    <button
                        onClick={closeModal}
                        className="absolute top-10 right-10 bg-black/60 hover:bg-black/80 text-white rounded-full w-8 h-8 flex items-center justify-center text-lg font-bold shadow-md"
                    >
                        ✕
                    </button>
                </div>
            )}
        </>
    );
};

export const MetricCard = React.memo(({ icon, value, title, iconColor, bgColor }) => {
    return (
        <div className="flex-1 w-auto bg-white shadow-lg rounded-lg p-4 m-2 flex items-center">
            <div className={`flex items-center justify-center mr-4 w-12 h-12 rounded-full ${bgColor}`}>
                <FontAwesomeIcon icon={icon} className={`${iconColor} text-xl`} />
            </div>
            <div>
                <p className="text-3xl font-bold text-indigo-600 truncate">{value}</p>
                <p className="text-sm font-semibold text-gray-400 truncate">{title}</p>
            </div>
        </div>
    );
});