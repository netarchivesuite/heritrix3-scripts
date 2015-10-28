// view-frontier-url: gives the ability to search the frontier by regex, view the URLs, delete them
// groovy script
// see org.archive.crawler.frontier.BdbMultipleWorkQueues.forAllPendingDo()
// author: KLM - updated: May 2015 
 
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.OperationStatus;
 
def nbUrlToList = 1000
def regex = /.*dns.*/ 
def regexStr = '.*dns.*'
def delete = false

if(delete == true) {
	//Note that the first parameter to this call, "queueRegex", is undocumented in the heritrix javadoc
	//The second parameter is just a regex of urls to be deleted.
	count = job.crawlController.frontier.deleteURIs(".*", regexStr)
	job.crawlController.loggerModule.setupSimpleLog("frontierlog").info("Deleted " + count + " uris matching regex '" + regexStr + "'")
	rawOut.println count + " uris deleted from frontier"
	return
}

pendingUris = job.crawlController.frontier.pendingUris

htmlOut.println('<p>pendings URLs : '+pendingUris.pendingUrisDB.count()+'</p>')
htmlOut.println('<pre style="overflow: auto; word-wrap: normal; white-space: pre; width:900px; height:500px;">');
 
cursor = pendingUris.pendingUrisDB.openCursor(null, null);
key = new DatabaseEntry();
value = new DatabaseEntry();
count = 0;
 
while (cursor.getNext(key, value, null) == OperationStatus.SUCCESS && count < nbUrlToList) {
    if (value.getData().length == 0) {
        continue;
    }
    curi = pendingUris.crawlUriBinding.entryToObject(value);
	if (curi =~ regex) {
		htmlOut.println curi
		count++
	}
    
}
cursor.close();
 
htmlOut.println('</pre>');
htmlOut.println '<p>' + count + " pending urls listed</p>"