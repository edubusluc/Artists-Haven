from locust import HttpUser, task, between
import random

# Lista de órdenes válidas y asociadas a usuarios


class ReturnLabelUser(HttpUser):
    wait_time = between(1, 3)

    @task
    def get_return_label(self):
        email = "duki@email.com"

        with self.client.get(f"/api/returns/{3}/label?email={email}", catch_response=True) as response:
            if response.status_code == 200:
                # Validar que la respuesta sea un PDF
                if response.headers.get("Content-Type") != "application/pdf":
                    response.failure("Content-Type is not PDF")
                else:
                    response.success()
            elif response.status_code in [403, 404]:
                response.success()  # escenarios de prueba permitidos
            else:
                response.failure(f"Unexpected status code {response.status_code}")
