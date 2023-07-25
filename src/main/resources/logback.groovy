
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import com.softwood.util.loggingConfiguration.HighlightingCompositeConverter
import org.fusesource.jansi.internal.Kernel32

import static ch.qos.logback.classic.Level.*
import static org.fusesource.jansi.internal.Kernel32.GetStdHandle
import static org.fusesource.jansi.internal.Kernel32.STD_OUTPUT_HANDLE


/**
 * config appears to be ignored in intellij when running a script
 *
 * now requires a 3rd party to process as logback have dropped official supprt for groovy config
 * https://virtualdogbert.github.io/logback-groovy-config/#getting-started
 *
 */

def appenderList = ["PlainConsole"]

conversionRule("highlight", HighlightingCompositeConverter)

final int VIRTUAL_TERMINAL_PROCESSING = 0x0004
long console = GetStdHandle(STD_OUTPUT_HANDLE)
int[] mode = new int[1]
def ansiEnabled = Kernel32.GetConsoleMode(console, mode)
//println "appended is ansi enabled $ansiEnabled"



//colour coded for display
appender("AnsiConsole", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        //logger{5} will shorten just to class name to x.y.z.ClassName form
        pattern = "[%d{HH:mm:ss.SSS}] %cyan([%thread]) %highlight([%level]) %magenta(%logger{5}) : %msg%n"
    }
}

appender("PlainConsole", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        //logger{5} will shorten just to class name to x.y.z.ClassName form
        pattern = "[%d{HH:mm:ss.SSS}] [%thread] [%level] %logger{0} : %msg%n"
    }
}

logger ("io.vertx.core", WARN)
logger ("io.netty", WARN)
logger ("io.netty.channel", WARN)

logger ("ch.qos.logback.classic", WARN)
logger ("com.hazelcast", WARN)

logger "datastore", DEBUG
//logger "datastore", Level.DEBUG, ["Console"]  //causes it print it twice!



root(DEBUG, ["PlainConsole"])
