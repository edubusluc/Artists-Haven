from locust import HttpUser, task, between
import random
import string
import json

# Utilidad para generar strings aleatorios
def random_string(length=8):
    return ''.join(random.choices(string.ascii_lowercase, k=length))

class AdminUser(HttpUser):
    wait_time = between(1, 3)

    def on_start(self):
        response = self.client.post("/api/auth/login", json={"email": "mod.artistheaven@gmail.com", "password": "admin"})
        self.token = response.json()["token"]

    def _headers(self):
        if self.token:
            return {"Authorization": f"Bearer {self.token}"}
        return {}

    # --- GET endpoints (consultas del dashboard) ---
    @task(2)
    def get_statistics(self):
        params = {"year": 2025}
        with self.client.get("/api/admin/staticsPerYear", headers=self._headers(), catch_response=True, params=params) as response:
            if response.status_code == 200:
                try:
                    data = response.json()
                    inner_data = data.get("data", {})
                    # Comprobamos algunos campos clave del DTO
                    if "numOrders" in inner_data and "numUsers" in inner_data:
                        response.success()
                    else:
                          response.failure(
                        f"Faltan campos esperados en 'data': {response.text}"
                        )
                except Exception as e:
                    response.failure(f"Error parseando JSON: {e} | Respuesta cruda: {response.text}")
            else: 
                response.failure(f"Status code inesperado: {response.status_code} | Respuesta cruda: {response.text}")

    @task(2)
    def get_monthly_sales(self):
        params = {"year": 2025}
        with self.client.get(
            "/api/admin/sales/monthly",
            headers=self._headers(),
            catch_response=True,
            params=params
        ) as response:
            if response.status_code == 200:
                try:
                    payload = response.json()
                    # La API devuelve StandardResponse con { message, data, status }
                    data = payload.get("data", [])

                    # Validamos que data tenga sentido
                    if data and all("month" in item and "totalOrders" in item for item in data):
                        response.success()
                    else:
                        response.failure(f"Respuesta sin datos válidos: {response.text}")
                except Exception as e:
                    response.failure(f"Error parseando JSON: {e} | Respuesta cruda: {response.text}")
            else:
                response.failure(f"Status code inesperado: {response.status_code} | Respuesta cruda: {response.text}")


    @task(2)
    def get_product_management(self):
        with self.client.get(
            "/api/admin/product-management",
            headers=self._headers(),
            catch_response=True
        ) as response:
            if response.status_code == 200:
                try:
                    payload = response.json()
                    data = payload.get("data", {})

                    # Validamos los campos clave del DTO
                    required_fields = [
                        "notAvailableProducts",
                        "availableProducts",
                        "promotedProducts",
                        "totalProducts"
                    ]
                    if all(field in data for field in required_fields):
                        response.success()
                    else:
                        response.failure(f"Campos faltantes en la respuesta: {data}")
                except Exception as e:
                    response.failure(f"Error parseando JSON: {e} | Respuesta cruda: {response.text}")
            else:
                response.failure(f"Status code inesperado: {response.status_code} | Respuesta cruda: {response.text}")

    @task(2)
    def get_verificationPending(self):
        with self.client.get(
            "/api/admin/verification/pending",
            headers=self._headers(),
            catch_response=True
        ) as response:
            if response.status_code == 200:
                try:
                    payload = response.json()
                    data = payload.get("data", [])

                    required_fields = ["id"]

                    missing = []
                    for item in data:
                        for field in required_fields:
                            if field not in item:
                                missing.append({"item": item, "missing_field": field})

                    if not missing:
                        response.success()
                    else:
                        response.failure(f"Campos faltantes en la respuesta: {missing}")

                except Exception as e:
                    response.failure(f"Error parseando JSON: {e} | Respuesta cruda: {response.text}")
            else:
                response.failure(f"Status code inesperado: {response.status_code} | Respuesta cruda: {response.text}")
     
    @task(2)
    def get_users(self):
        with self.client.get(
            "/api/admin/users",
            headers=self._headers(),
            catch_response=True
        ) as response:
            if response.status_code == 200:
                try:
                    payload = response.json()

                    required_fields = ["id"]
                    missing = []

                    if 'content' in payload:
                        for item in payload['content']: 
                            for field in required_fields:
                                if field not in item:
                                    missing.append({"item": item, "missing_field": field})
                    else:
                        missing.append({"item": "Respuesta", "missing_field": "'content' (datos de usuarios)"})

                    if not missing:
                        response.success()
                    else:
                        response.failure(f"Campos faltantes en la respuesta: {missing}")

                except Exception as e:
                    response.failure(f"Error parseando JSON: {e} | Respuesta cruda: {response.text}")
            else:
                response.failure(f"Status code inesperado: {response.status_code} | Respuesta cruda: {response.text}") 

    @task(2)
    def get_orders(self):
        with self.client.get(
            "/api/admin/orders",
            headers=self._headers(),
            catch_response=True
        ) as response:
            if response.status_code == 200:
                try:
                    payload = response.json()

                    required_fields = ["id"]
                    missing = []

                    if 'content' in payload:
                        for item in payload['content']: 
                            for field in required_fields:
                                if field not in item:
                                    missing.append({"item": item, "missing_field": field})
                    else:
                        missing.append({"item": "Respuesta", "missing_field": "'content' (datos de usuarios)"})

                    if not missing:
                        response.success()
                    else:
                        response.failure(f"Campos faltantes en la respuesta: {missing}")

                except Exception as e:
                    response.failure(f"Error parseando JSON: {e} | Respuesta cruda: {response.text}")
            else:
                response.failure(f"Status code inesperado: {response.status_code} | Respuesta cruda: {response.text}") 
