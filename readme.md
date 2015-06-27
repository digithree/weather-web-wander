# Weather Web Wander

### Purpose

This program automatically searches through the internet for pages related to climate change, attempting to walk through hyperlinks from page to page, returning to a Google search when it reaches a deadend.

### Technologies used

Built in JavaFX using only standard libraries. It it super portable between Mac, Windows and Linux, and automatically generates the installer.

### Display

A crawl topology graph is created in the left panel. Each time a new page is reached, a node is added. The draw size of the node is determined by the relevancy score of the page; the more relevant, the bigger it is. The nodes are laid out using custom force directed animation in real time on a subclassed Canvas.

### How it works

Climate change keywords are stored in a few different plaintext CSV files which are bundled into the package in /src/weatherwebwander/. There's currently around 160 of them with some duplicates. One of these keywords is used to perform a search.

Once the search page has loaded the program looks at all the links on the page and removes any that aren't proper full http/https links. It then further removes any with keywords which are on the blacklist. This list is also stored as a CSV and has terms like contact, privacy, support, etc. to stop the crawler going to a privacy policy, contact page, etc. There are some other blacklist terms too like Facebook, twitter, amazon.co, etc. and even wikipedia to stop it going to those sites and getting stuck in a loop. Note that the blacklist is only applied to the URL, not any page content.

Once the page loads, if it's not a search page, then it checks how many climate change keywords are on the page in HTML paragraph tags. I made this choice because most main content is in these tags. It seems to work pretty well. I'd like to make something a bit more intelligent in the place of this but it'll do for the moment. If it stays on the page, it waits 14 seconds to load the next page.

To crawl to the next page, all the links are scraped from the page, the filters applied, and a random one picked. If there are no links after scraping and cleaning, the program goes back to do another search.

This process goes on infinitely. However there are two error catchers. One is that if the program gets 20 HTTP request errors in a row then the program quits. This usually means that the internet connection is lost. Also, if the page takes longer than 10 seconds to load, then the program goes back to do another search.