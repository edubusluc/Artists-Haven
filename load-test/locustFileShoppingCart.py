from locust import HttpUser, task, between

class ShoppingCartUser(HttpUser):
    wait_time = between(1, 3)  # espera entre 1 y 3 segundos entre requests

    # Antes de ejecutar las tareas, podemos obtener un token JWT si la API requiere autenticación
    def on_start(self):
        response = self.client.post("/api/auth/login", json={"email": "dummyuser@email.com", "password": "dummyUser"})
        self.token = response.json()["token"]

    def _headers(self):
        if self.token:
            return {"Authorization": f"Bearer {self.token}"}
        return {}

    @task
    def get_my_cart_anonymous(self):
        with self.client.get("/api/myShoppingCart", catch_response=True) as response:
            if response.status_code == 200:
                print("Shopping cart response:", response.json())
                response.success()
            else:
                print("Failed to retrieve cart, status:", response.status_code)
                response.failure("Failed request")

    @task
    def get_my_cart(self):
        with self.client.get("/api/myShoppingCart", headers=self._headers(), catch_response=True) as response:
            if response.status_code == 200:
                print("Shopping cart response:", response.json())
                response.success()
            else:
                print("Failed to retrieve cart, status:", response.status_code)
                response.failure("Failed request")

    @task
    def add_product_to_authenitcate_cart(self):
        product = {"productId": 1, "size": "M", "color": "Blanco"} 
        payload = {
            "productId": product["productId"],
            "size": product.get("size"),
            "color": "Blanco"
        }
        with self.client.post("/api/myShoppingCart/addProducts", json=payload, headers=self._headers(), catch_response=True) as response:
            if response.status_code == 200:
                print("Added product to cart:", response.json())
                response.success()
            elif response.status_code == 404:
                print("Product not found:", payload["productId"])
                response.failure("Product not found")
            else:
                print("Failed request, status:", response.status_code)
                response.failure("Failed request")

    @task
    def add_product_to_cart_non_auth(self):
        product = {"productId": 1, "size": "M"} 
        payload = {
            "shoppingCart": {
                "id": None,
                "items": [],
                "user": None
            },
            "productId": product["productId"],
            "size": product["size"]
        }

        with self.client.post("/api/myShoppingCart/addProductsNonAuthenticate", json=payload, catch_response=True) as response:
            print("Response text:", response.text) 
            if response.status_code == 200:
                # Actualizamos el shoppingCart con la respuesta para simular el siguiente request
                self.shopping_cart = response.json()
                print("Cart updated:", self.shopping_cart)
                response.success()
            elif response.status_code == 404:
                print("Product not found:", product["productId"])
                response.failure("Product not found")
            else:
                print("Failed request, status:", response.status_code)
                response.failure("Failed request")
