import React, { useState, useEffect, useContext } from "react";
import { useParams, useNavigate } from "react-router-dom";
import bg from '../util-image/bg.png';
import Footer from './Footer';

const ReviewForm = () => {
  const { productId } = useParams();
  const [score, setScore] = useState(0);
  const [comment, setComment] = useState("");
  const [authToken] = useState(localStorage.getItem("authToken"));
  const navigate = useNavigate();
  const [product, setProduct] = useState({});

  useEffect(() => {
    const fetchProductDetails = async () => {
      try {
        const response = await fetch(`/api/product/details/${productId}`, {
          method: "GET",
        });

        if (!response.ok) {
          throw new Error("Error al obtener el producto: " + response.statusText);
        }

        const data = await response.json();
        data.sizes = data.size;
        data.categories = data.categories.map(category => category.id);

        setProduct(data);
      } catch (error) {
        console.error(error);
      }
    };

    fetchProductDetails();
  }, []);


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
        alert("¡Reseña añadida con éxito!");
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
    <>
      <div
        className="min-h-screen flex items-center justify-center p-6"
        style={{
          backgroundImage: `url(${bg})`,
          backgroundSize: 'cover',
          backgroundPosition: 'center',
          color: 'white',
          height: '100vh',
        }}
      >
        <div className="w-full max-w-4xl h-full rounded-lg shadow-lg bg-white backdrop-blur-md p-6 md:p-8">
          <h2 className="text-2xl font-bold mb-6 text-center text-black">
            ¿Qué te ha parecido el producto?
          </h2>

          <div className="flex flex-col md:flex-row justify-around items-center gap-6">
            {/* Contenedor de imagen */}
            <div className="shadow-lg rounded-lg p-4 flex justify-center items-center md:w-1/3">
              {product.images && product.images.length > 0 && (
                <img
                  src={`/api/product${product.images[0]}`}
                  alt={product.name || 'Producto'}
                  className="object-contain max-w-full max-h-[200px]"
                  loading="lazy"
                />
              )}
            </div>

            {/* Formulario */}
            <form onSubmit={handleSubmit} className="space-y-6 md:w-2/3 w-full">
              {/* Estrellas */}
              <div>
                <label className="block text-black font-medium">
                  Valoración:
                </label>
                <div>
                  {[1, 2, 3, 4, 5].map((star) => (
                    <span
                      key={star}
                      onClick={() => setScore(star)}
                      onMouseEnter={() => setScore(star)}
                      style={{
                        fontSize: "32px",
                        cursor: "pointer",
                        color: star <= score ? "#fbbf24" : "#e5e7eb",
                        transition: "color 0.2s",
                        textShadow: "0 1px 2px rgba(0,0,0,0.25)",
                      }}
                    >
                      ★
                    </span>
                  ))}
                </div>
              </div>

              {/* Comentario */}
              <div>
                <label className="block text-black font-medium mb-2">
                  Comentario:
                </label>
                <textarea
                  className="arial w-full p-4 border border-gray-300 rounded-lg focus:ring-1"
                  rows="4"
                  maxLength="255"
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  placeholder="Escribe tu experiencia..."
                ></textarea>
              </div>

              {/* Botón */}
              <div className="flex justify-center">
                <button
                  type="submit"
                  className="w-full md:w-2/3 button-custom bg-yellow-400 text-black py-2 px-4 border-none cursor-pointer"
                >
                  Enviar Reseña
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
      <Footer />
    </>
  );

};

export default ReviewForm;
