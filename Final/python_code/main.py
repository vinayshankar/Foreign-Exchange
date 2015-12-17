import logging
#logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)

# the setup function that reads the data and creates the dictionary and tfidf files
def setup():
    documents = []
    import glob
    import os
    directoryNames = list(set(glob.glob(os.path.join("Data", "*"))).difference(set(glob.glob(os.path.join("Data","*.*")))))
    numberOfDocuments = 0

    for folder in directoryNames:
        for fileNameDir in os.walk(folder):
            for fileName in fileNameDir[2]:
                if fileName[-4:] != ".txt":
                    continue
                nameFileDocument = "{0}{1}{2}".format(fileNameDir[0], os.sep, fileName)
                with open(nameFileDocument, 'r') as doc:
                    doc_text = doc.read().replace('\n', '')
                import re
                processed_doc_text = re.sub('[^a-zA-Z0-9\n]', ' ', doc_text)
                documents.append(processed_doc_text)
                numberOfDocuments += 1
                break

    print(numberOfDocuments)

    # remove common words and tokenize

    #from gensim.utils import lemmatize
    #lemmatized_docs = [lemmatize(document) for document in documents]

    from stop_words import get_stop_words
    stop_words = get_stop_words('english')
    texts = [[word for word in document if word not in stop_words]
             for document in documents]

    # remove words that appear only once
    from collections import defaultdict
    frequency = defaultdict(int)
    for text in texts:
        for token in text:
            frequency[token] += 1

    texts = [[token for token in text if frequency[token] > 1]
             for text in texts]

    from gensim import corpora
    dictionary = corpora.Dictionary(texts)
    dictionary.filter_extremes(no_below=20, no_above=0.1, keep_n=1000000)
    dictionary.save('files/pmc-data.dict') # store the dictionary, for future reference

    corpus = [dictionary.doc2bow(text) for text in texts]
    corpora.MmCorpus.serialize('files/pmc-data.mm', corpus) # store to disk, for later use


    from gensim.corpora import MmCorpus

    mm = MmCorpus('files/pmc-data.mm')

    from gensim.models import TfidfModel

    tfidf = TfidfModel(mm, id2word=dictionary, normalize=True)

    MmCorpus.serialize('files/pmc-data-tfidf.mm', tfidf[mm], progress_cnt=10000)

# Retrives all the doc IDs in the TFIDF that contain the word
def getDocumentsWithWord(term):
    docs = []
    id = id2word[term]
    i = 0
    for col in file.col:
        if col == id:
            docs.append(file.row[i])
        i += 1
    return docs

# Retrieves all the words along with their TFIDF values in the document with id docId
def getWordsAndTFIDF(docId):
    data = []
    i = 0
    for row in file.row:
        if docId == row:
            data.append([file.col[i], file.data[i]])
        i+=1
    return data


def generateJSON(inputWord, level, parents):
    docs = getDocumentsWithWord(inputWord)
    parents.append(inputWord)

    relevant_words = {}

    for doc in docs:
        data = getWordsAndTFIDF(doc)
        for d in data:
            if word2id[d[0]] in parents:
                continue
            if not d[0] in relevant_words:
                relevant_words[d[0]] = 0.0
            relevant_words[d[0]] += d[1]

    from operator import itemgetter
    sorted_relevant_words = sorted(relevant_words.items(), key=itemgetter(1), reverse=True)

    topic = {}
    topic['name'] = inputWord
    topic['words'] = []
    topic['children'] = []

    for w in sorted_relevant_words[:max_words]:
        topic['words'].append(word2id[w[0]])
        parents.append(word2id[w[0]])

    for w in sorted_relevant_words[:max_words]:
        if level == max_level:
            continue
        else:
            topic['children'].append(generateJSON(word2id[w[0]], level=level+1, parents=parents))
    return topic

# The max number of levels to find relevant words
max_level = 4
# Max number of relevant words at each level
max_words = 10

# check if the tfidf matrix file already exists. If not call setup
import os
if not os.path.exists('files/pmc-data-tfidf.mm'):
    setup()

# Load the dictionary from the file. Create id2Word and word2Id variables
from gensim import corpora
dictionary = corpora.Dictionary.load('files/pmc-data.dict')
id2word = dictionary.token2id
word2id = {v: k for k, v in id2word.items()}

# Load the tfidf data
from scipy.io import mmread
file = mmread('files/pmc-data-tfidf.mm')

# Ask user to input the term
term = input("Please input the term you want to visualize: ")
if term != "":
    # generate the json tree of relevant words for the input term
    response = generateJSON(term, 1, [])
    import json
    with open('/var/www/Hierarchie/app/data/pmc-data.json', 'w') as outfile:
        json.dump(response, outfile)