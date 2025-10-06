from io import BytesIO
import os
import random
import string
from locust import HttpUser, task, between

# ===== Configuración =====
PROFILE_PATH = "/api/users/profile"
LOGIN_PATH = "/api/auth/login"
VERIFY_TLS = os.getenv("VERIFY_TLS", "true").lower() == "true"  # para https con cert válido

def random_string(length=8):
    return ''.join(random.choices(string.ascii_letters, k=length))

def random_digits(length=10):
        return ''.join(random.choices(string.digits, k=length))


class ProfileUser(HttpUser):
    wait_time = between(1, 3)  # simula tiempos entre acciones de usuario

    def on_start(self):
        """
        Si hay AUTH_TOKEN en variables de entorno, lo usa (Opción A).
        Si no, intenta loguearse con AUTH_USERNAME/AUTH_PASSWORD (Opción B).
        """
        self.token = os.getenv("AUTH_TOKEN")

        if not self.token:
            username = "dummyuser@email.com"
            password = "dummyUser"
            if username and password:
                with self.client.post(
                    LOGIN_PATH,
                    json={"email": username, "password": password},
                    catch_response=True,
                    name=f"POST {LOGIN_PATH}",
                    verify=VERIFY_TLS,
                ) as res:
                    if res.status_code != 200:
                        res.failure(f"Login failed ({res.status_code}): {res.text[:200]}")
                        return
                    try:
                        data = res.json()
                        # Ajusta la clave del token según tu respuesta (token / access_token / jwt / etc.)
                        self.token = data.get("token") or data.get("access_token") or data.get("jwt")
                        if not self.token:
                            res.failure("No se encontró el token en la respuesta de login")
                    except Exception as e:
                        res.failure(f"JSON de login inválido: {e}")
            else:
                # Sin token ni credenciales—el endpoint devolverá 401
                self.token = None

    @task
    def get_profile(self):
        headers = {}
        if self.token:
            headers["Authorization"] = f"Bearer {self.token}"

        with self.client.get(
            PROFILE_PATH,
            headers=headers,
            catch_response=True,
            name=f"GET {PROFILE_PATH}",
            verify=VERIFY_TLS,
        ) as res:
            # Marca como fallo cualquier estado != 200
            if res.status_code != 200:
                res.failure(f"Status {res.status_code}: {res.text[:200]}")
                return

            # Validaciones ligeras del payload (ajusta según tu StandardResponse)
            try:
                payload = res.json()
                # Si usas StandardResponse< UserProfileDTO >
                # típicamente tendrás: message, data, statusCode (ajusta a tus campos reales)
                message = str(payload.get("message", ""))
                if "User profile retrieved successfully" not in message:
                    res.failure("Mensaje inesperado en la respuesta")
            except Exception as e:
                res.failure(f"JSON inválido: {e}")

    @task
    def update_profile(self):
        headers = {"Authorization": f"Bearer {self.token}"} if self.token else {}

        data = {
            "id": 8,
            "email": "dummyuser@email.com",
            "username": "dummyUserUpdated",
            "firstName": random_string(5),
            "lastName": random_string(5),
            "address": random_string(12) + " Street",
            "city": "Paris",
            "postalCode": "75000",
            "country": "France",
            "phone": random_digits(10),
            "lang": "es" 
        }

        with self.client.put(
            "/api/users/profile/edit",
            headers=headers,
            data=data,
            catch_response=True,
            name="PUT /profile/edit"
        ) as res:
            if res.status_code != 200:
                    res.failure(f"Status {res.status_code}: {res.text[:200]}")


    @task
    def register_new_user(self):
        # Generar datos aleatorios válidos
        username = random_string(6)
        email = f"{random_string(5)}@example.com"
        password = "Test1234"
        firstName = random_string(5)
        lastName = random_string(5)
        phone = random_digits(10)
        country = "France"
        postalCode = "75000"
        city = "Paris"
        address = random_string(12) + " Street"

        data = {
            "username": username,
            "email": email,
            "password": password,
            "firstName": firstName,
            "lastName": lastName,
            "phone": phone,
            "country": country,
            "postalCode": postalCode,
            "city": city,
            "address": address
        }

        # Parámetro lang
        params = {"lang": "es"}

        # Hacer request
        with self.client.post(
            "/api/users/register",
            json=data,
            params=params,
            catch_response=True,
            name="POST /register",
            verify=VERIFY_TLS
        ) as response:
            if response.status_code != 201:
                response.failure(f"Status {response.status_code}: {response.text[:200]}")
            else:
                response.success()
    
