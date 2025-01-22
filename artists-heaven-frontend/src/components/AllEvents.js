import React, { useState, useEffect } from 'react';
import { useNavigate } from "react-router-dom";

const AllEvents = () => {
    const [events, setEvents] = useState([]);

        const fetchEvents = async () => {
            try {
                const response = await fetch('/api/event/allEvents', {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json'
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

        const EventItem = ({ event}) => (
            <li>
                <h2>{event.name}</h2>
                <p>{event.description}</p>
                <p> <strong>Ubicación: </strong>{event.location}</p>
                <p> <strong>Más Información: </strong>{event.moreInfo}</p>
                <img src={`/api/event${event.image}`} alt={event.name} style={{ width: "150px", height: "150px", objectFit: "cover" }}/>
            </li>
        );

        return (
            <div>
                <h1>Eventos Disponibles</h1>
                <ul>
                    {events.map(event => (
                        <EventItem key={event.id} event={event} />
                    ))}
                </ul>
            </div>
        );

};

export default AllEvents;