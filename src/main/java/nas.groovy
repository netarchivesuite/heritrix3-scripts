
logfilePrefix = "scripting_events"   // A logfile will be created with this prefix + ".log"
initials = "ABC"   // Curator initials to be added to log-messages

// To use, just remove the initial "//" from any one of these lines.
//
//killToeThread(1)       //Kill a toe thread by number
//listFrontier('.*')     //List uris in the frontier matching a given regexp
//deleteFromFrontier('.*')    //Remove uris matching a given regexp from the frontier
//viewCrawlLog('.*')          //View already crawled lines uris matching a given regexp

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.OperationStatus

import java.util.logging.FileHandler
import java.util.logging.Logger;

void killToeThread(int thread) {
    job.crawlController.requestCrawlPause();
    //TODO actually this should be "true" to get a new thread, but "false" makes it easier to test
    job.crawlController.killThread(thread, false);
    logEvent("Killed Toe Thread number " + thread + ".")
    job.crawlController.requestCrawlResume();
}

/**
 * Utility method to find and return the logger for a given log-prefix, or initialise it
 * if it doesn't already exist.
 * @return
 */
Logger getLogger() {
    for (Map.Entry<Logger,FileHandler> entry: job.crawlController.loggerModule.fileHandlers ) {
        if (entry.key.name.contains(logfilePrefix)) {
            return entry.key
        }
    }
    return job.crawlController.loggerModule.setupSimpleLog(logfilePrefix);
}

void logEvent(String e) {
    getLogger().info("Action from user " + initials + ": " +e)
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
