import { useState } from "react";
import Logout from "../Logout";
import MyOrders from "./MyOrders";
import UserProfile from "./UserProfile";
import Footer from "../Footer";

const menuItems = [
    { key: "profile", label: "Profile" },
    { key: "orders", label: "My Order" },
    { key: "password", label: "Change Password" },
];

const getView = (key) => {
    switch (key) {
        case "profile":
            return <UserProfile />;
        case "orders":
            return <MyOrders />;
        case "password":
            return <div>🔒 Change Password View</div>;
        default:
            return null;
    }
};

const MySpace = () => {
    const [active, setActive] = useState("profile");
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

    return (
        <div className="flex flex-col md:flex-row min-h-screen bg-gray-50 text-gray-800">
            {/* Mobile Top Menu */}
            <div className="md:hidden bg-white p-4 shadow flex justify-between items-center">
                <h2 className="text-lg font-semibold">USER SPACE</h2>
                <button
                    className="text-sm px-3 py-1 bg-gray-200 rounded"
                    onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                >
                    {mobileMenuOpen ? "Close" : "Menu"}
                </button>
            </div>

            {/* Sidebar */}
            <aside className={`md:w-80 w-full bg-white shadow p-4 md:p-6 flex-col justify-between 
                ${mobileMenuOpen ? "flex" : "hidden"} md:flex transition`}>
                <nav className="space-y-2">
                    {menuItems.map((item) => (
                        <button
                            key={item.key}
                            onClick={() => {
                                setActive(item.key);
                                setMobileMenuOpen(false);
                            }}
                            className={`w-full text-left px-4 py-2 rounded-lg transition text-sm ${
                                active === item.key
                                    ? "bg-black text-white"
                                    : "bg-gray-100 hover:bg-gray-200 text-gray-700"
                            }`}
                        >
                            {item.label}
                        </button>
                    ))}
                    <div className="text-white bg-red-600 w-full px-4 py-2 text-left rounded-lg mt-4">
                        <Logout />
                    </div>
                </nav>
            </aside>

            {/* Main Content */}
            <main className="flex-1 p-4 md:p-10 bg-gray-100">
                <div className="bg-white p-4 md:p-6 rounded-xl shadow mb-4">
                    {getView(active)}
                </div>
                <Footer/>
            </main>
        </div>
    );
};

export default MySpace;
