from locust import HttpUser, task, between
import random

class ArtistUser(HttpUser):
    wait_time = between(1, 3)

    def on_start(self):
        # IDs de artistas de prueba
        self.artist_ids = [3, 4, 5, 6]
        
        self.image_files = [
                "/mainArtist_media/duki.png",
                "/mainArtist_media/trueno.png" 
            ]
        
        response = self.client.post("/api/auth/login", json={"email": "duki@email.com", "password": "asdasd"})
        self.token = response.json()["token"]

    def _headers(self):
        if self.token:
            return {"Authorization": f"Bearer {self.token}"}
        return {}


    @task
    def get_artist_by_id(self):
        artist_id = random.choice(self.artist_ids)
        url = f"/api/artists/{artist_id}"

        with self.client.get(url, catch_response=True) as response:
            if response.status_code == 200:
                data = response.json()
                print("Artist retrieved:", data.get("data", {}).get("artistName"))
                response.success()
            elif response.status_code == 404:
                print("Artist not found:", artist_id)
                response.failure("Artist not found")
            else:
                print("Error:", response.status_code, response.text)
                response.failure("Failed request")

    @task
    def get_dashboard(self):
        year = 2025
        url = f"/api/artists/dashboard?year={year}"

        with self.client.get(url, headers=self._headers(), catch_response=True) as response:
            if response.status_code == 200:
                data = response.json()
                print("Dashboard retrieved for year:", year)
                print("Dashboard", data)
                response.success()
            elif response.status_code == 401:
                print("Unauthorized access")
                response.failure("Unauthorized")
            else:
                print("Error:", response.status_code, response.text)
                response.failure("Failed request")

    @task
    def get_monthly_sales(self):
        year = 2025
        url = f"/api/artists/sales/monthly?year={year}"

        with self.client.get(url, headers=self._headers(), catch_response=True) as response:
            if response.status_code == 200:
                data = response.json()
                print(f"Monthly sales retrieved for year {year}: {len(data.get('data', []))} records")
                response.success()
            elif response.status_code == 401:
                print("Unauthorized access")
                response.failure("Unauthorized")
            else:
                print("Error:", response.status_code, response.text)
                response.failure("Failed request")

    @task
    def get_artist_main_view(self):
        url = f"/api/artists/main"

        with self.client.get(url, catch_response=True) as response:
            if response.status_code == 200:
                data = response.json()
                print(f"Artist Main view: {len(data.get('data', []))} records")
                response.success()
            elif response.status_code == 401:
                print("Unauthorized access")
                response.failure("Unauthorized")
            else:
                print("Error:", response.status_code, response.text)
                response.failure("Failed request")

    @task(weight=5)
    def get_artists_images(self):
        """Simula la carga de imágenes de productos"""
        file_name = random.choice(self.image_files)
        url = f"/api/artists{file_name}"
        with self.client.get(url, catch_response=True) as response:
            # Validamos que sea una imagen o 404
            if response.status_code == 200:
                response.success()
            elif response.status_code == 404:
                response.success()
            else:
                response.failure(f"Unexpected status code {response.status_code} or content type {response.headers.get('Content-Type')} for {url}")
