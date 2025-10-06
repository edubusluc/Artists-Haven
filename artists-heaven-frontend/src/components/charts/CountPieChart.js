import React from 'react';
import { PieChart, Pie, Tooltip, Cell, Legend, ResponsiveContainer, Label } from 'recharts';

// Componente para mostrar el gráfico de tarta
const OrderStatusPieChart = ({ orderStatusCounts }) => {
    // Convertimos los datos de orderStatusCounts a un formato adecuado para PieChart
    const data = Object.entries(orderStatusCounts).map(([status, count]) => ({
        name: status,
        value: count,
    }));

    // Colores más suaves y modernos con gradientes
    const COLORS = ['#4BC0C0', '#FF6F61', '#6B5B95', '#F2C94C', '#F0A202', '#38A3A5'];

    return (
        <ResponsiveContainer width="100%" height={300}>
            <PieChart>
                <Pie
                    data={data}
                    dataKey="value"
                    nameKey="name"
                    outerRadius={120}
                    innerRadius={50}
                    fill="#8884d8"
                    labelLine={false}
                    isAnimationActive={true}
                    animationDuration={1000} // Animación más fluida
                    startAngle={90}
                    endAngle={450}
                >
                    {data.map((entry, index) => (
                        <Cell
                            key={`cell-${index}`}
                            fill={COLORS[index % COLORS.length]}
                            strokeWidth={3}
                            stroke="#fff" // Bordes blancos entre secciones
                        />
                    ))}
                </Pie>
                {/* Tooltip personalizado */}
                <Tooltip
                    contentStyle={{
                        backgroundColor: 'white',
                        borderRadius: '8px',
                        color: '#fff',
                        fontSize: '14px',
                        padding: '5px',
                        justifyContent: 'center',  // Centra el contenido horizontalmente
                        alignItems: 'center',      // Centra el contenido verticalmente
                        textAlign: 'center',
                    }}
                    formatter={(value, name, props) => [
                        value.toLocaleString(),
                        `Estado: ${props.name}`,
                    ]}
                />
                {/* Leyenda con estilo mejorado */}
                <Legend
                    verticalAlign="top"
                    align="center"
                    iconSize={10}
                    iconType="circle"
                    wrapperStyle={{
                        fontSize: '14px',
                        paddingTop: '10px',
                        color: '#333',
                        fontFamily: "'Arial', sans-serif",
                        fontWeight: 'bold',
                    }}
                />
            </PieChart>
        </ResponsiveContainer>
    );
};

export default OrderStatusPieChart;
