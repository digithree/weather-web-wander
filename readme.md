# Weather Web Wander

### Purpose

This program automatically searches through the internet for pages related to climate change, attempting to walk through hyperlinks from page to page, returning to a Google search when it reaches a deadend.

### Technologies used

Built in JavaFX. Libraries and their dependecies:
* [Boilerpipe](http://nekohtml.sourceforge.net) - removal of boilerplate from webpage
  * [Xerces](http://xerces.apache.org) - XML parsing
  * [NekoHTML](http://nekohtml.sourceforge.net) - HTML scanner and tag balancer

## Methodology

### Web crawl

Climate change search strings are stored in plaintext CSV files which are bundled into the package in /src/weatherwebwander/. A term is picked at random and a search performed. Once the search page has loaded the program looks at all the links on the page and removes any that aren't full http/https links. It then further removes any links with keywords which are on the blacklist. This list is also stored as a CSV and has terms like contact, privacy, support, etc. to stop the crawler going to a privacy policy, contact page, etc. There are some other blacklist terms too like Facebook, twitter, amazon.co, etc. and even wikipedia. Note that the blacklist is only applied to the URL, not any page content.

The web page is processed and a relevancy score calculated (see below). If the page is relevant enough (currently if there are _any_ keyword matches) then the program waits before moving on. If it fails this test a new search is performed immediately.

To crawl to the next page, all the links are scraped from the page, the filters applied, and a random one picked. If there are no links after scraping and cleaning, the program goes back to do another search.

The process continues infinitely.

### Web page processing

When the crawler reaches a non search page it is parsed by Boilerpipe and a single plain text string generated using the ArticleExtractor as we are looking for webpage in article format (headings, paragraphs of text). This is to facilitate two tests which generate a relevancy and emotional score.

#### Relevancy

The BoilerPipe text is cleaned by removing matches using a regex, viewable on [regexr.com](http://regexr.com/3b9o2). The number of keyword matches are counted and the resulting sum is the relevancy score. 

#### Emotion

Again, the BoilerPaper text is cleaned, view the regex [here](http://regexr.com/3b9nv). A dictionary of words paired with scores is used, based on a dictionary created by [SentiStrength](http://sentistrength.wlv.ac.uk). Each emotionally significant word has a positive or negative integer score attached. However the match process does not use any semantic context so a word such as "like" has a positive scoring even though it may be used for example "moving like a car". This will be looked at in the future but for the moment it still provides a rough metric.

### Display

The screen is divided into two columns. On the left is a node tree graph, on the right is the webpage display.

#### Node tree graph

A crawl topology graph is created in the left panel. Each time a new page is reached, a node is added. The draw size of the node is determined by the relevancy score of the page; the more relevant, the bigger it is. The colour of the node is determined by the emotional score. The nodes are laid out using custom force directed animation in real time on a subclassed Canvas.

#### Webpage display

A WebView JavaFX component is used to render the current webpage. If the page is accepted as relevant, a JavaScript function is run on the webpage to automatically scroll the page down slowly for forteen seconds.

Note that there does seem to be an issue with resource management in the WebView / WebEngine nodes. Webpage history resources are retained indescriminantly and so after visiting a significant number of pages the program will get a error as it runs out of memory. The issue is discussed [here](http://stackoverflow.com/questions/23668910/how-to-clear-javafx-webview-memory-usage-after-closing-stage) on Stack Overflow.

### Error catching

If the program gets twenty HTTP request errors in a row then the program quits. This usually means that the internet connection is lost. Secondly, if the page takes longer than 10 seconds to load, then the program goes back to do another search.