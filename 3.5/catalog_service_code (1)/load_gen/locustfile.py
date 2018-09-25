from locust import HttpLocust, TaskSet, task
import json

class WebsiteTasks(TaskSet):
    def on_start(self):
        headers = {'content-type': 'application/json'}
        for id in range(0,40):
            song_id = str(id)
            author_id = "author" + str(id)
            artifact_uri = "s3://bucket/song" + str(id) + ".mp4"
            payload = {
                "id": song_id,
                "author_id": author_id,
                "release_date": 1,
                "duration_in_seconds": 10,
                "artifact_uri": artifact_uri
            }
            self.client.post("/songs", data=json.dumps(payload), headers=headers)

    @task
    def read(self):
        for id in range(0,40):
            song_id = str(id)
            self.client.get("/songs/" + song_id)

class WebsiteUser(HttpLocust):
    task_set = WebsiteTasks
    min_wait = 50000
    max_wait = 50000