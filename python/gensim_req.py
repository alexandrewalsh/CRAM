from re import sub
from gensim.utils import simple_preprocess
import gensim.downloader as api
from gensim.corpora import Dictionary
from gensim.models import TfidfModel
from gensim.models import WordEmbeddingSimilarityIndex
from gensim.similarities import SparseTermSimilarityMatrix
from gensim.similarities import SoftCosineSimilarity
import nltk
from nltk.corpus import stopwords
import numpy as np
import json


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
    """ Create soft cosine similarity model

    Keywords arguments:
    json_in -- json returned from the YouTube Captions API

    Returns:
    - A Soft Cosine Measure model
    - The dictionary of terms computed
    """

    # get stop words
    nltk.download('stopwords')
    stop_words = set(stopwords.words('english')) # common words to ignore
    documents = processInput(json_in)

    # create a corpus from documents
    corpus = [preprocess(document) for document in documents]

    # Load the model: this is a big file, can take a while to download and open
    glove = api.load("glove-wiki-gigaword-50")
    similarity_index = WordEmbeddingSimilarityIndex(glove)

    # Build the term dictionary, TF-idf model
    dictionary = Dictionary(corpus)
    tfidf = TfidfModel(dictionary=dictionary)

    # Create the term similarity matrix.
    similarity_matrix = SparseTermSimilarityMatrix(similarity_index, dictionary, tfidf)

    # Compute Soft Cosine Measure between documents
    index = SoftCosineSimilarity(
            tfidf[[dictionary.doc2bow(document) for document in corpus]],
            similarity_matrix)
    
    # put index and dictionary in database
    return index, dictionary


def query_phrase(query, json_in, n=3):
    """ Get the top `n` closest caption lines to a `query`

    Keywords arguments:
    query   -- an input query to search in the video
    json_in -- json returned from the YouTube Captions API
    n       -- the number of documents to return 

    Returns:
    An array of indices of the closest line number matches to
    the query in confidence sorted order

    ex:
    [(0, 0.865), (1, 0.783), (4, .424), ...]
    """

    # Get the Soft Cosime similarity model and dictionary 
    index, dictionary = createModel(json_in)

    # Build the term dictionary, TF-idf model
    tfidf = TfidfModel(dictionary=dictionary)
    query_tf = tfidf[dictionary.doc2bow(query)]

    # index the model by the query
    doc_similarity_scores = index[query_tf]
    sorted_indexes = np.argsort(doc_similarity_scores)[::-1]

    # maybe threshold
    return sorted_indexes[:n]
