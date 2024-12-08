import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

function UserList() {
  const [users, setUsers] = useState([]);
  const [authToken, setAuthToken] = useState(localStorage.getItem("authToken"));

  useEffect(() => {
    fetch('/api/users/list', {
      headers: {
        'Authorization': `Bearer ${authToken}`
      }
    })
      .then(response => response.json())
      .then(data => setUsers(data))
      .catch(error => console.error('Error fetching users:', error));
  }, [authToken]);

  // Función para hacer la verificación de un artista
  const verifyArtist = (id) => {
    fetch('/api/admin/validate_artist', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`
      },
      body: JSON.stringify({ id }),
    })
    .then(response => {
      if (response.ok) {
        setUsers(users.map(user => 
          user.id === id ? { ...user, role: "ARTIST" } : user
        ));
        console.log("Artitsa verificado")
      } else {
        console.log('Error al verificar al artista');
      }
    })
    .catch(error => {
      console.error('Error:', error);
    });
  };

  // Comprobar si el usuario tiene el rol ADMIN
  const userRole = localStorage.getItem("role");

  return (
    <div>
      <h1>Lista de Usuarios</h1>
      <h2>Estos son los usuarios creados hasta el momento:</h2>
      <ul>
        {users.map(user => (
          <li key={user.id}>
            {user.firstName} {user.lastName} - {user.role}
            {userRole === "ADMIN" && user.role === "ARTIST" && (
              <button onClick={() => verifyArtist(user.id)}>Verificar Artista</button>
            )}
          </li>
        ))}
      </ul>
      <Link to="/">
        <button>Home</button>
      </Link>
    </div>
  );
}

export default UserList;
