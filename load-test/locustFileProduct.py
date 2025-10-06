from locust import HttpUser, task, between
import random
import string
import json
import os
from io import BytesIO


LOGIN_PATH = "/api/auth/login"
VERIFY_TLS = os.getenv("VERIFY_TLS", "true").lower() == "true"  # para https con cert válido

class Prodcut(HttpUser):
    wait_time = between(1, 3)

    def on_start(self):
        self.image_files = [
                "/product_media/camiseta1.png",
                "/product_media/Camiseta1-back.png",
                "/product_media/Camiseta001.png",
                "/product_media/GorraNicki-1.png"  # Para probar 404
            ]
        
        response = self.client.post("/api/auth/login", json={"email": "mod.artistheaven@gmail.com", "password": "admin"})
        self.token = response.json()["token"]

    def _headers(self):
        if self.token:
            return {"Authorization": f"Bearer {self.token}"}
        return {}

    @task(weight=5)
    def get_all_products(self):
        with self.client.get("/api/product/allProducts", catch_response=True) as response:
            try:
                payload = response.json()
                if all(k in payload for k in ("message", "data", "status")):
                    response.success()
                else:
                    response.failure(f"Campos faltantes en la respuesta: {payload}")
            except Exception as e:
                response.failure(f"Error parseando JSON: {e} | Respuesta cruda: {response.text}")

    @task(weight=5)
    def get_products_images(self):
        """Simula la carga de imágenes de productos"""
        file_name = random.choice(self.image_files)
        url = f"/api/product{file_name}"
        with self.client.get(url, catch_response=True) as response:
            # Validamos que sea una imagen o 404
            if response.status_code == 200:
                response.success()
            elif response.status_code == 404:
                # Opcionalmente, puedes considerar 404 como "success" si es esperado
                response.success()
            else:
                response.failure(f"Unexpected status code {response.status_code} or content type {response.headers.get('Content-Type')} for {url}")

    @task(weight=5)
    def get_all_categories(self):
        with self.client.get("/api/product/categories", catch_response=True) as response:
            try:
                payload = response.json()
                if all(k in payload for k in ("message", "data", "status")):
                    response.success()
                else:
                    response.failure(f"Campos faltantes en la respuesta: {payload}")
            except Exception as e:
                response.failure(f"Error parseando JSON: {e} | Respuesta cruda: {response.text}")

    @task(weight=1)
    def create_new_product(self):
        """Simula la creación de un producto con imágenes"""

        # Color con imágenes ya declaradas
        color = {
            "colorName": "Test Red",
            "hexCode": "#FF0000",
            "sizes": {"S": 5, "M": 10},
            "availableUnits": None,
            "images": ["test1.png", "test2.png"]  # importante
        }

        # Construcción del ProductDTO
        product_dto = {
            "name": f"LoadTest Product {random.randint(1,10000)}",
            "description": "Producto de prueba creado vía test de carga.",
            "price": round(random.uniform(50, 500), 2),
            "categories": [{"id": 1, "name": "SUMMER"}],
            "collectionId": None,
            "onPromotion": random.choice([True, False]),
            "discount": random.choice([0, 10, 20]),
            "section": "HOODIES",
            "composition": "Canvas, acrylic",
            "shippingDetails": "Ships within 5 business days",
            "colors": [color]
        }

        # Simular archivos de imagen
        image_content = b"\x89PNG\r\n\x1a\n"  # cabecera PNG mínima
        files = [
            ("product", (None, json.dumps(product_dto), "application/json")),
            ("images", ("test1.png", image_content, "image/png")),
            ("images", ("test2.png", image_content, "image/png"))
        ]

        with self.client.post("/api/product/new", headers=self._headers(), files=files, catch_response=True) as response:
            if response.status_code == 201:
                try:
                    payload = response.json()
                    if all(k in payload for k in ("message", "data", "status")):
                        response.success()
                    else:
                        response.failure(f"Respuesta no tiene StandardResponse completo: {payload}")
                except Exception as e:
                    response.failure(f"Error parseando JSON: {e} | Respuesta cruda: {response.text}")
            else:
                response.failure(f"Unexpected status code {response.status_code} | Respuesta cruda: {response.text}")


    @task(weight=5)
    def get_existing_product(self):
        """Consulta detalles de un producto válido"""
        product_id = 1
        url = f"/api/product/details/{product_id}"
        with self.client.get(url, catch_response=True) as response:
            if response.status_code == 200:
                try:
                    payload = response.json()
                    if all(k in payload for k in ("message", "data", "status")):
                        response.success()
                    else:
                        response.failure(f"Respuesta inválida: {payload}")
                except Exception as e:
                    response.failure(f"Error parseando JSON: {e} | Respuesta cruda: {response.text}")
            else:
                response.failure(f"Status inesperado {response.status_code} para {url}")


    @task(weight=5)
    def update_product(self):
        color = {
            "colorId": 1,
            "colorName": "Test Red",
            "hexCode": "#FF0000",
            "sizes": {"S": 5, "M": 10},
            "availableUnits": None,
            "images": ["test1.png", "test2.png"] 
        }

        base_product_dto = {
            "name": "Updated Abstract Landscape Painting",
            "description": "Updated description for testing",
            "price": 149.99,
            "sizes": {"M": 8, "L": 2},
            "categories": [{ "id": 1, "name": "SUMMER" }],
            "collectionId": None,
            "images": ["/product_media/camiseta1.png"],
            "onPromotion": False,
            "discount": 0,
            "section": "HOODIES",
            "availableUnits": 0,
            "available": True,
            "reference": 123456,
            "composition": "Acrylic on canvas",
            "shippingDetails": "Ships in 5-7 business days",
            "colors": [color]
        }

        """Simula la actualización de un producto existente con multipart/form-data"""
        product_id = 1
        url = f"/api/product/edit/{product_id}"

        # Construir multipart: el DTO en un campo de texto + opcional archivos
        multipart_data = {
            "product": (None, json.dumps(base_product_dto), "application/json")
        }

        # Si tu endpoint también espera archivos reales (ejemplo imagen subida):
        # files = {"images": open("tests/assets/test1.png", "rb")}
        # Pero si no hace falta, puedes omitirlos.

        with self.client.put(
            url,
            headers=self._headers(),   # sin forzar Content-Type, requests lo arma solo
            files=multipart_data,
            catch_response=True
        ) as response:
            if response.status_code == 200:
                try:
                    payload = response.json()
                    if all(k in payload for k in ("message", "data", "status")):
                        response.success()
                    else:
                        response.failure(f"Respuesta inválida: {payload}")
                except Exception as e:
                    response.failure(f"Error parseando JSON: {e} | Respuesta cruda: {response.text}")
            elif response.status_code in (400, 404):
                response.success()
            else:
                response.failure(
                    f"Status inesperado {response.status_code} para {url} | Respuesta cruda: {response.text}"
                )



    @task(weight=5)
    def get_sorted_12_products(self):
        with self.client.get("/api/product/sorted12Product", catch_response=True) as response:
            if response.status_code == 200:
                try:
                    data = response.json()
                    products = data.get("data", [])
                    if len(products) <= 12:
                        response.success()
                    else:
                        response.failure(f"Expected max 12 products, got {len(products)}")
                except Exception as e:
                    response.failure(f"Failed to parse JSON: {e}")
            else:
                response.failure(f"Unexpected status {response.status_code}")
    @task(weight=5)
    def get_tshirt(self):
        with self.client.get("/api/product/tshirt", catch_response=True) as response:
            if response.status_code == 200:
                try:
                    data = response.json()
                    products = data.get("data", [])
                    if all(k in data for k in ("message", "data", "status")):
                        response.success()
                    else:
                        response.failure(f"Expected tshirt, got {products}")
                except Exception as e:
                    response.failure(f"Failed to parse JSON: {e}")
            else:
                response.failure(f"Unexpected status {response.status_code}")


    @task(weight=5)
    def get_pants(self):
        with self.client.get("/api/product/pants", catch_response=True) as response:
            if response.status_code == 200:
                try:
                    data = response.json()
                    products = data.get("data", [])
                    if all(k in data for k in ("message", "data", "status")):
                        response.success()
                    else:
                        response.failure(f"Expected pants, got {products}")
                except Exception as e:
                    response.failure(f"Failed to parse JSON: {e}")
            else:
                response.failure(f"Unexpected status {response.status_code}")


    @task(weight=5)
    def get_hoodies(self):
        with self.client.get("/api/product/hoodies", catch_response=True) as response:
            if response.status_code == 200:
                try:
                    data = response.json()
                    products = data.get("data", [])
                    if all(k in data for k in ("message", "data", "status")):
                        response.success()
                    else:
                        response.failure(f"Expected hoodies, got {products}")
                except Exception as e:
                    response.failure(f"Failed to parse JSON: {e}")
            else:
                response.failure(f"Unexpected status {response.status_code}")

    @task(weight=5)
    def get_accessories(self):
        with self.client.get("/api/product/accessories", catch_response=True) as response:
            if response.status_code == 200:
                try:
                    data = response.json()
                    products = data.get("data", [])
                    if all(k in data for k in ("message", "data", "status")):
                        response.success()
                    else:
                        response.failure(f"Expected accessories, got {products}")
                except Exception as e:
                    response.failure(f"Failed to parse JSON: {e}")
            else:
                response.failure(f"Unexpected status {response.status_code}")

    @task(weight=5)
    def get_related_products(self):
        section = "TSHIRT"
        product_id = 1

        with self.client.get(
            f"/api/product/related?section={section}&id={product_id}",
            catch_response=True
        ) as response:
            if response.status_code == 200:
                try:
                    data = response.json()
                    products = data.get("data", [])
                    
                    # Validación 1: máximo 4 productos
                    if len(products) > 4:
                        response.failure(f"Expected max 4 products, got {len(products)}")
                    
                    # Validación 2: no debe estar el mismo id
                    for p in products:
                        if p.get("id") == product_id:
                            response.failure(f"Response contains excluded product id {product_id}")
                            break

                    response.success()
                except Exception as e:
                    response.failure(f"Failed to parse JSON: {e}")

            elif response.status_code == 404:
                response.success()  # Caso válido: no hay relacionados
            else:
                    response.failure(f"Unexpected status {response.status_code}")

    @task(weight=5)
    def find_product_valid(self):
        reference = 12345
        lang = "es"

        with self.client.get(
            f"/api/product/by-reference?reference={reference}&lang={lang}",
            catch_response=True
        ) as response:
            if response.status_code == 200:
                try:
                    data = response.json()
                    if all(k in data for k in ("message", "data", "status")):
                        response.success()
                    else:
                        response.failure()
                except Exception as e:
                    response.failure(f"Failed to parse JSON: {e}")
            else:
                response.failure(f"Unexpected status code {response.status_code}")

    @task(weight=5)
    def get_promoted_collections(self):
        with self.client.get("/api/product/promoted-collections", catch_response=True) as response:
            if response.status_code == 200:
                try:
                    data = response.json()
                    if all(k in data for k in ("message", "data", "status")):
                        response.success()
                    else:
                        response.failure()
                except Exception as e:
                    response.failure(f"Failed to parse JSON: {e}")
            elif response.status_code == 404:
                response.success()  
            else:
                response.failure(f"Unexpected status code {response.status_code}")
