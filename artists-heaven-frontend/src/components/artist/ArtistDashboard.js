import Footer from '../Footer';
import { useState, useEffect } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCalendar, faBackward, faCheck, faXmark, faClock, faQuestion } from '@fortawesome/free-solid-svg-icons';
import SalesChart from '../charts/SalesChart';
import TopSellingItemsChart from '../charts/TopSellingItemsChart';
import NonAuthorise from '../NonAuthorise';
import { Link } from 'react-router-dom';
import { getArtistDashboardStatistics } from '../../services/artistServices';

const ArtistDashboard = () => {
    const currentYear = new Date().getFullYear();
    const role = localStorage.getItem("role");

    const yearsOptions = [];
    for (let y = currentYear; y >= currentYear - 5; y--) {
        yearsOptions.push(y);
    }

    const [year, setYear] = useState(currentYear);
    const [data, setData] = useState({
        isVerificated: false,
        futureEvents: 0,
        pastEvents: 0,
        orderItemCount: {},
        mostCountrySold: {},
    });

    const [authToken] = useState(localStorage.getItem("authToken"));

    useEffect(() => {
        window.scrollTo(0, 0);
    }, []);

    console.log(data)

    useEffect(() => {
        if (!authToken || role !== 'ARTIST') return;

        const controller = new AbortController();

        const fetchData = async () => {
            try {
                const data = await getArtistDashboardStatistics(authToken, year)
                setData({
                    ...data,
                    futureEvents: data.futureEvents ?? 0,
                });
            } catch (error) {
                if (error.name !== 'AbortError') {
                    console.error(error);
                }
            }
        };
        fetchData();
        return () => {
            controller.abort();
        };
    }, [authToken, year]);

    const MetricCard = ({ icon, value, title, iconColor, bgColor, bgColorCard, textColor }) => (
        <div
            className={`flex-1 shadow-lg rounded-lg p-4 m-2 flex items-center 
        ${bgColorCard || 'bg-white'}`}
        >
            <div className={`flex justify-center items-center m-4 w-12 h-12 rounded-full ${bgColor || 'bg-gray-100'}`}>
                <FontAwesomeIcon icon={icon} className={`${iconColor || 'text-gray-500'} text-2xl`} />
            </div>
            <div>
                <p className={`text-3xl font-bold ${textColor || 'text-indigo-600'}`}>{value}</p>
                <p className={`text-sm font-semibold ${textColor ? textColor : 'text-gray-400'}`}>{title}</p>
            </div>
        </div>
    );


    const STATUS_MAP = {
        VERIFIED: {
            label: "Verificado",
            iconColor: "text-green-600",
            bgColor: "bg-green-100",
            bgColorCard: "bg-green-400",
            icon: faCheck,
            textColor: "text-green-600",

        },
        ACCEPTED: {
            label: "Verificado",
            iconColor: "text-green-600",
            bgColor: "bg-green-100",
            bgColorCard: "bg-green-400",
            icon: faCheck,
            textColor: "text-green-600",

        },
        PENDING: {
            label: "Pendiente",
            iconColor: "text-yellow-700",
            bgColor: "bg-yellow-500",
            bgColorCard: "bg-yellow-300",
            icon: faClock,
            textColor: "text-yellow-700",

        },
        REJECTED: {
            label: "No verificado",
            iconColor: "text-red-600",
            bgColor: "bg-red-100",
            bgColorCard: "bg-red-300",
            icon: faXmark,
            textColor: "text-red-600",

        },
        NOT_VERIFIED: {
            label: "No verificado",
            iconColor: "text-red-600",
            bgColor: "bg-red-100",
            bgColorCard: "bg-red-300",
            icon: faXmark,
            textColor: "text-red-600",

        }
    };

    const DEFAULT_STATUS = {
        label: "No verificado",
        iconColor: "text-red-600",
        bgColor: "bg-red-100",
        bgColorCard: "bg-red-300",
        icon: faXmark,
        textColor: "text-red-600",

    };

    const getStatusProps = (status) => {
        const statusStr = typeof status === 'string' ? status.toUpperCase() : 'NOT_VERIFIED';
        return STATUS_MAP[statusStr] || DEFAULT_STATUS;
    };

    const getLabel = (status) => getStatusProps(status).label;
    const getIconColor = (status) => getStatusProps(status).iconColor;
    const getBgColor = (status) => getStatusProps(status).bgColor;
    const getBgColorCard = (status) => getStatusProps(status).bgColorCard;
    const getIcon = (status) => getStatusProps(status).icon;
    const getTextColor = (status) => getStatusProps(status).textColor;




    if (role !== 'ARTIST') {
        return <NonAuthorise />;
    }

    return (
        <>
            <div className="min-h-screen bg-gradient-to-r from-gray-300 to-white flex flex-col">
                <div className="pl-6 mt-2 flex justify-start items-center space-x-2">
                    <label htmlFor="year-select" className="block text-gray-500 font-medium m-0">
                        Año:
                    </label>
                    <select
                        id="year-select"
                        value={year}
                        onChange={(e) => setYear(Number(e.target.value))}
                        className="border border-gray-200 bg-white rounded p-1 text-sm text-gray-600 focus:outline-none focus:ring-0"
                    >
                        {yearsOptions.map((y) => (
                            <option key={y} value={y}>{y}</option>
                        ))}
                    </select>
                </div>
                <div className="m-4 flex flex-wrap justify-between items-center">
                    {/* Métricas */}
                    <div className="flex flex-wrap justify-between flex-1">
                        <MetricCard
                            icon={getIcon(data.isVerificated)}
                            value={getLabel(data.isVerificated)}
                            title="Verificación"
                            iconColor={getIconColor(data.isVerificated)}
                            bgColor={getBgColor(data.isVerificated)}
                            bgColorCard={getBgColorCard(data.isVerificated)}
                            textColor={getTextColor(data.isVerificated)} />
                        <MetricCard icon={faCalendar} value={data.futureEvents} title="Próximos eventos" iconColor="text-green-600" bgColor="bg-green-300" />
                        <MetricCard icon={faBackward} value={data.pastEvents} title="Eventos realizados" iconColor="text-orange-600" bgColor="bg-orange-300" />
                    </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 p-6">
                    <div className="bg-white shadow-lg rounded-lg p-6">
                        <p className='text-2xl'>Sales Summary Artist</p>
                        <p className="text-sm text-gray-400 mb-5">Yearly Sales Report</p>
                        <div className="p-4">
                            {Object.keys(data.orderItemCount).length > 0 ? (
                                <SalesChart year={year} />
                            ) : (
                                <p className="text-gray-400 italic text-center w-full">No hay datos disponibles</p>
                            )}
                        </div>
                    </div>

                    <div className="grid-cols-1 rounded-lg">
                        <div className='bg-white shadow-lg rounded-lg p-6'>
                            <p className='text-2xl'>Estado de la verificación</p>

                            {!["PENDING", "ACCEPTED"].includes(data.isVerificated) ? (
                                <Link to="/verification">
                                    <button
                                        className="w-full md:w-auto bg-yellow-400 text-black font-semibold py-2 px-6 rounded-md shadow-md transition duration-300 ease-in-out hover:bg-yellow-500 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-yellow-300"
                                    >Send Verification Request</button>
                                </Link>
                            ) : (
                                <p className="text-sm text-green-600">Verificación en proceso o ya aceptada</p>
                            )}
                        </div>
                        <div className="flex flex-col md:flex-row gap-4">
                            <div className="bg-white shadow-lg rounded-lg p-6 mt-2 flex-1">
                                <h2 className="text-lg font-semibold text-gray-600 mb-2">Productos Más Vendidos</h2>
                                <TopSellingItemsChart orderItemCount={data.orderItemCount} />
                            </div>
                            <div className="bg-white shadow-lg rounded-lg p-6 mt-2 flex-1">
                                <h2 className="text-lg font-semibold text-gray-600 mb-2">Países Más Vendidos</h2>
                                <TopSellingItemsChart orderItemCount={data.mostCountrySold} />
                            </div>
                        </div>
                    </div>
                </div>

            </div>
            <Footer />
        </>
    );
}

export default ArtistDashboard;