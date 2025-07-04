import { AnimatePresence, motion } from "framer-motion";
import ReactDOM from "react-dom";

const SlidingPanel = ({ isOpen, position = "right", onClose, children, maxWidth = "500px" }) => {
    const sideStyles = position === "left" ? { left: 0 } : { right: 0 };

    return ReactDOM.createPortal(
        <AnimatePresence>
            {isOpen && (
                <>
                    <motion.div
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                        onClick={onClose}
                        className="fixed inset-0 bg-black bg-opacity-50 backdrop-blur-sm z-[1000]"
                    />
                    <motion.div
                        initial={{ x: position === "left" ? "-100%" : "100%" }}
                        animate={{ x: 0 }}
                        exit={{ x: position === "left" ? "-100%" : "100%" }}
                        transition={{ duration: 0.3 }}
                        className="fixed top-0 h-full bg-white z-[2000] p-6 overflow-y-auto shadow-xl"
                        style={{ ...sideStyles, maxWidth, width: "100%" }}
                    >
                        <button onClick={onClose} className="absolute top-4 right-4 font-bold">
                            X
                        </button>
                        {children}
                    </motion.div>
                </>
            )}
        </AnimatePresence>,
        document.body
    );
};

export default SlidingPanel;