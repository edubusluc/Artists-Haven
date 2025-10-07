import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

const RewardCards = () => {
    const [points, setPoints] = useState(0);
    const [cards, setCards] = useState([]);
    const [loading, setLoading] = useState(true);
    const { t, i18n } = useTranslation();
    const lang = i18n.language;
    const token = localStorage.getItem("authToken");

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            // ✅ Obtener puntos del usuario
            const userRes = await fetch("http://localhost:8080/api/reward-cards/me", {
                headers: { 'Authorization': `Bearer ${token}` },
            });
            const userJson = await userRes.json();
            const userData = userJson.data;

            //✅ Obtener reward cards
            const cardsRes = await fetch("http://localhost:8080/api/reward-cards/my", {
                headers: { 'Authorization': `Bearer ${token}` },
            });
            const cardsJson = await cardsRes.json();
            const cardsData = cardsJson.data;

            setPoints(userData);
            setCards(cardsData);
        } catch (err) {
            console.error("Error fetching data", err);
        } finally {
            setLoading(false);
        }
    };


    const redeemCard = async (requiredPoints) => {
        try {
            const response = await fetch(`http://localhost:8080/api/reward-cards/redeem?lang=${lang}`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`
                },
                body: JSON.stringify({ requiredPoints }),
            });

            const result = await response.json();
            const errorMessage = result.message;

            if (!response.ok) throw new Error(errorMessage);

            // 🔄 refrescar datos
            fetchData();
            window.location.href = "/users/mySpace";
        } catch (err) {
            alert(err.message);
        }
    };

    if (loading) {
        return <p>{t("loading")}...</p>;
    }

    return (
        <div className="space-y-6">
            {/* Puntos actuales */}
            <div className="p-4 bg-gray-100 rounded-xl shadow">
                <h2 className="text-xl font-semibold">{t("mySpace.pointsBalance")}</h2>
                <p className="text-2xl font-bold text-black">{points} pts</p>
            </div>

            {/* Opciones de canje */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {/* Card 500 pts */}
                <div className="p-4 bg-white border rounded-xl shadow flex flex-col justify-between">
                    <div>
                        <h3 className="text-lg font-semibold">500 pts → 10% OFF</h3>
                        <p className="text-sm text-gray-600 mb-4">
                            {t("mySpace.nextOrderDiscount")}
                        </p>
                    </div>
                    <button
                        disabled={points < 500}
                        onClick={() => redeemCard(500)}
                        className={`px-4 py-2 rounded-lg text-sm font-medium ${points >= 500
                            ? "bg-black text-white hover:bg-gray-800"
                            : "bg-gray-300 text-gray-500 cursor-not-allowed"
                            }`}
                    >
                        {points >= 500 ? t("mySpace.redeemNow") : t("mySpace.notEnough")}
                    </button>
                </div>

                {/* Card 950 pts */}
                <div className="p-4 bg-white border rounded-xl shadow flex flex-col justify-between">
                    <div>
                        <h3 className="text-lg font-semibold">950 pts → 15% OFF</h3>
                        <p className="text-sm text-gray-600 mb-4">
                            {t("mySpace.nextOrderDiscount")}
                        </p>
                    </div>
                    <button
                        disabled={points < 950}
                        onClick={() => redeemCard(950)}
                        className={`px-4 py-2 rounded-lg text-sm font-medium ${points >= 950
                            ? "bg-black text-white hover:bg-gray-800"
                            : "bg-gray-300 text-gray-500 cursor-not-allowed"
                            }`}
                    >
                        {points >= 950 ? t("mySpace.redeemNow") : t("mySpace.notEnough")}
                    </button>
                </div>
            </div>

            {/* Cards ya canjeadas */}
            <div className="p-4 bg-white border rounded-xl shadow">
                <h3 className="text-lg font-semibold mb-2">{t("mySpace.myCards")}</h3>
                {cards.length === 0 ? (
                    <p className="text-gray-500">{t("mySpace.noCards")}</p>
                ) : (
                    <ul className="space-y-2">
                        {cards
                            .sort((a, b) => (a.redeemed ? 1 : 0) - (b.redeemed ? 1 : 0))
                            .map((c) => (
                                <li
                                    key={c.id}
                                    className={`p-3 rounded-lg border ${c.redeemed ? "bg-gray-100 text-gray-500" : "bg-green-50"
                                        }`}
                                >
                                    {c.discountPercentage}% OFF –{" "}
                                    {c.redeemed ? t("mySpace.used") : t("mySpace.active")}
                                </li>
                            ))}
                    </ul>
                )}
            </div>
        </div>
    );
};

export default RewardCards;
