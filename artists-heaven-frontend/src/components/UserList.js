import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

function UserList() {
  const [users, setUsers] = useState([]);

  useEffect(() => {
    fetch('/api/users/list')
      .then(response => response.json())
      .then(data => setUsers(data))
      .catch(error => console.error('Error fetching users:', error));
  }, []);

  return (
    <div>
      <h1>Lista de Usuarios</h1>
      <h2>Estos son los usuarios creados hasta el momento:</h2>
      <ul>
        {users.map(user => (
          <li key={user.id}>{user.firstName} {user.lastName} - {user.role}</li>
        ))}
      </ul>
      <Link to="/">
        <button>Home</button>
      </Link>
    </div>
  );
}

export default UserList;
