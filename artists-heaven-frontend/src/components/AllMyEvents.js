import React, { useState, useEffect } from 'react';
import { useNavigate } from "react-router-dom";

const AllMyEvents = () => {
    const [events, setEvents] = useState([]);
    const navigate = useNavigate();
    const token = localStorage.getItem("authToken");

    const fetchEvents = async () => {
        try {
            const response = await fetch('/api/event/allMyEvents', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                throw new Error('Error al obtener los eventos');
            }

            const data = await response.json();
            setEvents(data);
        } catch (error) {
            console.error(error.message);
        }
    };

    useEffect(() => {
        fetchEvents();
    }, []);

    const handleDelete = async (id) => {
        try {
            const response = await fetch(`/api/event/delete/${id}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                throw new Error('Error al eliminar el evento');
            }

            setEvents(events.filter(event => event.id !== id));
        } catch (error) {
            console.error('Error:', error);
        }
    };


    return (
        <div>
            <h1>Mis Eventos</h1>
            <ul>
                {events.map(event => (
                    <EventItem key={event.id} event={event} handleDelete={handleDelete} />
                ))}
            </ul>
        </div>
    );
};

const EventItem = ({ event, handleDelete }) => (
    <li>
        <h2>{event.name}</h2>
        <p>{event.description}</p>
        <p>{event.location}</p>
        <p>{event.moreInfo}</p>
        <button onClick={() => handleDelete(event.id)} className="btn btn-primary">Eliminar Evento</button>
    </li>
);

export default AllMyEvents;