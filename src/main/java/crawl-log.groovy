// crawl-log.groovy: gives the ability to search the crawl.log
// groovy script
// author: KLM - updated: May 2015 

def regex = / 404 /
def linesNb = 0 // 0 displays all
def page = 1
def reverse = true

def crawlLogFile = job.crawlController.frontier.loggerModule.crawlLogPath.file
def lines = crawlLogFile.readLines()

def count = lines.size()
def i = 0

htmlOut.println('<pre style="overflow: auto; word-wrap: normal; white-space: pre; width:900px; height:500px;">');

if(reverse == true) {
	count = count - ((page-1)*linesNb)
	if(count <= 0) {
		htmlOut.println('<p>no results</p>')
		return
	}
	crawlLogFile.withReader { reader -> 
		while (count >= 0) {
			if (lines[count-1] =~ regex) {
				htmlOut.println('<span style="font-size:small;">'+lines[count-1]+'</span>')
				i++
			}
			count--
			if(linesNb != 0 && i >= linesNb) {
				break
			}
		}
	}
}
else {
	k = 0
	k = k + ((page-1)*linesNb)
	if(k > count) {
		htmlOut.println('<p>no results</p>')
		return
	}
	crawlLogFile.withReader { reader -> 
		while (k < count) {
			if (lines[k] =~ regex) {
				htmlOut.println('<span style="font-size:small;">'+lines[k]+'</span>')
				i++
			}
			k++
			if(linesNb != 0 && i >= linesNb) {
				break
			}
		}
	}
}

htmlOut.println('</pre>');