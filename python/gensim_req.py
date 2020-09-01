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

from re import sub
import gensim.downloader as api
from gensim.models import KeyedVectors
from gensim.corpora import Dictionary
from gensim.models import TfidfModel
from gensim.models import WordEmbeddingSimilarityIndex
from gensim.similarities import SparseTermSimilarityMatrix
from gensim.similarities import SoftCosineSimilarity
import numpy as np
import json
import os
from gensim.models import Word2Vec
import lemmatizer
import pickle
import os
from google.cloud import storage


# flag that toggles debug messages
debug_messages = True
bucket_name = 'lecture-buddy-287518.appspot.com'


def processInput(json_in):
    """ format input JSON to a document format

    Keywords arguments:
    json_in -- json returned from the YouTube Captions API

    Returns:
    An array of caption strings
    """

    json_processed = json_in if isinstance(json_in, dict) else json.loads(json_in)

    documents = [caption['text'] for caption in json_processed['captions']]

    return documents


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

    return [token for token in lemmatizer.lemmatize_sentence(doc) if token not in stop_words]


def download_resources():
    """ Download stop words and the corpus KeyedVectors

    Returns:
    - A set of stop words
    - KeyedVectors representing Wiki word embeddings
    """

    # get stop words
    stop_words = set()
    english_file = open('static/resources/corpora/stopwords/english', 'r')
    for line in english_file:
        stop_words.add(line.strip())
    english_file.close()

    glove_fname = "static/resources/glove/glove.gz"

    if os.path.isfile(glove_fname):
        glove_vec = KeyedVectors.load(glove_fname, mmap='None')
    else:
        dataset = api.load("text8")
        glove_model = Word2Vec(dataset)
        glove_vec = glove_model.wv
        glove_vec.save(glove_fname)

    return stop_words, glove_vec


def blob_exists(storage_client, blob_name):
    """ Check whether a blob exists in a bucket in Storage

    Keywords arguments:
    storage_client -- a Storage instance
    blob_name      -- the name of the blob (model) 

    Returns:
    - True if the blob `blob_name` exists in `bucket_name`,
      and false otherwise
    """

    blobs = storage_client.list_blobs(bucket_name)
    for blob in blobs:
        if blob.name == blob_name:
            return True
    return False


def create_model(storage_client, json_in, video_id):
    """ Create soft cosine similarity model

    Keywords arguments:
    storage_client -- a Storage instance
    json_in        -- json returned from the YouTube Captions API
    video_id       -- the Youtube video_id

    Returns:
    - A Soft Cosine Measure model
    - The dictionary of terms computed
    """

    video_id = video_id.lower()

    # check if bucket exists
    if blob_exists(storage_client, video_id):
        # retrieve blob from bucket
        bucket = storage_client.bucket(bucket_name)
        blob = bucket.blob(video_id)    # The blob's name is the video ID

        # download the storage pickle as a binary string
        blob_str = blob.download_as_string()
        dictionary, index = pickle.loads(blob_str)
        return dictionary, index

    # download stop_words and glove
    stop_words, glove = download_resources()

    # Create Glove similarity Index
    similarity_index = WordEmbeddingSimilarityIndex(glove)

    # parse json captions into document form
    documents = processInput(json_in)

    # create a corpus from documents
    corpus = [preprocess(document, stop_words) for document in documents]

    # create dictionary from documents
    dictionary = Dictionary(corpus)
    tfidf = TfidfModel(dictionary=dictionary)

    # create a term similarity matrix
    similarity_matrix = SparseTermSimilarityMatrix(similarity_index, dictionary, tfidf)

    # Compute Soft Cosine Measure between documents
    index = SoftCosineSimilarity(
            tfidf[[dictionary.doc2bow(document) for document in corpus]],
            similarity_matrix)

    # save index and dictionary
    storage_client = storage.Client()

    # create a binary pickle representation
    bin_tuple = pickle.dumps((dictionary, index))
    bucket = storage_client.bucket(bucket_name)
    blob = bucket.blob(video_id)

    # save to storage
    blob.upload_from_string(bin_tuple)

    if debug_messages:
        print("Binary model with name {} and dictionary uploaded.".format(video_id))

    return dictionary, index


def query_phrase(storage_client, query_string, video_id, threshold=0.2, n=8):
    """ Get the top `n` closest caption lines to a `query`

    Keywords arguments:
    storage_client -- a Storage instance
    query_string   -- an input query to search in the video
    video_id       -- the YouTube video ID
    threshold      -- the similarity threshold between a document
                      and a query
    n              -- the maximum number of elements to return

    Returns:
    An array of indices of the closest line number matches to
    the query in confidence sorted order

    ex:
    [0, 2, 5, 1, ...]
    """

    # convert video to lowercase
    video_id = video_id.lower()
    dictionary = set()

    # check if bucket exists
    if blob_exists(storage_client, video_id):
        # retrieve blob from bucket
        bucket = storage_client.bucket(bucket_name)
        blob = bucket.blob(video_id)    # The blob's name is the video ID

        # download the storage pickle as a binary string
        blob_str = blob.download_as_string()
        dictionary, index = pickle.loads(blob_str)
    else:
        raise Exception("No blob {} exists!".format(video_id))

    # Build the term dictionary, TF-idf model
    tfidf = TfidfModel(dictionary=dictionary)
    query = preprocess(query_string, set())

    query_tf = tfidf[dictionary.doc2bow(query)]

    # index the model by the query
    doc_similarity_scores = index[query_tf]

    if doc_similarity_scores.ndim <= 0:
        if debug_messages:
            print("No non-zero similarities")
        return []

    sorted_indexes = np.argsort(doc_similarity_scores)[::-1]

    # filter results by threshold
    res = sorted_indexes.tolist()
    res = [el for el in res if doc_similarity_scores[el] > threshold]

    if len(res) > n:
        return res[:n]
    else:
        return res
