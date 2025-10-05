from locust import HttpUser, task, between
import random

class ForgotPasswordUser(HttpUser):
    wait_time = between(1, 3)  # Tiempo de espera entre requests

    def on_start(self):
        # Emails simulados para pruebas
        self.emails = [
            "user1@example.com",
            "user2@example.com",
            "user3@example.com",
            "nonexistent@example.com"  # Para probar casos donde la cuenta no existe
        ]

    @task
    def forgot_password(self):
        email = random.choice(self.emails)
        payload = {"email": email}

        with self.client.post("/api/auth/forgot-password", json=payload, catch_response=True) as response:
            if response.status_code == 200:
                print(f"Correo enviado (si existe): {email}")
                response.success()
            else:
                print(f"Error: {response.status_code} -> {response.text}")
                response.failure("Fallo en forgot-password")
