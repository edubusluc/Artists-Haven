import { useState } from "react";
import faqData from "../utils/FAQ.json";
import { useTranslation } from "react-i18next";
import { motion, AnimatePresence } from "framer-motion";
import Footer from "./Footer"

const FAQ = () => {
  const [activeIndex, setActiveIndex] = useState(null);
  const { t, i18n } = useTranslation();

  const language = i18n.language.split("-")[0];
  const questions = faqData.faq[language]?.questions || [];

  const toggleQuestion = (index) => {
    setActiveIndex((prevIndex) => (prevIndex === index ? null : index));
  };

  // Variants para animaciones suaves
  const containerVariants = {
    hidden: { opacity: 0, y: 30 },
    visible: {
      opacity: 1,
      y: 0,
      transition: { duration: 0.6, ease: "easeOut", staggerChildren: 0.1 },
    },
  };

  const itemVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: { opacity: 1, y: 0, transition: { duration: 0.4, ease: "easeOut" } },
  };

  const answerVariants = {
    hidden: { opacity: 0, height: 0 },
    visible: {
      opacity: 1,
      height: "auto",
      transition: { duration: 0.4, ease: [0.4, 0, 0.2, 1] },
    },
    exit: {
      opacity: 0,
      height: 0,
      transition: { duration: 0.3, ease: "easeInOut" },
    },
  };

  return (
    <><div className="min-h-screen bg-gradient-to-b from-gray-50 to-white py-12 px-6">
      <div className="max-w-4xl mx-auto">
        <motion.div
          className="bg-white shadow-xl rounded-2xl p-8 border border-gray-100"
          variants={containerVariants}
          initial="hidden"
          animate="visible"
        >
          <h1 className="text-4xl font-extrabold text-gray-900 mb-8 text-center">
            {t("faq.title") || "FAQs"}
          </h1>

          {questions.length > 0 ? (
            <motion.ul
              className="space-y-4"
              variants={containerVariants}
              initial="hidden"
              animate="visible"
            >
              {questions.map((item, index) => {
                const isOpen = activeIndex === index;
                return (
                  <motion.li
                    key={index}
                    variants={itemVariants}
                    className={`rounded-2xl border border-gray-200 overflow-hidden transition-shadow ${isOpen ? "shadow-lg" : "shadow-sm"}`}
                  >
                    <button
                      onClick={() => toggleQuestion(index)}
                      aria-expanded={isOpen}
                      aria-controls={`faq-content-${index}`}
                      className="w-full text-left px-6 py-4 bg-white flex justify-between items-center font-semibold text-gray-800 hover:bg-gray-50 transition-colors"
                    >
                      {item.question}
                      <motion.span
                        animate={{ rotate: isOpen ? 180 : 0 }}
                        transition={{ duration: 0.3, ease: "easeInOut" }}
                        className="inline-block"
                      >
                        ▼
                      </motion.span>
                    </button>

                    <AnimatePresence initial={false}>
                      {isOpen && (
                        <motion.div
                          id={`faq-content-${index}`}
                          initial={{ opacity: 0, scaleY: 0 }}
                          animate={{ opacity: 1, scaleY: 1 }}
                          exit={{ opacity: 0, scaleY: 0 }}
                          transition={{ duration: 0.35, ease: "easeInOut" }}
                          className="origin-top px-6 pt-0 pb-4 text-gray-600 bg-gray-50 text-sm"
                        >
                          {item.answer}
                        </motion.div>
                      )}
                    </AnimatePresence>
                  </motion.li>
                );
              })}
            </motion.ul>
          ) : (
            <p className="text-center text-gray-400">{t("faq.noFaqAvailable")}</p>
          )}
        </motion.div>
      </div>

    </div>
    <Footer />
    </>
  );
};

export default FAQ;
