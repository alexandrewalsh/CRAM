# Copyright 2018 Google LLC
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
from re import sub
from gensim.utils import simple_preprocess
import nltk
from nltk.corpus import stopwords
import numpy as np
import json


app = Flask(__name__)


# def quickstart():
#     import os
#     credential_path = "client_secret.json"
#     os.environ['GOOGLE_APPLICATION_CREDENTIALS'] = credential_path


def preprocess(doc, stop_words):
    """ Tokenize webpages from Wikipedia.

    Keyword arguments:
    doc        -- a Wikipedia page to preprocess and tokenize
    stop_words -- a collection of words to ignore
    """

    # Tokenize, clean up input document string
    doc = sub(r'<img[^<>]+(>|$)', " image_token ", doc)
    doc = sub(r'<[^<>]+(>|$)', " ", doc)
    doc = sub(r'\[img_assist[^]]*?\]', " ", doc)
    doc = sub(r'http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\(\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+', " url_token ", doc)
    return [token for token in simple_preprocess(doc, min_len=0, max_len=float("inf")) if token not in stop_words]


def processInput(json_in):
    """ format input JSON to a document format

    Keywords arguments:
    json_in -- json returned from the YouTube Captions API

    Returns:
    An array of caption strings
    """

    documents = []
    json_processed = json.loads(json_in)
    for caption in json_processed['captions']:
        documents.append(caption['text'])

    return documents


def createModel(json_in):
    # get stop words
    nltk.download('stopwords')
    stop_words = set(stopwords.words('english')) # common words to ignore
    documents = processInput(json_in)
    return documents


@app.route('/', methods=['GET', 'POST', 'OPTIONS'])
def root():
    origin = ''
    if request.headers.get('Origin'):
        origin = request.headers.get('Origin')

    if request.method == 'GET':
        # get url params: request.args.get(KEY)
        # Instantiates a client
        return jsonify({'test': request.args.get('test')})

    if request.method == 'POST':
        # get params from post: request.form[KEY]
        request_json = request.json
        return _corsify_actual_response(jsonify(request_json))
    
    if request.method == 'OPTIONS':
        return _build_cors_prelight_response()


def _build_cors_prelight_response():
    response = make_response()
    response.headers.add("Access-Control-Allow-Origin", "*")
    response.headers.add('Access-Control-Allow-Headers', "*")
    response.headers.add('Access-Control-Allow-Methods', "*")
    return response

def _corsify_actual_response(response):
    response.headers.add("Access-Control-Allow-Origin", "*")
    return response
    # method, origin


def handlePost(json):
    '''
    Handle POST request
    '''
    return None

if __name__ == '__main__':
    # This is used when running locally only. When deploying to Google App
    # Engine, a webserver process such as Gunicorn will serve the app. This
    # can be configured by adding an `entrypoint` to app.yaml.
    # Flask's development server will automatically serve static files in
    # the "static" directory. See:
    # http://flask.pocoo.org/docs/1.0/quickstart/#static-files. Once deployed,
    # App Engine itself will serve those files as configured in app.yaml.
    app.run(host='127.0.0.1', port=8080, debug=True)
