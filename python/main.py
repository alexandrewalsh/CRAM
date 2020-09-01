# Copyright 2020 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from flask import Flask, request, jsonify, make_response
from gensim_req import query_phrase, create_model
from google.cloud import storage
import os


app = Flask(__name__)
os.environ['GOOGLE_APPLICATION_CREDENTIALS'] = 'static/resources/lecture-buddy-service.json'


@app.route('/', methods=['GET', 'POST', 'OPTIONS'])
def root():

    if request.method == 'GET':
        try:
            # create a storage client
            storage_client = storage.Client()

            # make sure these keys are present 
            query = request.headers.get("query")
            v_id = request.headers.get("vid")

            if query is None or v_id is None:
                raise Exception("query({}) or v_id({}) are null!".format(query, v_id))

            indices = query_phrase(storage_client, query, v_id)
            return _corsify_actual_response(jsonify({"indices": indices}))

        except Exception as e:
            return _corsify_actual_response(jsonify({'error': str(e)})), 500, {'ContentType':'application/json'}

    if request.method == 'POST':
        storage_client = storage.Client()

        try:
            # get params from post: request.form[KEY]
            request_json = request.json
            json_in = request_json['data']
            v_id = request_json['v_id']
            create_model(storage_client, json_in, v_id)
            return _corsify_actual_response(jsonify("Success"))

        except Exception as e:
            return _corsify_actual_response(jsonify({'error': str(e)})), 500, {'ContentType':'application/json'}

    if request.method == 'OPTIONS':
        return _build_cors_prelight_response()


def _build_cors_prelight_response():
    # Send the correct headers in an OPTIONS preflight request
    response = make_response()
    response.headers.add("Access-Control-Allow-Origin", "*")
    response.headers.add('Access-Control-Allow-Headers', "*")
    response.headers.add('Access-Control-Allow-Methods', "*")
    return response


def _corsify_actual_response(response):
    # Send the correct headers for a POST request
    response.headers.add("Access-Control-Allow-Origin", "*")
    return response


if __name__ == '__main__':
    # This is used when running locally only. When deploying to Google App
    # Engine, a webserver process such as Gunicorn will serve the app. This
    # can be configured by adding an `entrypoint` to app.yaml.
    # Flask's development server will automatically serve static files in
    # the "static" directory. See:
    # http://flask.pocoo.org/docs/1.0/quickstart/#static-files. Once deployed,
    # App Engine itself will serve those files as configured in app.yaml.
    app.run(host='127.0.0.1', port=8080, debug=True)
