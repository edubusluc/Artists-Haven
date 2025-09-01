import { useTranslation } from 'react-i18next';
import { motion } from "framer-motion";
import Footer from './Footer';

const AboutUs = () => {
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
            {t('about.title')}
          </h1>

          <div className="space-y-6">
            <motion.p
              className=" text-gray-700 inter-400 text-md leading-relaxed"
              initial={{ opacity: 0, x: -30 }}
              whileInView={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.5 }}
              viewport={{ once: true }}
            >
              {t('about.paragraph1')}
            </motion.p>

            <motion.p
              className="text-gray-700 inter-400 text-md leading-relaxed"
              initial={{ opacity: 0, x: -30 }}
              whileInView={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.5, delay: 0.2 }}
              viewport={{ once: true }}
            >
              {t('about.paragraph2')}
            </motion.p>

            <motion.p
              className="text-gray-700 inter-400 text-md leading-relaxed"
              initial={{ opacity: 0, x: -30 }}
              whileInView={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.5, delay: 0.4 }}
              viewport={{ once: true }}
            >
              {t('about.paragraph3')}
            </motion.p>
          </div>
        </motion.div>
      </div>
    </div><Footer /></>
  );
};

export default AboutUs;
