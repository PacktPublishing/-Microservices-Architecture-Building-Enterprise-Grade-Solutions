from locust import HttpLocust, TaskSet, task
import json

class WebsiteTasks(TaskSet):
    @task
    def write(self):
        headers = {'content-type': 'application/json'}
        for id in range(1, 1000):
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

class WebsiteUser(HttpLocust):
    task_set = WebsiteTasks
    min_wait = 50000
    max_wait = 50000