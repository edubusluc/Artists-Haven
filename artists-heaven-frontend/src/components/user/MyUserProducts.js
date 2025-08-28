import { useEffect, useState } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faThumbsUp } from "@fortawesome/free-solid-svg-icons";
import { useTranslation } from 'react-i18next';

const ITEMS_PER_PAGE = 3;

const MyUserProducts = () => {
    const [products, setProducts] = useState([]);
    const [page, setPage] = useState(1);
    const [authToken] = useState(localStorage.getItem("authToken"));
    const { t, i18n } = useTranslation();
    const currentLang = i18n.language;

    useEffect(() => {
        const fetchProducts = async () => {
            try {
                const res = await fetch("/api/user-products/myUserProducts", {
                    headers: {
                        Authorization: `Bearer ${authToken}`,
                    },
                });
                if (res.ok) {
                    const myUserProducts = await res.json();
                    setProducts(myUserProducts.data);
                }
            } catch (err) {
                console.error("Error fetching user products:", err);
            }
        };
        fetchProducts();
    }, [authToken]);

    const start = (page - 1) * ITEMS_PER_PAGE;
    const end = start + ITEMS_PER_PAGE;
    const currentProducts = products.slice(start, end);
    const totalPages = Math.ceil(products.length / ITEMS_PER_PAGE);

    const getStatusColor = (status) => {
        const lang = currentLang.startsWith("es") ? "es" : "en";
        console.log(lang)
        const statusText = lang === "es"
            ? {
                accepted: "ACEPTADO",
                pending: "PENDIENTE",
                rejected: "RECHZADO"
            }
            : {
                accepted: "ACCEPTED",
                pending: "PENDING",
                rejected: "REJECTED"
            };

        const statusLower = status?.toLowerCase();

        switch (statusLower) {
            case "accepted":
                return { colorClass: "bg-green-100 text-green-700", text: statusText.accepted };
            case "pending":
                return { colorClass: "bg-yellow-100 text-yellow-700", text: statusText.pending };
            case "rejected":
                return { colorClass: "bg-red-100 text-red-700", text: statusText.rejected };
            default:
                return { colorClass: "bg-gray-100 text-gray-700", text: statusText[statusLower] || "Unknown" };
        }
    };
    console.log(products)
    return (
        <div className="w-full">
            <h2 className="text-3xl font-bold mb-8 text-gray-900 text-center">
                ✨ My Products
            </h2>

            {currentProducts.length === 0 ? (
                <p className="text-gray-500 text-center py-10">No products found</p>
            ) : (
                <div className="grid gap-8 sm:grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
                    {currentProducts.map((prod, idx) => (
                        <div
                            key={idx}
                            className="bg-white/80 backdrop-blur-md border border-gray-100 rounded-2xl shadow-md hover:shadow-xl hover:-translate-y-1 transition-all duration-300 p-6 flex flex-col"
                        >
                            {/* Imagen */}
                            <div className="relative w-full aspect-[4/5] rounded-xl overflow-hidden group mb-4">
                                <img
                                    src={`/api/user-products${prod.images[0]}`}
                                    loading="lazy"
                                    className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
                                />
                                {prod.images[1] && (
                                    <img
                                        src={`/api/user-products${prod.images[1]}`}
                                        alt={`${prod.name} hover`}
                                        loading="lazy"
                                        className="absolute inset-0 h-full w-full object-cover opacity-0 group-hover:opacity-100 transition-all duration-500 group-hover:scale-105"
                                    />
                                )}
                            </div>

                            {/* Info */}
                            <h3 className="text-xl font-semibold text-gray-900 mb-2 line-clamp-1">
                                {prod.name}
                            </h3>
                            <p className="text-gray-600 inter-400 mb-3 line-clamp-2">{prod.description}</p>

                            {/* Status Badge */}
                            <span
                                className={`px-3 py-1 rounded-full text-sm inter-400 w-fit mb-4 ${getStatusColor(prod.status).colorClass}`}
                            >
                                {getStatusColor(prod.status).text}
                            </span>

                            {/* Votos */}
                            <div className="flex items-center gap-2 mt-auto justify-between">
                                <div className="space-x-1">
                                    <FontAwesomeIcon
                                        icon={faThumbsUp} className="w-5 h-5 text-gray-300" />
                                    <span className="text-gray-700 font-medium">{prod.numVotes}</span>
                                </div>
                                <p className="inter-400">
                                    <p className="inter-400">
                                        {new Date(prod.createdAt).toLocaleDateString('es-ES', {
                                            day: 'numeric',
                                            month: 'short',    
                                            year: 'numeric'
                                        })}
                                    </p>
                                </p>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* Paginación */}
            {totalPages > 1 && (
                <div className="flex justify-center items-center mt-10 space-x-2">
                    <button
                        onClick={() => setPage((p) => Math.max(p - 1, 1))}
                        disabled={page === 1}
                        className="px-4 py-2 rounded-full border bg-white text-gray-700 shadow-sm hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed transition"
                    >
                        ⬅ Prev
                    </button>

                    {Array.from({ length: totalPages }, (_, i) => (
                        <button
                            key={i}
                            onClick={() => setPage(i + 1)}
                            className={`px-4 py-2 rounded-full transition shadow-sm ${page === i + 1
                                ? "bg-indigo-600 text-white font-semibold"
                                : "bg-white border text-gray-700 hover:bg-gray-100"
                                }`}
                        >
                            {i + 1}
                        </button>
                    ))}

                    <button
                        onClick={() => setPage((p) => Math.min(p + 1, totalPages))}
                        disabled={page === totalPages}
                        className="px-4 py-2 rounded-full border bg-white text-gray-700 shadow-sm hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed transition"
                    >
                        Next ➡
                    </button>
                </div>
            )}
        </div>
    );
};

export default MyUserProducts;
