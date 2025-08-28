import { motion, AnimatePresence } from "framer-motion";

const SlidingPanel = ({
    isOpen,
    position = "right",
    onClose,
    children,
    maxWidth = "400px",
    title,
}) => {
    const sideStyles = position === "left"
        ? { left: 0 }
        : { right: 0 };

    return (
        <AnimatePresence>
            {isOpen && (
                <>
                    <motion.div
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                        onClick={onClose}
                        className="fixed inset-0 bg-black bg-opacity-50 backdrop-blur-sm z-[1201]"
                    />
                    <motion.div
                        initial={{ x: position === 'left' ? '-100%' : '100%' }}
                        animate={{ x: 0 }}
                        exit={{ x: position === 'left' ? '-100%' : '100%' }}
                        transition={{ duration: 0.3 }}
                        className="fixed top-0 h-full bg-white z-[1202] p-6 overflow-y-auto shadow-xl"
                        style={{ ...sideStyles, maxWidth, width: '100%' }}
                    >
                        <header className="flex justify-between items-center mb-4">
                            <p className="inter-400 text-sm">{title}</p>
                            <button
                                onClick={onClose}
                                aria-label="Close"
                                className="w-8 h-8 flex items-center justify-center rounded-full bg-gray-200 hover:bg-gray-300 text-gray-700 hover:text-black transition duration-200 shadow-md"
                            >
                                &times;
                            </button>
                        </header>
                        <hr className="border-t border-gray-300 mb-4" />
                        {children}
                    </motion.div>
                </>
            )}
        </AnimatePresence>
    );
};

export default SlidingPanel;