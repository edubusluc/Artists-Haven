import Footer from '../Footer';
import { useState, useEffect } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCalendar, faBackward, faCheck, faXmark, faClock, faQuestion } from '@fortawesome/free-solid-svg-icons';
import SalesChart from '../charts/SalesChart';
import TopSellingItemsChart from '../charts/TopSellingItemsChart';
import NonAuthorise from '../NonAuthorise';
import { Link } from 'react-router-dom';
import { getArtistDashboardStatistics } from '../../services/artistServices';
import { checkTokenExpiration } from '../../utils/authUtils';
import SessionExpired from '../SessionExpired';
import { useTranslation } from 'react-i18next';

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
    const {t} = useTranslation();

    useEffect(() => {
        window.scrollTo(0, 0);
    }, []);

    useEffect(() => {
        if (!checkTokenExpiration || role !== 'ARTIST') return;

        const controller = new AbortController();

        const fetchData = async () => {
            try {
                const response = await getArtistDashboardStatistics(authToken, year)
                setData({
                    ...response.data,
                    futureEvents: response.data.futureEvents ?? 0,
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
            label: t('artistDashboard.verified'),
            iconColor: "text-green-600",
            bgColor: "bg-green-100",
            bgColorCard: "bg-green-400",
            icon: faCheck,
            textColor: "text-green-600",

        },
        ACCEPTED: {
            label: t('artistDashboard.verified'),
            iconColor: "text-green-600",
            bgColor: "bg-green-100",
            bgColorCard: "bg-green-400",
            icon: faCheck,
            textColor: "text-green-600",

        },
        PENDING: {
            label: t('artistDashboard.pending'),
            iconColor: "text-yellow-700",
            bgColor: "bg-yellow-500",
            bgColorCard: "bg-yellow-300",
            icon: faClock,
            textColor: "text-yellow-700",

        },
        REJECTED: {
            label: t('artistDashboard.notVerified'),
            iconColor: "text-red-600",
            bgColor: "bg-red-100",
            bgColorCard: "bg-red-300",
            icon: faXmark,
            textColor: "text-red-600",

        },
        NOT_VERIFIED: {
            label: t('artistDashboard.notVerified'),
            iconColor: "text-red-600",
            bgColor: "bg-red-100",
            bgColorCard: "bg-red-300",
            icon: faXmark,
            textColor: "text-red-600",

        }
    };

    const DEFAULT_STATUS = {
        label: t('artistDashboard.notVerified'),
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



    if (!role || role !== 'ARTIST') {
        return <NonAuthorise />;
    } else if (!checkTokenExpiration()) {
        return <SessionExpired />;
    }

    return (
        <>
            <div className="min-h-screen bg-gradient-to-r from-gray-300 to-white flex flex-col">
                <div className="pl-6 mt-2 flex justify-start items-center space-x-2">
                    <label htmlFor="year-select" className="block text-gray-500 font-medium m-0">
                       {t('artistDashboard.year')}:
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
                            title={t('artistDashboard.verification')}
                            iconColor={getIconColor(data.isVerificated)}
                            bgColor={getBgColor(data.isVerificated)}
                            bgColorCard={getBgColorCard(data.isVerificated)}
                            textColor={getTextColor(data.isVerificated)} />
                        <MetricCard icon={faCalendar} value={data.futureEvents} title={t('artistDashboard.nextEvents')} iconColor="text-green-600" bgColor="bg-green-300" />
                        <MetricCard icon={faBackward} value={data.pastEvents} title={t('artistDashboard.pastEvents')} iconColor="text-orange-600" bgColor="bg-orange-300" />
                    </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 p-6">
                    <div className="bg-white shadow-lg rounded-lg p-6">
                        <p className='text-2xl'>{t('artistDashboard.salesSummaryArtist')}</p>
                        <p className="text-sm text-gray-400 mb-5">{t('artistDashboard.yearlySalesReport')}</p>
                        <div className="p-4">
                            {Object.keys(data.orderItemCount).length > 0 ? (
                                <SalesChart year={year} />
                            ) : (
                                <p className="text-gray-400 italic text-center w-full">{t('artistDashboard.noDataAvailable')}</p>
                            )}
                        </div>
                    </div>

                    <div className="grid-cols-1 rounded-lg">
                        <div className='bg-white shadow-lg rounded-lg p-6'>
                            <p className='text-2xl'>{t('artistDashboard.realVerficationStatus')}</p>

                            {!["PENDING", "ACCEPTED", "Verified"].includes(data.isVerificated) ? (
                                <Link to="/verification">
                                    <button
                                        className="w-full md:w-auto bg-yellow-400 text-black font-semibold py-2 px-6 rounded-md shadow-md transition duration-300 ease-in-out hover:bg-yellow-500 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-yellow-300"
                                    >{t('artistDashboard.sendVerification')}</button>
                                </Link>
                            ) : (
                                <p className="text-sm text-green-600">{t('artistDashboard.verificationStatus')}</p>
                            )}
                        </div>
                        <div className="flex flex-col md:flex-row md:flex-wrap gap-4 mt-2">
                            <div className="bg-white shadow-lg rounded-lg p-6 flex-1 min-w-[300px]">
                                <h2 className="text-lg font-semibold text-gray-600 mb-2">{t('artistDashboard.bestSellingProduct')}</h2>
                                <TopSellingItemsChart orderItemCount={data.orderItemCount} />
                            </div>
                            <div className="bg-white shadow-lg rounded-lg p-6 flex-1 min-w-[300px]">
                                <h2 className="text-lg font-semibold text-gray-600 mb-2">{t('artistDashboard.bestSellingCountry')}</h2>
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