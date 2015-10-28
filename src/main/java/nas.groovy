logfilePrefix = "scripting_events"
initials = "ABC"

//killToeThread(1)
//listFrontier('.*')
//deleteFromFrontier('.*')
//viewCrawlLog('.*')

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.OperationStatus;

void killToeThread(int thread) {
    job.crawlController.requestCrawlPause();
    //TODO actually this should be "true" to get a new thread, but "false" makes it easier to test
    job.crawlController.killThread(thread, false);
    logEvent("Killed Toe Thread number " + thread + ".")
    job.crawlController.requestCrawlResume();
}


void logEvent(String e) {
    logger = job.crawlController.loggerModule.setupSimpleLog(logfilePrefix);
    logger.info("Action from user " + initials + ": " +e)
}

void deleteFromFrontier(String regex) {
    job.crawlController.requestCrawlPause()
    count = job.crawlController.frontier.deleteURIs(".*", regex)
    job.crawlController.requestCrawlResume();
    logEvent("Deleted " + count + " uris matching regex '" + regex + "'")
    rawOut.println count + " uris deleted from frontier."
    rawOut.println("This action has been logged in " + logfilePrefix + ".log")
}

void listFrontier(String regex) {
    style = 'overflow: auto; word-wrap: normal; white-space: pre; width:1200px; height:500px'
    htmlOut.println '<pre style="' + style +'">'

    pendingUris = job.crawlController.frontier.pendingUris
    cursor = pendingUris.pendingUrisDB.openCursor(null, null);
    key = new DatabaseEntry();
    value = new DatabaseEntry();

    while (cursor.getNext(key, value, null) == OperationStatus.SUCCESS) {
        if (value.getData().length == 0) {
            continue;
        }
        curi = pendingUris.crawlUriBinding.entryToObject(value);
        if (curi =~ regex) {
            htmlOut.println '<span style="font-size:small;">'+curi+'</span>'
        }
    }
    cursor.close();
    htmlOut.println '</pre>'
}

void viewCrawlLog(String regex) {
    style = 'overflow: auto; word-wrap: normal; white-space: pre; width:1200px; height:500px'
    htmlOut.println '<pre style="' + style +'">'

    crawlLogFile = job.crawlController.frontier.loggerModule.crawlLogPath.file
    lines = crawlLogFile.readLines()
    count = lines.size()
    k = 0
    crawlLogFile.withReader { reader ->
        while (k < count) {
            if (lines[k] =~ regex) {
                htmlOut.println '<span style="font-size:small;">'+lines[k]+'</span>'
                //rawOut.println lines[k]
            }
            k++
        }
        htmlOut.println '</pre>'
    }
    htmlOut.println '<p>'+ k + " matching urls found </p>"
}
