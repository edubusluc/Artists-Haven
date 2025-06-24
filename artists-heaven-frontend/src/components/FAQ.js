import { useState } from "react";
import faqData from "../utils/FAQ.json"; // Ajusta según tu estructura
import { useTranslation } from "react-i18next";

const FAQ = () => {
  const [activeIndex, setActiveIndex] = useState(null);
  const { t, i18n } = useTranslation();

  const language = i18n.language.split("-")[0];
  const questions = faqData.faq[language]?.questions || [];

  const toggleQuestion = (index) => {
    setActiveIndex((prevIndex) => (prevIndex === index ? null : index));
  };

  return (
    <div
      style={{
        maxWidth: "800px",
        margin: "0 auto",
        padding: "2rem",
        fontFamily: "system-ui, sans-serif",
      }}
    >
      <h1 className="custom-font-shop custom-font-shop-black" style={{fontSize:"30px"}}>FAQs</h1>
      {questions.length > 0 ? (
        <ul style={{ listStyle: "none", padding: 0 }}>
          {questions.map((item, index) => {
            const isOpen = activeIndex === index;
            return (
              <li
                key={index}
                style={{
                  marginBottom: "1rem",
                  borderRadius: "0.5rem",
                  border: "1px solid #e5e7eb",
                  overflow: "hidden",
                  boxShadow: isOpen ? "0 4px 8px rgba(0,0,0,0.05)" : "none",
                  transition: "box-shadow 0.3s ease",
                }}
              >
                <button
                  onClick={() => toggleQuestion(index)}
                  aria-expanded={isOpen}
                  aria-controls={`faq-content-${index}`}
                  style={{
                    width: "100%",
                    textAlign: "left",
                    padding: "1rem",
                    background: "white",
                    border: "none",
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    fontSize: "1rem",
                    fontWeight: "600",
                    cursor: "pointer",
                    transition: "background 0.3s ease",
                  }}
                >
                  {item.question}
                  <span
                    style={{
                      transform: isOpen ? "rotate(180deg)" : "rotate(0deg)",
                      transition: "transform 0.3s ease",
                      display: "inline-block",
                    }}
                  >
                    ▼
                  </span>
                </button>
                <div
                  id={`faq-content-${index}`}
                  style={{
                    maxHeight: isOpen ? "300px" : "0",
                    overflow: "hidden",
                    padding: isOpen ? "0 1rem 1rem" : "0 1rem",
                    fontSize: "0.95rem",
                    color: "#4b5563",
                    transition: "all 0.4s ease",
                    background: "#f9fafb",
                  }}
                >
                  {item.answer}
                </div>
              </li>
            );
          })}
        </ul>
      ) : (
        <p style={{ textAlign: "center", color: "#9ca3af" }}>No FAQs available.</p>
      )}
    </div>
  );
};

export default FAQ;
