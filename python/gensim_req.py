from re import sub
from gensim.utils import simple_preprocess
import gensim.downloader as api
from gensim.models import KeyedVectors
from gensim.corpora import Dictionary
from gensim.models import TfidfModel
from gensim.models import WordEmbeddingSimilarityIndex
from gensim.similarities import SparseTermSimilarityMatrix
from gensim.similarities import SoftCosineSimilarity
import nltk
from nltk.corpus import stopwords
import numpy as np
import json
import os
from binary_ops import join_binary_files
from gensim.models import Word2Vec


def processInput(json_in):
    """ format input JSON to a document format

    Keywords arguments:
    json_in -- json returned from the YouTube Captions API

    Returns:
    An array of caption strings
    """

    documents = []
    # if isinstance(json_in, dict):
    json_processed = json_in if isinstance(json_in, dict) else json.loads(json_in)
    # print("RECIEVED FROM GENSIM: \n" + str(json_processed))

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
    return [token for token in simple_preprocess(doc, min_len=0, max_len=float("inf")) if token not in stop_words]


def download_resources():

    # get stop words
    stop_words = set()
    english_file = open('static/resources/corpora/stopwords/english', 'r')
    for line in english_file:
        stop_words.add(line.strip())
    english_file.close()
    print(stop_words)
    print("STOP WORDS processed")

    glove_fname = "static/resources/glove/glove.gz"

    if os.path.isfile(glove_fname):
        print("about to try")
        glove_vec = KeyedVectors.load(glove_fname, mmap='None')
    else: 
        print("glove model downloading...")
        dataset = api.load("text8")
        print("training Word2Vec...")
        glove_model = Word2Vec(dataset)
        glove_vec = glove_model.wv

        print(glove_vec)
        glove_vec.save(glove_fname)
    
    print("glove model finished")
    return stop_words, glove_vec


def create_model(json_in):
    """ Create soft cosine similarity model

    Keywords arguments:
    json_in -- json returned from the YouTube Captions API

    Returns:
    - A Soft Cosine Measure model
    - The dictionary of terms computed
    """

    # download stop_words and glove
    stop_words, glove = download_resources()
    print("WE DOWNLOADED THE RESOURCES HELL YEAH")
    # Create Glove similarity Index
    similarity_index = WordEmbeddingSimilarityIndex(glove)

    # parse json captions into document form
    documents = processInput(json_in)
    print("documents: " + str(documents))

    # create a corpus from documents
    corpus = [preprocess(document, stop_words) for document in documents]
    print("corpus created: " + str(corpus))

    # create dictionary from documents
    dictionary = Dictionary(corpus)
    tfidf = TfidfModel(dictionary=dictionary)
    print("dictionary created: " + str(dictionary))

    # create a term similarity matrix
    similarity_matrix = SparseTermSimilarityMatrix(similarity_index, dictionary, tfidf)
    print("similarity_matrix created")

    # Compute Soft Cosine Measure between documents
    index = SoftCosineSimilarity(
            tfidf[[dictionary.doc2bow(document) for document in corpus]],
            similarity_matrix)

    print("index created")
    return stop_words, dictionary, index, documents


def query_phrase(query_string, json_in, n=3):
    """ Get the top `n` closest caption lines to a `query`

    Keywords arguments:
    query   -- an input query to search in the video
    json_in -- json returned from the YouTube Captions API
    n       -- the number of documents to return 

    Returns:
    An array of indices of the closest line number matches to
    the query in confidence sorted order

    ex:
    [0, 2, 5, 1, ...]
    """

    # Get the Soft Cosime similarity model and dictionary 
    stop_words, dictionary, index, documents = create_model(json_in)
    print("MODEL CREATED")

    # Build the term dictionary, TF-idf model
    tfidf = TfidfModel(dictionary=dictionary)
    query = preprocess(query_string, stop_words)
    query_tf = tfidf[dictionary.doc2bow(query)]
    print("query CREATED")

    # index the model by the query
    doc_similarity_scores = index[query_tf]
    sorted_indexes = np.argsort(doc_similarity_scores)[::-1]

    print(doc_similarity_scores)
    print("FINAL CREATED: " + str(sorted_indexes.tolist()))
    print("doc0: " + str(documents[0]))

    # maybe threshold
    return sorted_indexes.tolist()
