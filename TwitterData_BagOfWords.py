import TwitterData_Wordlist as TDWL
import pandas as pd
class TwitterData_BagOfWords(TDWL.TwitterData_Wordlist):
    def __init__(self, previous):
        self.processed_data = previous.processed_data
        self.wordlist = previous.wordlist
    
    def build_data_model(self):
        label_column = []
        if not self.is_testing:
            label_column = ["label"]

        columns = list(
            map(lambda w: w + "_bow",self.wordlist)) + label_column 
        labels = []
        rows = []
        for idx in self.processed_data.index:
            current_row = []

            # add bag-of-words
            tokens = set(self.processed_data.loc[idx, "text"])
            for _, word in enumerate(self.wordlist):
            	current_row.append(1 if word in tokens else 0)

            if not self.is_testing:
                # add label as last column in dataset
                
                #0-positive, 1-negative, 2-neutral
                
                current_label = self.processed_data.loc[idx, "emotion"]
                if current_label=='positive':
                	code_label=0
                	#code_label='postive'
                elif current_label=='negative':
                	code_label=1
                	#code_label='negative'
                else:
                	code_label=2
                	#code_label='neutral'
                
                labels.append(code_label)
                current_row.append(code_label)


            rows.append(current_row)

        self.data_model = pd.DataFrame(rows, columns=columns)
        self.data_labels = pd.Series(labels)
        return  self.data_model, self.data_labels