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

from nltk.corpus import wordnet as wn
from nltk.stem import PorterStemmer, WordNetLemmatizer
from nltk.tag import PerceptronTagger
import nltk

nltk.data.path.append('static/resources/')

porter = PorterStemmer()
wnl = WordNetLemmatizer()

tagger = PerceptronTagger()
pos_tag = tagger.tag

'''
Stemming and Lemmatization both generate the root form of the inflected words.
The difference is that stem might not be an actual word whereas, lemma is an actual language word.
'''


def lemmatize(ambiguous_word, pos=None, neverstem=True, 
              lemmatizer=wnl, stemmer=porter):
    """
    Tries to convert a surface word into lemma, and if lemmatize word is not in
    wordnet then try and convert surface word into its stem.
    This is to handle the case where users input a surface word as an ambiguous 
    word and the surface word is a not a lemma.

    Keyword arguments:
    ambiguous_word -- a word to lemmatize
    pos            -- a part of speech tag 
    neverstem      -- a flag indicating whether to never stem
    lemmatizer     -- a model to lemmatize words
    stemmer        -- a model to stem words

    Returns:
    - The lemma for a word, or stem if lemma doesn't exist
    """

    if pos:
        lemma = lemmatizer.lemmatize(ambiguous_word, pos=pos)
    else:
        lemma = lemmatizer.lemmatize(ambiguous_word)
    stem = stemmer.stem(ambiguous_word)

    # Ensure that ambiguous word is a lemma.
    if not wn.synsets(lemma):
        if neverstem:
            return ambiguous_word
        if not wn.synsets(stem):
            return ambiguous_word
        else:
            return stem
    else:
        return lemma


def penn2morphy(penntag, returnNone=False):
    """
    Get the POS (Part of Speech) tag for a word

    Keyword arguments:
    penntag    -- a Penn Treebank POS code
    returnNone -- whether to return no tag

    Returns:
    - a Morphy POS tag
    """

    morphy_tag = {'NN': wn.NOUN, 'JJ': wn.ADJ,
                  'VB': wn.VERB, 'RB': wn.ADV}
    try:
        return morphy_tag[penntag[:2]]
    except:
        return None if returnNone else ''


def word_tokenize(text):
    """
    Get a tokenized list of a sentence

    Keyword arguments:
    text -- a string to tokenize

    Returns:
    - a list of tokenized word
    """

    return text.split()


def lemmatize_sentence(sentence, neverstem=False, keepWordPOS=False, 
                       tokenizer=word_tokenize, postagger=pos_tag, 
                       lemmatizer=wnl, stemmer=porter):
    """
    Get a list of tokenized, lemmitized words. Entry point for
    this file.

    Keywords arguments:
    sentence       --  a sentence to lemmatize
    neverstem      -- a boolean indicating whether to never
                      generate word stems
    keepWordPOS    -- boolean indicating whether to keep word
                      part of speech elements
    tokenizer      -- a function for tokenizing words
    postagger      -- a POS (Part of Speech) categorizer
    lemmatizer     -- a model to lemmatize words
    stemmer        -- a model to get word stems

    Returns:
    - a list of lemmatized words
    """

    words, lemmas, poss = [], [], []
    for word, pos in postagger(tokenizer(sentence)):
        pos = penn2morphy(pos)
        lemmas.append(lemmatize(word.lower(), pos, neverstem,
                                lemmatizer, stemmer))
        poss.append(pos)
        words.append(word)
    if keepWordPOS:
        return words, lemmas, [None if i == '' else i for i in poss]
    return lemmas


if __name__ == '__main__':
    pass
