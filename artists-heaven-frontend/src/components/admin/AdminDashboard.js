import bg from '../../util-image/bg.png';
import Footer from '../Footer';
import React, { useState, useEffect } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faBug, faEnvelope, faShieldAlt, faExclamationCircle, faLock, faCoffee, faSackDollar, faShirt, faUser, faMusic } from '@fortawesome/free-solid-svg-icons';
import SalesChart from '../charts/SalesChart';
import CountStatusPieChart from '../charts/CountPieChart';

const AdminDashboard = () => {

    const [data, setData] = useState({
        numOrders: 0,
        incomePerYear: 0,
        emailCounts: {},
        numUsers: 0,
        numArtists: 0,
        orderStatusCounts: {},
        vericationStatusCount: {}
    });

    const [authToken] = useState(localStorage.getItem("authToken"));
    const year = new Date().getFullYear();



    useEffect(() => {
        fetch(`/api/admin/staticsPerYear?year=${year}`, {
            method: "GET",
            headers: {
                'Authorization': `Bearer ${authToken}`,
            },
        })
            .then((response) => response.ok ? response.json() : Promise.reject('Error al obtener datos'))
            .then(setData)
            .catch(console.error);
    }, [authToken])
    console.log("DATA", data);
    console.log(authToken)
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
        const { icon, color, iconColor } = categoryDetails[category] || {}; // Default to empty object if category doesn't exist

        return (
            icon ? (
                <div className="bg-gray-50 p-4 rounded-lg m-2 flex flex-col md:flex-row items-center justify-between w-full max-w-xs md:max-w-sm shadow-md overflow-hidden">

                    {/* Icon Container */}
                    <div className={`mr-2 px-3 py-3 rounded-full ${color} w-12 h-12 flex items-center justify-center mb-4 md:mb-0`}>
                        <FontAwesomeIcon icon={icon} className={`${iconColor} text-2xl md:text-3xl`} />
                    </div>

                    {/* Text Container */}
                    <div className="text-center md:text-left flex-1">
                        <p className="text-sm font-medium text-gray-700 capitalize">{category.replace('_', ' ')}</p>
                        <p className="text-lg md:text-xl font-bold text-gray-900">{count ?? 0}</p>
                    </div>
                </div>
            ) : null // Avoid rendering if no valid category
        );
    };

    return (
        <div className="min-h-screen bg-gradient-to-r from-gray-300 to-white flex flex-col">
            <div className="m-4 flex flex-wrap justify-between">
                <MetricCard icon={faShirt} value={data.numOrders} title="Ventas Totales" iconColor="text-blue-600" bgColor="bg-blue-300" />
                <MetricCard icon={faSackDollar} value={`${data.incomePerYear} €`} title="Total de Ingresos" iconColor="text-green-600" bgColor="bg-green-300" />
                <MetricCard icon={faUser} value={data.numUsers} title="Usuarios Registrados" iconColor="text-orange-600" bgColor="bg-orange-300" />
                <MetricCard icon={faMusic} value={data.numArtists} title="Artistas Registrados" iconColor="text-yellow-600" bgColor="bg-yellow-300" />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 p-6">
                <div className="bg-white shadow-lg rounded-lg p-6">
                    <p className='text-2xl'>Sales Summary</p>
                    <p className="text-sm text-gray-400">Yearly Sales Report</p>
                    <div className="flex items-end rounded-lg p-4 h-auto">
                        <SalesChart />
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
                    <div className="flex gap-4">
                        {data.orderStatusCounts && Object.keys(data.orderStatusCounts).length > 0 && (
                            <div className="bg-white shadow-lg rounded-lg p-6 mt-2 flex-1">
                                <p className="text-lg font-semibold text-gray-600">Resumen de Estados de Pedidos</p>
                                <CountStatusPieChart orderStatusCounts={data.orderStatusCounts} />
                            </div>
                        )}
                        {data.verificationStatusCounts && Object.keys(data.verificationStatusCounts).length > 0 && (
                            <div className="bg-white shadow-lg rounded-lg p-6 mt-2 flex-1">
                                <p className="text-lg font-semibold text-gray-600">Resumen Verificaciones</p>
                                <CountStatusPieChart orderStatusCounts={data.verificationStatusCounts} />
                            </div>
                        )}
                    </div>
                </div>

            </div>



            <div className="bg-white shadow-lg rounded-lg p-6 m-6">
                <p className="text-lg font-semibold text-gray-600 mb-4">Gráficos</p>
                <div className="bg-gray-50 rounded-lg p-6">
                    <p className="text-sm text-gray-600">Gráfico de ventas</p>
                    <p className="text-sm text-gray-600">Productos Más Vendidos:</p>
                    <p className="text-sm text-gray-600">Rendimiento por Categorías:</p>
                    <p className="text-sm text-gray-600">Ventas por Ubicación</p>
                </div>
            </div>

            <Footer />
        </div>
    );
}

export default AdminDashboard;
