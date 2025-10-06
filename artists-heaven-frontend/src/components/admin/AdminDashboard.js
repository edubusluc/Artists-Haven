import Footer from '../Footer';
import { useState, useEffect, useMemo, useCallback } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faBug, faShieldAlt, faExclamationCircle, faLock, faCoffee, faSackDollar, faShirt, faUser, faMusic } from '@fortawesome/free-solid-svg-icons';
import SalesChart from '../charts/SalesChart';
import CountStatusPieChart from '../charts/CountPieChart';
import TopSellingItemsChart from '../charts/TopSellingItemsChart';
import NonAuthorise from '../NonAuthorise';
import { getStatisticsPerYear } from '../../services/adminServices';
import { checkTokenExpiration } from '../../utils/authUtils';
import SessionExpired from '../SessionExpired';
import { useTranslation } from "react-i18next";

const categoryDetails = {
    'FEATURE_REQUEST': { icon: faCoffee, color: 'bg-blue-300', iconColor: 'text-blue-600' },
    'BUG_REPORT': { icon: faBug, color: 'bg-red-300', iconColor: 'text-red-600' },
    'ABUSE_REPORT': { icon: faExclamationCircle, color: 'bg-yellow-300', iconColor: 'text-yellow-600' },
    'ISSUE_REPORT': { icon: faLock, color: 'bg-orange-300', iconColor: 'text-orange-600' },
    'SECURITY_REPORT': { icon: faShieldAlt, color: 'bg-purple-300', iconColor: 'text-purple-600' },
};

const ReportCard = ({ category, count }) => {
        const { icon, color, iconColor } = categoryDetails[category] || {};
        if (!icon) return null;
        return (
            <div className="bg-gray-50 p-4 rounded-lg m-2 flex flex-col md:flex-row items-center justify-between w-full max-w-xs md:max-w-sm shadow-md overflow-hidden">
                <div className={`mr-2 px-3 py-3 rounded-full ${color} w-12 h-12 flex items-center justify-center mb-4 md:mb-0`}>
                    <FontAwesomeIcon icon={icon} className={`${iconColor} text-2xl md:text-3xl`} />
                </div>
                <div className="text-center md:text-left flex-1">
                    <p className="text-sm font-medium text-gray-700 capitalize">{category.replace('_', ' ')}</p>
                    <p className="text-lg md:text-xl font-bold text-gray-900">{count ?? 0}</p>
                </div>
            </div>
        );
    };

