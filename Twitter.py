import numpy as np
##import plotly
class Twitter:
	def transform_tweets_to_dataset(self,input_tweets,out_dataset):
		print '-----------------Sentiment data preprocessing--------------------'
		# plotly configuration
##		plotly.offline.init_notebook_mode()

		#load twitter data, preprocess and plot it
		import TwitterData_Initialize as TD

		data = TD.TwitterData_Initialize()
		data.initialize(input_tweets)
		#print np.shape(data.processed_data)
		print '\nText preprocessed..........'
		#print data.processed_data.head(5)

		#clean the loaded data : Remove URLs, Remove usernames (mentions), 

		#Remove tweets with Not Available text, Remove special characters, Remove numbers

		import TwitterData_Cleansing as TDC
		import TwitterCleanuper as TC
		data = TDC.TwitterData_Cleansing(data)
		data.cleanup(TC.TwitterCleanuper())
		print '\nText cleaned..........'
		#print data.processed_data.head(5)

		#Text processing: Tokenize, Transform to lowercase, Stem
		import TwitterData_TokenStem as TDTS
		data = TDTS.TwitterData_TokenStem(data)
		print '\nText tokentized, lower case..........'
		#print data.processed_data.head(5)
		data.tokenize()
		print '\nText stemmed..........'
		data.stem()
		#print data.processed_data.head(5)

		#Build word list for Bag-of-Words
		import TwitterData_Wordlist as TDWL
		data = TDWL.TwitterData_Wordlist(data)
		data.build_wordlist()
		print '\nText wordlisted..........'
		#print data.processed_data.head(5)

		#Build Bag-of-Words
		import TwitterData_BagOfWords as TDBOW
		data = TDBOW.TwitterData_BagOfWords(data)
		bow, labels = data.build_data_model()
		print 'Text BOW..........'
		#print bow
		#print labels
		x=np.array(bow)
		fnamestr="./datasets/"+out_dataset+".csv"
		np.savetxt(fnamestr, x, delimiter=',', fmt='%s')  
		print '\ndataset saved to '+fnamestr
		print 'Splitting Training and Test data 80:20'
		import pandas as pd
		x = pd.read_csv(fnamestr, header=None, sep=',')
		choices = np.in1d(x.index, np.random.choice(x.index,int(0.8*len(x)),replace=False))
		training = x[choices]
		testing = x[np.invert(choices)]
		
		df = pd.DataFrame(training) 
		#C=["id", "emotion", "text"]
		fnamestr="./datasets/"+out_dataset+"Train.csv"
		df.to_csv(fnamestr, header=None,index=False)
		print '\nTrain dataset saved to '+fnamestr
		df = pd.DataFrame(testing) 
		#C=["id", "emotion", "text"]
		fnamestr="./datasets/"+out_dataset+"Test.csv"
		df.to_csv(fnamestr, header=None ,index=False)
		print '\nTest dataset saved to '+fnamestr
		print '\n\n\n\n-------Optimization using Nature Inspired Techniques Starts--------'
