import React, { useEffect, useState } from 'react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const SalesChart = ({ year }) => {
    const [salesData, setSalesData] = useState([]);
    const [authToken] = useState(localStorage.getItem("authToken"));

    const months = [
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    ];

    const role = localStorage.getItem("role")?.toLowerCase();
    const rolePath = role === "artist" ? "artists" : role;

    useEffect(() => {
        fetch(`/api/${rolePath}/sales/monthly?year=${year}`, {
            method: "GET",
            headers: {
                'Authorization': `Bearer ${authToken}`,
            },
        })
            .then(response => response.json())
            .then(data => {
                const mappedData = data.map(item => ({
                    ...item,
                    month: months[item.month - 1]
                }));
                setSalesData(mappedData);
            })
            .catch(error => {
                console.error("Hubo un error al obtener los datos", error);
            });
    }, [year, authToken]);

    console.log(salesData)

    return (
        <ResponsiveContainer width="100%" height={400}>
            <AreaChart data={salesData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
                <defs>
                    <linearGradient id="colorOrders" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#8884d8" stopOpacity={0.8} />
                        <stop offset="95%" stopColor="#8884d8" stopOpacity={0} />
                    </linearGradient>
                    <linearGradient id="colorRevenue" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#82ca9d" stopOpacity={0.8} />
                        <stop offset="95%" stopColor="#82ca9d" stopOpacity={0} />
                    </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="month" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Area type="monotone" dataKey="totalOrders" name="Ventas Realizadas" stroke="#8884d8" fillOpacity={1} fill="url(#colorOrders)" />
                {role === 'admin' && (
                    <Area
                        type="monotone"
                        dataKey="totalRevenue"
                        name="Ingresos Obtenidos"
                        stroke="#82ca9d"
                        fillOpacity={1}
                        fill="url(#colorRevenue)"
                    />
                )}

            </AreaChart>
        </ResponsiveContainer>
    );
};

export default SalesChart;
