# Weather Web Wander

### Purpose

This program automatically searches through the internet for pages related to climate change, attempting to walk through hyperlinks from page to page, returning to a DuckDuckGo search when it reaches a deadend

### Technologies used

Built in JavaFX using only standard libraries. It it super portable between Mac, Windows and Linux, and automatically generates the installer, which is one of the reasons why JavaFX is so cool.

### How it works

Climate change keywords are stored in a few different plaintext CSV files which are bundled into the package in /src/weatherwebwander/. There's currently around 160 of them with some duplicates. One of these keywords is used to performa a search. I'm using DuckDuckGo for the search because they don't track you but also because they use less guesswork in the search to try and make it personalised. In other words, it should give a fairer search. But for some reason the text is messed for the DuckDuckGo website in the JavaFX web viewer. I kind of like it actually but I'm going to see if I can fix it all the same.

Once the search page has loaded the program looks at all the links on the page and removes any that aren't proper full http/https links. It then further removes any with keywords which are on the blacklist. This list is also stored as a CSV and has terms like contact, privacy, support, etc. to stop the crawler going to a privacy policy, contact page, etc. There are some other blacklist terms too like Facebook, twitter, amazon.co, etc. and even wikipedia to stop it going to those sites and getting stuck in a loop of bullshit. Note that the blacklist is only applied to the URL, not any page content.

Once the page loads, if it's not a search page, then it checks how many climate change keywords are on the page in <p> tags. I made this choice because most main content is in these tags (I think, right?). It seems to work pretty well. I'd like to make something a bit more intelligent in the place of this but it'll do for the moment. If it stays on the page, it waits 14 seconds to load the next page.

To crawl to the next page, all the links are scraped from the page, the filters applied, and a random one picked. If there are no links after scraping and cleaning, the program goes back to do another search.

This process goes on infinitely. However there are two error catchers. One is that if the program gets 20 HTTP request errors in a row then the program quits. This usually means that the internet connection is lost. Also, if the page takes longer than 10 seconds to load, then the program goes back to do another search.