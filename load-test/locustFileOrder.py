from locust import HttpUser, task, between
import random

class MyOrdersUser(HttpUser):
    wait_time = between(1, 3)

    def on_start(self):
        response = self.client.post("/api/auth/login", json={"email": "dummyuser@email.com", "password": "dummyUser"})
        self.token = response.json()["token"]

    def _headers(self):
        if self.token:
            return {"Authorization": f"Bearer {self.token}"}
        return {}  

    @task
    def get_my_orders(self):
        """Simula la carga de la API de mis pedidos"""
        page = 1
        size = random.choice([3, 5, 10]) 

        url = f"/api/orders/myOrders?page={page}&size={size}"

        with self.client.get(url, headers=self._headers(), catch_response=True) as response:
            try:
                data = response.json()
            except Exception:
                data = None

            if response.status_code == 200 and data and "data" in data:
                response.success()
                print(f"Pedidos obtenidos correctamente | página {page}, size {size}")
            elif response.status_code == 401:
                print("Usuario no autorizado")
                response.failure("Usuario no autorizado")
            else:
                print(f"Error inesperado: {response.status_code} | {response.text}")
                response.failure("Fallo al obtener mis pedidos")

    @task
    def get_order_details(self):
        """Simula la carga de la API de detalles de pedido"""
        # IDs de pedidos que existan en tu sistema
        order_id = 2

        url = f"/api/orders/{order_id}"

        with self.client.get(url, headers=self._headers(), catch_response=True) as response:
            try:
                data = response.json()
            except Exception:
                data = None

            if response.status_code == 200 and data and "data" in data:
                response.success()
                print(f"Pedido {order_id} obtenido correctamente")
            elif response.status_code == 403:
                print(f"Acceso denegado al pedido {order_id}")
                response.failure(f"Acceso denegado al pedido {order_id}")
            elif response.status_code == 404:
                print(f"Pedido {order_id} no encontrado")
                response.failure(f"Pedido {order_id} no encontrado")
            else:
                print(f"Error inesperado {response.status_code} | {response.text}")
                response.failure("Error inesperado al obtener pedido")

    @task(3)
    def get_order_by_identifier(self):
        """Simula la carga del endpoint que obtiene pedidos por identificador"""
        order_identifier = random.choice([100001, 100002, 100003])

        # Idioma de la respuesta, puede ser "en", "es", etc.
        lang = random.choice(["en", "es"])

        url = f"/api/orders/by-identifier?identifier={order_identifier}&lang={lang}"

        with self.client.get(url,  catch_response=True) as response:
            try:
                data = response.json()
            except Exception:
                data = None

            if response.status_code == 200 and data and "data" in data:
                response.success()
                print(f"Pedido {order_identifier} obtenido correctamente")
            elif response.status_code == 404:
                response.failure(f"Pedido {order_identifier} no encontrado")
            else:
                response.failure(f"Error inesperado {response.status_code} | {response.text}")
