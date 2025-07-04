// src/components/ProductCard.js
import React from "react";

const ProductCard = ({ product }) => (
    <div className="w-full group">
        <div className="relative w-full h-[300px] md:h-[600px] flex items-center justify-center bg-gray-100 overflow-hidden">
            <img
                src={`/api/product${product.images[0]}`}
                alt={product.name}
                loading="lazy"
                decoding="async"
                className="absolute object-contain transition-all duration-500 ease-in-out group-hover:opacity-0 group-hover:scale-95"
            />
            {product.images[1] && (
                <img
                    src={`/api/product${product.images[1]}`}
                    alt={product.name}
                    loading="lazy"
                    decoding="async"
                    className="absolute object-contain opacity-0 transition-all duration-500 ease-in-out group-hover:opacity-100 group-hover:scale-100"
                />
            )}
        </div>
        <div className="mt-3 text-left">
            <p className="custom-font-shop-regular" style={{ color: "black" }}>
                {product.name}
            </p>
            {product.on_Promotion && product.discount > 0 ? (
                <div className="flex items-center gap-2">
                    <span
                        className="custom-font-shop-regular line-through"
                        style={{ color: "#909497", fontSize: "15px" }}
                    >
                        {(product.price / ((100 - product.discount) / 100)).toFixed(2)}€
                    </span>
                    <span className="custom-font-shop-regular" style={{ color: "red" }}>
                        {product.price.toFixed(2)}€
                    </span>
                </div>
            ) : (
                <span
                    className="custom-font-shop-regular"
                    style={{ color: "#909497", fontSize: "15px" }}
                >
                    {product.price.toFixed(2)}€
                </span>
            )}
        </div>
    </div>
);

export default ProductCard;
