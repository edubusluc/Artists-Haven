import React from "react";
import faqData from "../utils/FAQ.json"; // Ajusta la ruta al archivo JSON según tu estructura de carpetas

const FAQ = () => {
  // Detectar el idioma del navegador
  const userLanguage = navigator.language.split("-")[0]; // Obtiene el código del idioma (ej. 'en' o 'es')

  // Verificar si existen preguntas para el idioma detectado, si no, se usa el idioma por defecto ('en')
  const language = faqData.faq[userLanguage] ? userLanguage : "en"; // Si el idioma no está disponible, usa 'en' por defecto
  const questions = faqData.faq[language].questions;

  return (
    <div>
      <h1>FAQs</h1>
      {questions.length > 0 ? (
        <ul>
          {questions.map((item, index) => (
            <li key={index}>
              <h3>{item.question}</h3>
              <p>{item.answer}</p>
            </li>
          ))}
        </ul>
      ) : (
        <p>No FAQs available.</p>
      )}
    </div>
  );
};

export default FAQ;
