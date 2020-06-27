# UpdatedStockMarket
StockMarket plugin for BanditMC

# Info
This was a plugin (initially private) created for the prison server BanditMC. The plugin serves as an alternative way of getting money through purchasing stocks from different companies, where periodically 'stock events' will happen, causing the price to fluctuate. Unfortunately, the server has been shut down due to lack of passion and I have been allowed to open source this project along with a few others I made for BanditMC. Currently this plugin isn't intended to just be out of the box (although it does work that way), and I won't be actively maintaining it. 

# Code Structure
The project was intially based on an older plugin named Stocks, however the more and more I used their initial foundation the more I realized that the code was poorly written and unoptimized. Other than a few small parts of the code still using that system, the entire plugin was rewritten with my ideas on what should work best. Some of these include:
- Caching information from MySQL database to avoid several statements being executed in a second
- Completely overhauled the stock properties to be more object oriented and abide by OOP 
- Completely GUI based (it was just text before)
- Stock price formula changed to be more consistent 
- Volatility effects improved
- Updated a lot of the older code 
as well as dozens of other changes. 
