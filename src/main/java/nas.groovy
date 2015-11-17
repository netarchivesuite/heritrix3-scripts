
logfilePrefix = "scripting_events"   // A logfile will be created with this prefix + ".log"
initials = "ABC"   // Curator initials to be added to log-messages

// To use, just remove the initial "//" from any one of these lines.
//
//killToeThread(1)       //Kill a toe thread by number
//listFrontier('.*')     //List uris in the frontier matching a given regexp
//deleteFromFrontier('.*')    //Remove uris matching a given regexp from the frontier
//viewCrawlLog('.*')          //View already crawled lines uris matching a given regexp

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.OperationStatus;

void killToeThread(int thread) {
    job.crawlController.requestCrawlPause();
    job.crawlController.killThread(thread, true);
    logEvent("Killed Toe Thread number " + thread + ".")
    rawOut.println "WARNING: This job and heritrix may now need to be manually terminated when it is finished harvesting."
    rawOut.println "REMINDER: This job is now in a Paused state."
}


void logEvent(String e) {
    logger = job.crawlController.loggerModule.setupSimpleLog(logfilePrefix);
    logger.info("Action from user " + initials + ": " +e)
}

void deleteFromFrontier(String regex) {
    job.crawlController.requestCrawlPause()
    count = job.crawlController.frontier.deleteURIs(".*", regex)
    rawOut.println "REMINDER: This job is now in a Paused state."
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
