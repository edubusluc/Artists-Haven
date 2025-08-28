// src/components/ProductCard.js
const ProductCard = ({ product }) => (
  <div className="w-full group">
    <div
      className={`relative w-full aspect-[700/986] flex items-center justify-center bg-gray-100 overflow-hidden ${product.onPromotion ? 'promo-border' : ''}`}
    >
      <img
        src={`/api/product${product.images[0]}`}
        alt={product.name}
        loading="lazy"
        className="h-auto absolute object-contain transition-all duration-500 ease-in-out group-hover:opacity-0 group-hover:scale-110"
        style={{ transformOrigin: 'center center' }}
      />
      {product.images[1] && (
        <img
          src={`/api/product${product.images[1]}`}
          alt={`${product.name} hover`}
          loading="lazy"
          className="h-auto absolute object-cover opacity-0 transition-all duration-500 ease-in-out group-hover:opacity-100 group-hover:scale-110"
          style={{ transformOrigin: 'center center' }}
        />
      )}
    </div>
    <div className="mt-3 text-left ml-3">
      <p className="custom-font-shop-regular" style={{ color: 'black' }}>
        {product.name}
      </p>
      {product.onPromotion && product.discount > 0 ? (
        <div className="flex items-center gap-2">
          <span
            className="custom-font-shop-regular line-through"
            style={{ color: '#909497', fontSize: '15px' }}
          >
            {(product.price / ((100 - product.discount) / 100)).toFixed(2)}€
          </span>
          <span
            className="custom-font-shop-regular"
            style={{ color: 'red' }}
          >
            {product.price.toFixed(2)}€
          </span>
        </div>
      ) : (
        <span
          className="custom-font-shop-regular"
          style={{ color: '#909497', fontSize: '15px' }}
        >
          {product.price.toFixed(2)}€
        </span>
      )}
    </div>
  </div>
);


export default ProductCard;
