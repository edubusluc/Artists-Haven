import Footer from '../Footer';
import { useState, useEffect } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faBug, faEnvelope, faShieldAlt, faExclamationCircle, faLock, faCoffee, faSackDollar, faShirt, faUser, faMusic } from '@fortawesome/free-solid-svg-icons';
import SalesChart from '../charts/SalesChart';
import CountStatusPieChart from '../charts/CountPieChart';
import TopSellingItemsChart from '../charts/TopSellingItemsChart';
import NonAuthorise from '../NonAuthorise';

const AdminDashboard = () => {
    const currentYear = new Date().getFullYear();
    const role = localStorage.getItem("role");

    const yearsOptions = [];
    for (let y = currentYear; y >= currentYear - 5; y--) {
        yearsOptions.push(y);
    }

    const [year, setYear] = useState(currentYear);
    const [data, setData] = useState({
        numOrders: 0,
        incomePerYear: 0,
        emailCounts: {},
        numUsers: 0,
        numArtists: 0,
        orderStatusCounts: {},
        verificationStatusCounts: {},
        orderItemCount: {},
        categoryItemCount: {},
        mostCountrySold: {}
    });

    const [authToken] = useState(localStorage.getItem("authToken"));

    useEffect(() => {
        if (!authToken || role !== 'ADMIN') return;

        const controller = new AbortController();

        const fetchData = async () => {
            try {
                const response = await fetch(`/api/admin/staticsPerYear?year=${year}`, {
                    method: "GET",
                    headers: {
                        'Authorization': `Bearer ${authToken}`,
                    },
                    signal: controller.signal,
                });
                if (!response.ok) throw new Error('Error al obtener datos');
                const data = await response.json();
                setData({
                    ...data,
                    incomePerYear: data.incomePerYear ?? 0,
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

    const categoryDetails = {
        'FEATURE_REQUEST': { icon: faCoffee, color: 'bg-blue-300', iconColor: 'text-blue-600' },
        'BUG_REPORT': { icon: faBug, color: 'bg-red-300', iconColor: 'text-red-600' },
        'ABUSE_REPORT': { icon: faExclamationCircle, color: 'bg-yellow-300', iconColor: 'text-yellow-600' },
        'ISSUE_REPORT': { icon: faLock, color: 'bg-orange-300', iconColor: 'text-orange-600' },
        'SECURITY_REPORT': { icon: faShieldAlt, color: 'bg-purple-300', iconColor: 'text-purple-600' },
    };

    const MetricCard = ({ icon, value, title, iconColor, bgColor }) => (
        <div className="flex-1 bg-white shadow-lg rounded-lg p-4 m-2 flex items-center">
            <div className={`flex items-center m-4 px-4 py-4 rounded-full ${bgColor}`}>
                <FontAwesomeIcon icon={icon} className={`${iconColor} text-2xl`} />
            </div>
            <div>
                <p className="text-3xl font-bold text-indigo-600">{value}</p>
                <p className="text-sm font-semibold text-gray-400">{title}</p>
            </div>
        </div>
    );

    const ReportCard = ({ category, count }) => {
        const { icon, color, iconColor } = categoryDetails[category] || {};
        return (
            icon ? (
                <div className="bg-gray-50 p-4 rounded-lg m-2 flex flex-col md:flex-row items-center justify-between w-full max-w-xs md:max-w-sm shadow-md overflow-hidden">
                    <div className={`mr-2 px-3 py-3 rounded-full ${color} w-12 h-12 flex items-center justify-center mb-4 md:mb-0`}>
                        <FontAwesomeIcon icon={icon} className={`${iconColor} text-2xl md:text-3xl`} />
                    </div>
                    <div className="text-center md:text-left flex-1">
                        <p className="text-sm font-medium text-gray-700 capitalize">{category.replace('_', ' ')}</p>
                        <p className="text-lg md:text-xl font-bold text-gray-900">{count ?? 0}</p>
                    </div>
                </div>
            ) : null
        );
    };

    if (role !== 'ADMIN') {
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
                        <MetricCard icon={faShirt} value={data.numOrders} title="Ventas Totales" iconColor="text-blue-600" bgColor="bg-blue-300" />
                        <MetricCard icon={faSackDollar} value={`${data.incomePerYear} €`} title="Total de Ingresos" iconColor="text-green-600" bgColor="bg-green-300" />
                        <MetricCard icon={faUser} value={data.numUsers} title="Usuarios Registrados" iconColor="text-orange-600" bgColor="bg-orange-300" />
                        <MetricCard icon={faMusic} value={data.numArtists} title="Artistas Registrados" iconColor="text-yellow-600" bgColor="bg-yellow-300" />
                    </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 p-6">
                    <div className="bg-white shadow-lg rounded-lg p-6">
                        <p className='text-2xl'>Sales Summary</p>
                        <p className="text-sm text-gray-400">Yearly Sales Report</p>
                        <div className="flex items-end rounded-lg p-4 h-auto">

                            {data.numOrders > 0 ? (
                                <SalesChart year={year} />
                            ) : (
                                <p className="text-gray-400 italic text-center w-full">No hay datos disponibles</p>
                            )}
                        </div>
                    </div>

                    <div className="grid-cols-1 rounded-lg">
                        <div className='bg-white shadow-lg rounded-lg p-6'>
                            <p className='text-2xl'>Reports Summary</p>
                            <p className="text-sm text-gray-400">Yearly Report Summary</p>
                            <div className='flex'>
                                {Object.keys(categoryDetails).map((category) => (
                                    <ReportCard key={category} category={category} count={data.emailCounts[category]} />
                                ))}
                            </div>
                        </div>
                        <div className="flex flex-col md:flex-row gap-4">
                            <div className="bg-white shadow-lg rounded-lg p-6 mt-2 flex-1">
                                <p className="text-lg font-semibold text-gray-600 mb-4">Resumen de Estados de Pedidos</p>
                                {data.orderStatusCounts && Object.keys(data.orderStatusCounts).length > 0 ? (
                                    <CountStatusPieChart orderStatusCounts={data.orderStatusCounts} />
                                ) : (
                                    <p className="text-gray-400 italic text-center">No hay datos disponibles</p>
                                )}
                            </div>
                            <div className="bg-white shadow-lg rounded-lg p-6 mt-2 flex-1">
                                <p className="text-lg font-semibold text-gray-600 mb-4">Resumen Verificaciones</p>
                                {data.verificationStatusCounts && Object.keys(data.verificationStatusCounts).length > 0 ? (
                                    <CountStatusPieChart orderStatusCounts={data.verificationStatusCounts} />
                                ) : (
                                    <p className="text-gray-400 italic text-center">No hay datos disponibles</p>
                                )}
                            </div>
                        </div>
                    </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 p-6">
                    <div className="bg-gray-50 rounded-lg p-4 flex flex-col">
                        <h2 className="text-lg font-semibold text-gray-600 mb-2">Productos Más Vendidos</h2>
                        <TopSellingItemsChart orderItemCount={data.orderItemCount} />
                    </div>
                    <div className="bg-gray-50 rounded-lg p-4 flex flex-col">
                        <h2 className="text-lg font-semibold text-gray-600 mb-2">Categorías Más Vendidos</h2>
                        <TopSellingItemsChart orderItemCount={data.categoryItemCount} />
                    </div>
                    <div className="bg-gray-50 rounded-lg p-4 flex flex-col">
                        <h2 className="text-lg font-semibold text-gray-600 mb-2">Paises Más Vendidos</h2>
                        <TopSellingItemsChart orderItemCount={data.mostCountrySold} />
                    </div>
                </div>

            </div>
            <Footer />
        </>
    );
}

export default AdminDashboard;
