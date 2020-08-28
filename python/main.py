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
from gensim_req import query_phrase


app = Flask(__name__)


@app.route('/', methods=['GET', 'POST', 'OPTIONS'])
def root():

    if request.method == 'GET':
        # get url params: request.args.get(KEY)
        # This request is currently not being used

        # query = 'unrelated'
        while True:
            query = input('input query: ')
            json_in = '{"captions": [{"text": "hello, world"}, {"text": "forget me"}]}'
            res = query_phrase(query, json_in)
            print("RES: " + str(res))
        return jsonify({"indices": res})

    if request.method == 'POST':
        try:
            # get params from post: request.form[KEY]
            request_json = request.json
            query = request_json['query']
            json_in = request_json['ytCaptions']

            indices = query_phrase(query, json_in)
            ret = {"indices": indices} 

            return _corsify_actual_response(jsonify(ret))
        except Exception as e:
            return _corsify_actual_response(jsonify({'error': str(e)})), 201, {'ContentType':'application/json'}

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
