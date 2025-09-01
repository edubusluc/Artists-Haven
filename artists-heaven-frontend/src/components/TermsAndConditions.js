import { useTranslation } from 'react-i18next';
import { motion } from "framer-motion";
import Footer from './Footer';

const TermsAndConditions = () => {
  const { t } = useTranslation();

  return (
    <><div className="min-h-screen bg-gradient-to-b from-gray-50 to-white py-12 px-6">
      <div className="max-w-4xl mx-auto">
        <motion.div
          className="bg-white shadow-xl rounded-2xl p-8 border border-gray-100"
          initial={{ opacity: 0, y: 40 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
        >
          <h1 className="text-4xl font-extrabold text-gray-900 mb-6 text-center">
            {t('terms.title')}
          </h1>
          <p className="text-lg text-gray-600 leading-relaxed mb-8 inter-400 text-center">
            {t('terms.intro')}
          </p>

          <div className="space-y-8">
            <motion.div
              initial={{ opacity: 0, x: -30 }}
              whileInView={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.5 }}
              viewport={{ once: true }}
            >
              <h2 className="text-2xl font-semibold text-gray-800 border-l-4 border-blue-500 pl-3 mb-2">
                {t('terms.useTitle')}
              </h2>
              <p className="text-gray-700 inter-400 text-sm leading-relaxed">{t('terms.use')}</p>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, x: -30 }}
              whileInView={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.5, delay: 0.2 }}
              viewport={{ once: true }}
            >
              <h2 className="text-2xl font-semibold text-gray-800 border-l-4 border-green-500 pl-3 mb-2">
                {t('terms.shippingTitle')}
              </h2>
              <p className="text-gray-700 inter-400 text-sm leading-relaxed">{t('terms.shipping')}</p>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, x: -30 }}
              whileInView={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.5, delay: 0.4 }}
              viewport={{ once: true }}
            >
              <h2 className="text-2xl font-semibold text-gray-800 border-l-4 border-purple-500 pl-3 mb-2">
                {t('terms.lawTitle')}
              </h2>
              <p className="text-gray-700 inter-400 text-sm leading-relaxed">{t('terms.law')}</p>
            </motion.div>
          </div>
        </motion.div>
      </div>
    </div><Footer /></>
  );
};

export default TermsAndConditions;