const AdminDashboard = () => {
    const currentYear = new Date().getFullYear();
    const role = localStorage.getItem("role");

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

    const yearsOptions = useMemo(() => {
        return Array.from({ length: 6 }, (_, i) => currentYear - i);
    }, [currentYear]);

    const [authToken] = useState(localStorage.getItem("authToken"));
    const { t } = useTranslation();

    const fetchData = useCallback(async () => {
        if (!checkTokenExpiration || role !== 'ADMIN') return;

        const controller = new AbortController();
        try {
            const data = await getStatisticsPerYear(authToken, year);
            setData(prevData => ({
                ...prevData,
                ...data.data,
                incomePerYear: data.data.incomePerYear ?? 0
            }));
        } catch (error) {
            if (error.name !== 'AbortError') {
                console.error('Error fetching statistics:', error);
            }
        }
    }, [authToken, role, year]);

    
    useEffect(() => {
        fetchData();
        return () => fetchData();
    }, [fetchData]);



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

    if (!role || role !== 'ADMIN') {
        return <NonAuthorise />;
    } else if (!checkTokenExpiration()) {
        return <SessionExpired />;
    }
    return (
        <>
            <div className="min-h-screen bg-gradient-to-r from-gray-300 to-white flex flex-col">
                <div className="pl-6 mt-2 flex justify-start items-center space-x-2">
                    <label htmlFor="year-select" className="block text-gray-500 font-medium m-0">
                        {t('adminDashboard.year')}
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
                        <MetricCard icon={faShirt} value={data.numOrders} title={t('adminDashboard.totalSales')} iconColor="text-blue-600" bgColor="bg-blue-300" />
                        <MetricCard icon={faSackDollar} value={`${data.incomePerYear} €`} title={t('adminDashboard.totalIncome')} iconColor="text-green-600" bgColor="bg-green-300" />
                        <MetricCard icon={faUser} value={data.numUsers} title={t('adminDashboard.usersRegister')} iconColor="text-orange-600" bgColor="bg-orange-300" />
                        <MetricCard icon={faMusic} value={data.numArtists} title={t('adminDashboard.ArtistsRegister')} iconColor="text-yellow-600" bgColor="bg-yellow-300" />
                    </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 p-6">
                    <div className="bg-white shadow-lg rounded-lg p-6">
                        <p className='text-2xl'>{t('adminDashboard.salesSummary')}</p>
                        <p className="text-sm text-gray-400">{t('adminDashboard.yearlySalesReport')}</p>
                        <div className="flex items-end rounded-lg p-4 h-auto">

                            {data.numOrders > 0 ? (
                                <SalesChart year={year} />
                            ) : (
                                <p className="text-gray-400 italic text-center w-full">{t('adminDashboard.noDataAvailable')}</p>
                            )}
                        </div>
                    </div>

                    <div className="grid-cols-1 rounded-lg">
                        <div className='bg-white shadow-lg rounded-lg p-6'>
                            <p className='text-2xl'>{t('adminDashboard.reportSummary')}</p>
                            <p className="text-sm text-gray-400">{t('adminDashboard.yearlyReportSummary')}</p>
                            <div className='flex'>
                                {Object.keys(categoryDetails).map((category) => (
                                    <ReportCard key={category} category={category} count={data.emailCounts[category]} />
                                ))}
                            </div>
                        </div>
                        <div className="flex flex-col md:flex-row gap-4">
                            <div className="bg-white shadow-lg rounded-lg p-6 mt-2 flex-1">
                                <p className="text-lg font-semibold text-gray-600 mb-4">{t('adminDashboard.orderStatusSummary')}</p>
                                {data.orderStatusCounts && Object.keys(data.orderStatusCounts).length > 0 ? (
                                    <CountStatusPieChart orderStatusCounts={data.orderStatusCounts} />
                                ) : (
                                    <p className="text-gray-400 italic text-center">{t('adminDashboard.noDataAvailable')}</p>
                                )}
                            </div>
                            <div className="bg-white shadow-lg rounded-lg p-6 mt-2 flex-1">
                                <p className="text-lg font-semibold text-gray-600 mb-4">{t('adminDashboard.summaryVerifications')}</p>
                                {data.verificationStatusCounts && Object.keys(data.verificationStatusCounts).length > 0 ? (
                                    <CountStatusPieChart orderStatusCounts={data.verificationStatusCounts} />
                                ) : (
                                    <p className="text-gray-400 italic text-center">{t('adminDashboard.noDataAvailable')}</p>
                                )}
                            </div>
                        </div>
                    </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 p-6">
                    <div className="bg-gray-50 rounded-lg p-4 flex flex-col">
                        <h2 className="text-lg font-semibold text-gray-600 mb-2">{t('adminDashboard.bestSellingProducts')}</h2>
                        <TopSellingItemsChart orderItemCount={data.orderItemCount} />
                    </div>
                    <div className="bg-gray-50 rounded-lg p-4 flex flex-col">
                        <h2 className="text-lg font-semibold text-gray-600 mb-2">{t('adminDashboard.categoriesBestSellers')}</h2>
                        <TopSellingItemsChart orderItemCount={data.categoryItemCount} />
                    </div>
                    <div className="bg-gray-50 rounded-lg p-4 flex flex-col">
                        <h2 className="text-lg font-semibold text-gray-600 mb-2">{t('adminDashboard.countryBestSelling')}</h2>
                        <TopSellingItemsChart orderItemCount={data.mostCountrySold} />
                    </div>
                </div>

            </div>
            <Footer />
        </>
    );
}

export default AdminDashboard;
