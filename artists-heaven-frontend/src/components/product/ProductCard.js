// src/components/ProductCard.js
import { useTranslation } from "react-i18next";

const ProductCard = ({ product }) => {
  const { t } = useTranslation();

  // 🔹 Verificamos si hay stock en al menos un color
  const hasStock = product.colors?.some(color =>
    product.section === "ACCESSORIES"
      ? (color.availableUnits ?? 0) > 0
      : Object.values(color.sizes || {}).some(qty => qty > 0)
  );

  const isDark = (hex) => {
    if (!hex) return false;
    let c = hex.substring(1); // quitar "#"
    if (c.length === 3) {
      c = c.split("").map(ch => ch + ch).join(""); // expandir #123 -> #112233
    }
    const r = parseInt(c.substr(0, 2), 16);
    const g = parseInt(c.substr(2, 2), 16);
    const b = parseInt(c.substr(4, 2), 16);

    // fórmula relativa de luminancia
    const luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;

    return luminance < 0.5; // si < 0.5 lo consideramos oscuro
  };

  return (
    <div className="w-full group">
      {/* Imagen principal */}
      <div
        className={`relative w-full aspect-[700/986] flex items-center justify-center bg-gray-100 overflow-hidden
          ${!hasStock ? "grayscale-50" : ""} ${product.onPromotion ? "promo-border" : ""}`}
        data-content={product.onPromotion ? t("promotion") : ""}
      >
        <img
          src={`http://localhost:8080/api/product${product.colors[0].images[0]}`}
          alt={product.name}
          loading="lazy"
          className="h-auto absolute object-contain transition-all duration-500 ease-in-out 
                     group-hover:opacity-0 group-hover:scale-110"
        />
        {product.colors[0].images[1] && (
          <img
            src={`http://localhost:8080/api/product${product.colors[0].images[1]}`}
            alt={`${product.name} hover`}
            loading="lazy"
            className="h-auto absolute object-cover opacity-0 transition-all duration-500 
                       ease-in-out group-hover:opacity-100 group-hover:scale-110"
          />
        )}

        {/* 🔹 Mensaje "Sin stock" solo si NO hay stock en ningún color */}
        {!hasStock && (
          <div className="absolute inset-0 bg-gray-200 bg-opacity-70 flex items-center justify-center">
            {/* Etiqueta lateral SOLD OUT */}
            <div className="absolute top-10 right-0 ">
              <div className="bg-black text-white text-xs font-bold px-2 py-1 transform -rotate-90 origin-center">
                {t("productDetails.ProductOutOfStock")}
              </div>
            </div>
          </div>
        )}

        {/* 🔹 Tallas disponibles al hover (parte inferior) */}
        {hasStock && (
          <div className="absolute bottom-0 left-0 w-full 
                  backdrop-blur-sm shadow-md
                  opacity-0 group-hover:opacity-100 
                  transition-all duration-300 ease-in-out 
                  p-3">
            {product.section === "ACCESSORIES" ? (
              <div className="flex flex-col items-center gap-1">

              </div>
            ) : (
              <div className="space-y-2 max-h-30 overflow-y-auto scrollbar-thin scrollbar-thumb-gray-300">
                {product.colors.map((color, i) => {
                  const sizes = Object.entries(color.sizes || {}).filter(([, qty]) => qty > 0);

                  // Definir el orden deseado
                  const order = ["XS", "S", "M", "L", "XL", "XXL"];

                  // Reordenar las tallas
                  const sortedSizes = sizes.sort(
                    ([a], [b]) => order.indexOf(a) - order.indexOf(b)
                  );

                  return (
                    <div key={i} className="flex flex-col items-center">
                      <div className="flex flex-wrap justify-center gap-1">
                        {sortedSizes.length > 0 ? (
                          sortedSizes.map(([size]) => (
                            <span
                              key={size}
                              className={`w-16 h-8 flex items-center justify-center 
    text-sm inter-400 uppercase
    border border-gray-300 shadow-sm hover:opacity-80 transition rounded-sm`}
                              style={{ backgroundColor: color.hexCode, color: isDark(color.hexCode) ? "white" : "black" }}
                            >
                              {size}
                            </span>
                          ))
                        ) : (
                          <span className="text-xs text-red-500">{t("NotAvailable")}</span>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        )}
      </div>

      {/* Nombre + Precio */}
      <div className="mt-3 text-left ml-3">
        <p className="custom-font-shop-regular" style={{ color: 'black' }}>{product.name}</p>
        {product.onPromotion && product.discount > 0 ? (
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

      {/* Colores disponibles */}
      <div className="flex gap-1 ml-3 mt-2">
        {product.colors?.map((color, i) => (
          <span
            key={i}
            className="w-4 h-4 rounded-full border"
            style={{ backgroundColor: color.hexCode }}
          />
        ))}
      </div>
    </div>
  );
};

export default ProductCard;
