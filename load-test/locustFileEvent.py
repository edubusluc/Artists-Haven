from locust import HttpUser, task, between
import random

class FutureEventsUser(HttpUser):
    wait_time = between(1, 3)

    def on_start(self):
        self.image_files = [
                "/event_media/DukiCartel.jpg",
                "/event_media/DukiBoombastic.jpg",
                "/event_media/DukiAmeri.jpg",
                "/event_media/DukiBernabeu.jpg" 
            ]
        
        self.artist_ids = [2,3,4,5,6]
        response = self.client.post("/api/auth/login", json={"email": "duki@email.com", "password": "asdasd"})
        self.token = response.json()["token"]

    def _headers(self):
        if self.token:
            return {"Authorization": f"Bearer {self.token}"}
        return {}

    @task
    def get_future_events(self):
        url = "/api/event/allFutureEvents"

        with self.client.get(url, catch_response=True) as response:
            status_code = response.status_code
            try:
                data = response.json()
            except Exception:
                data = None

            if status_code == 200:
                events_count = len(data.get("data", [])) if data else 0
                print(f"Future events retrieved successfully: {events_count} events")
                response.success()
            elif status_code == 204:
                print("No future events found")
                response.success()
            else:
                print(f"Unexpected status code {status_code}")
                print(f"Response: {response.text}")
                response.failure("Failed request")

    @task
    def get_future_events_by_artist(self):
        artist_id = random.choice(self.artist_ids)
        url = f"/api/event/futureEvents/{artist_id}"

        with self.client.get(url, catch_response=True) as response:
            status_code = response.status_code
            try:
                data = response.json()
            except Exception:
                data = None

            if status_code == 200:
                events_count = len(data.get("data", [])) if data else 0
                print(f"Artist {artist_id}: Future events retrieved successfully ({events_count} events)")
                response.success()
            elif status_code == 204:
                print(f"Artist {artist_id}: No future events found")
                response.success()
            elif status_code == 404:
                print(f"Artist {artist_id}: Artist not found")
                response.success()
            else:
                print(f"Artist {artist_id}: Unexpected status code {status_code}")
                print(f"Response: {response.text}")
                response.failure("Failed request")

    @task()
    def check_verified_artist(self):
        """Simula la verificación de un artista autenticado"""
        with self.client.get("/api/event/isVerified", headers=self._headers(), catch_response=True) as response:
            try:
                data = response.json()
            except Exception:
                data = None

            if response.status_code == 200 and data and data.get("data") is True:
                response.success()
                print("Artista verificado correctamente")
            else:
                print(f"Unexpected response: {response.status_code} | {response.text}")
                response.failure("Error verificando artista")


    @task
    def get_my_events_paginated(self):
        """Simula la consulta paginada de eventos de un artista"""
        # Elegir aleatoriamente una página y tamaño
        page = 1
        size = 6

        url = f"/api/event/allMyEvents?page={page}&size={size}"

        with self.client.get(url, headers=self._headers(), catch_response=True) as response:
            try:
                data = response.json()
            except Exception:
                data = None

            if response.status_code == 200 and data and "data" in data:
                response.success()
                print(f"Página {page} con tamaño {size} recibida correctamente")
            else:
                print(f"Error inesperado: {response.status_code} | {response.text}")
                response.failure("Fallo al obtener eventos del artista")

    @task
    def get_event_details(self):
        """Simula la consulta de detalles de eventos por ID"""
        event_id = 1
        url = f"/api/event/details/{event_id}"

        with self.client.get(url, headers=self._headers(),catch_response=True) as response:
            try:
                data = response.json()
            except Exception:
                data = None

            if response.status_code == 200 and data and "data" in data:
                response.success()
                print(f"Evento {event_id} obtenido correctamente")
            elif response.status_code == 404:
                print(f"Evento {event_id} no encontrado")
                response.failure("Evento no encontrado")
            else:
                print(f"Error inesperado: {response.status_code} | {response.text}")
                response.failure("Fallo al obtener detalles del evento")

    
    @task()
    def get_products_images(self):
        """Simula la carga de imágenes de productos"""
        file_name = random.choice(self.image_files)
        url = f"/api/event{file_name}"
        with self.client.get(url, catch_response=True) as response:
            # Validamos que sea una imagen o 404
            if response.status_code == 200:
                response.success()
            elif response.status_code == 404:
                # Opcionalmente, puedes considerar 404 como "success" si es esperado
                response.success()
            else:
                response.failure(f"Unexpected status code {response.status_code} or content type {response.headers.get('Content-Type')} for {url}")