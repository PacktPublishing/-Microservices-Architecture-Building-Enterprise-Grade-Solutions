from locust import HttpLocust, TaskSet, task
import json
import random

class WebsiteTasks(TaskSet):
    @task
    def read(self):
        for id in range(1, 100):
            random_id = random.sample(range(1,1000),  1)[0]
            song_id = str(random_id)
            self.client.get("/songs/" + song_id)

    @task
    def simple_check(self):
        for id in range(1, 100):
            self.client.get("/health-check")

class WebsiteUser(HttpLocust):
    task_set = WebsiteTasks
    min_wait = 50000
    max_wait = 50000