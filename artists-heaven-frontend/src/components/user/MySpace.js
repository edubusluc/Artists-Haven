import { useState, useEffect } from "react";
import Logout from "../Logout";
import MyOrders from "./MyOrders";
import UserProfile from "./UserProfile";
import Footer from "../Footer";
import { checkTokenExpiration } from "../../utils/authUtils";
import SessionExpired from "../SessionExpired";
import { useTranslation } from "react-i18next";
import RewardCards from "./RewardCards";
import MyUserProducts from "./MyUserProducts";

const getView = (key) => {
    switch (key) {
        case "profile":
            return <UserProfile />;
        case "orders":
            return <MyOrders />;
        case "rewards":
            return <RewardCards />;
        case "userProducts":
            return <MyUserProducts />;
        default:
            return null;
    }
};

const MySpace = () => {
    const [active, setActive] = useState("profile");
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
    const { t } = useTranslation();

    const menuItems = [
        { key: "profile", label: t('mySpace.profile') },
        { key: "orders", label: t('mySpace.myOrders') },
        { key: "rewards", label: t('mySpace.rewardCards') },
        { key: "userProducts", label: t('mySpace.myProducts') },
    ];


    useEffect(() => {
        window.scrollTo(0, 0);
    }, []);

    if (!checkTokenExpiration()) {
        return <SessionExpired />;
    }

    return (
        <div className="flex flex-col md:flex-row min-h-screen bg-gray-50 text-gray-800">
            {/* Mobile Top Bar */}
            <div className="md:hidden mt-14 p-4 flex justify-between items-center top-0 left-0 right-0 z-50">
                <button
                    className="text-sm px-3 py-1 bg-gray-200 rounded"
                    onClick={() => setMobileMenuOpen(true)}
                >
                    {t('mySpace.mySpace')}
                </button>
            </div>

            {/* Overlay en móvil */}
            {mobileMenuOpen && (
                <div
                    onClick={() => setMobileMenuOpen(false)}
                    className="fixed inset-0 bg-black bg-opacity-40 z-40 md:hidden"
                />
            )}

            {/* Sidebar */}
            <aside
                className={`fixed md:relative top-0 left-0 h-full md:h-auto bg-white shadow-lg p-6 transform transition-transform duration-300 z-50 md:z-auto flex flex-col justify-between
          ${mobileMenuOpen ? "translate-x-0" : "-translate-x-full"}
          md:translate-x-0 md:w-80 w-72`}
            >
                {/* Botón cerrar en móvil */}
                <div className="flex justify-between items-center mb-6 md:hidden">
                    <h3 className="text-lg font-semibold">{t('mySpace.menu')}</h3>
                    <button
                        className="text-gray-600"
                        onClick={() => setMobileMenuOpen(false)}
                    >
                        ✕
                    </button>
                </div>

                <nav className="space-y-2 flex-1 min-h">
                    {menuItems.map((item) => (
                        <button
                            key={item.key}
                            onClick={() => {
                                setActive(item.key);
                                setMobileMenuOpen(false);
                            }}
                            className={`w-full text-left px-4 py-2 mt-10 rounded-lg transition text-sm ${active === item.key
                                ? "bg-black text-white"
                                : "bg-gray-100 hover:bg-gray-200 text-gray-700"
                                }`}
                        >
                            {item.label}
                        </button>
                    ))}
                    <div className="text-white bg-red-600 w-full px-4 py-2 text-left rounded-lg mt-4 text-sm">
                        <Logout />
                    </div>
                </nav>
            </aside>

            {/* Main Content */}
            <main className="flex-1 p-4 md:p-10 md:mt-5">
                <div className="bg-white md:p-6 rounded-xl shadow mb-4 p-4">
                    {getView(active)}
                </div>
                <Footer />
            </main>
        </div>
    );
};

export default MySpace;
