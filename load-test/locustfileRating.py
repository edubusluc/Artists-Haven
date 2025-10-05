from locust import HttpUser, task, between
import random

# Lista de IDs de productos válidos para la prueba de carga

class ProductReviewUser(HttpUser):
    wait_time = between(1, 3)

    @task
    def get_product_ratings(self):
        product_id = 1
        with self.client.get(f"/api/rating/productReview/{product_id}", catch_response=True) as response:
            if response.status_code == 200:
                try:
                    data = response.json()
                    if "data" not in data:
                        response.failure("Response missing 'data' field")
                    else:
                        response.success()
                except Exception as e:
                    response.failure(f"Failed to parse JSON: {e}")
            elif response.status_code == 404:
                response.success()  # Producto no encontrado es aceptable
            else:
                response.failure(f"Unexpected status code {response.status_code}")
