import pandas as pd
#from emoticons import EmoticonDetector
class TwitterData_Initialize():
    data = []
    processed_data = []
    wordlist = []

    data_model = None
    data_labels = None
    is_testing = False
    
    def initialize(self, csv_file, is_testing_set=False, from_cached=None):
        if from_cached is not None:
            self.data_model = pd.read_csv(from_cached)
            return

        self.is_testing = is_testing_set

        if not is_testing_set:
        	#import numpy as np
        	#self.data = pd.read_csv(csv_file, header=1, names=["id", "emotion", "text"], encoding='utf-8')
        	self.data = pd.read_csv(csv_file, header=1, names=["id", "emotion", "text"], encoding='latin1')
        	#x=self.data
        	#print np.shape(x)
        	self.data = self.data[self.data["emotion"].isin(["positive", "negative", "neutral"])]
        	#x=self.data
        	#print np.shape(x)
        else:
            self.data = pd.read_csv(csv_file, header=0, names=["id", "text"],dtype={"id":"int64","text":"str"},nrows=4000)
            not_null_text = 1 ^ pd.isnull(self.data["text"])
            not_null_id = 1 ^ pd.isnull(self.data["id"])
            self.data = self.data.loc[not_null_id & not_null_text, :]

        self.processed_data = self.data
        self.wordlist = []
        self.data_model = None
        self.data_labels = None