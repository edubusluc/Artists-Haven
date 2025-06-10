import React from 'react';
import { Bar } from 'react-chartjs-2';
import {
    Chart as ChartJS,
    BarElement,
    CategoryScale,
    LinearScale,
    Tooltip,
    Legend,
    Title,
} from 'chart.js';

ChartJS.register(BarElement, CategoryScale, LinearScale, Tooltip, Legend, Title);

const TopSellingItemsChart = ({ orderItemCount }) => {
    if (!orderItemCount || Object.keys(orderItemCount).length === 0) {
        return <p className="text-gray-500">No hay datos de productos vendidos.</p>;
    }

    const labels = Object.keys(orderItemCount);
    const values = Object.values(orderItemCount);

    const data = {
        labels,
        datasets: [
            {
                label: 'Cantidad Vendida',
                data: values,
                backgroundColor: 'rgba(59, 130, 246, 0.6)', // Indigo 500
                borderColor: 'rgba(37, 99, 235, 1)',
                borderWidth: 1,
                borderRadius: 6,
                barPercentage: 0.6,
            },
        ],
    };

    const options = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                display: false,
            },
            tooltip: {
                backgroundColor: '#1f2937', // gray-800
                titleColor: '#fff',
                bodyColor: '#d1d5db', // gray-300
                borderColor: '#4b5563', // gray-600
                borderWidth: 1,
                cornerRadius: 4,
                padding: 10,
            },
        },
        scales: {
            x: {
                ticks: {
                    color: '#6b7280', // gray-500
                    font: {
                        weight: '500',
                    },
                },
                grid: {
                    display: false,
                },
            },
            y: {
                beginAtZero: true,
                ticks: {
                    color: '#6b7280',
                    stepSize: 1,
                },
                grid: {
                    color: '#e5e7eb', // gray-200
                    borderDash: [4, 4],
                },
            },
        },
    };

    return (
        <div className="w-full h-96">
            <Bar data={data} options={options} />
        </div>
    );
};

export default TopSellingItemsChart;
