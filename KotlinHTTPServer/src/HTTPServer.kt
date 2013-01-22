/**
 * Created with IntelliJ IDEA.
 * User: maples
 * Date: 15/01/2013
 * Time: 12:49
 */
import java.io.*
import java.net.*
import java.util.StringTokenizer
import java.util.Date
import apple.laf.JRSUIConstants.WindowTitleBarSeparator

public class HttpServer(val connect : Socket) : Runnable {

    val WEB_ROOT : File = File(".")
    val DEFAULT_FILE : String = "index.html"

    public override fun run() : Unit {
        var out = PrintWriter(connect.getOutputStream()!!)
        var input = BufferedReader(InputStreamReader(connect.getInputStream()!!))
        var dataOut = BufferedOutputStream(connect.getOutputStream()!!)

        //try
        //{

            var parse : StringTokenizer = StringTokenizer(input.readLine())
            var method : String = parse.nextToken().toUpperCase()
            var fileRequested = parse.nextToken().toLowerCase()

            if (!(method equals "GET") && !(method equals "HEAD"))
            {
                print(out = out,
                      pre = "HTTP/1.1 501 Not Implemented",
                      title = "Not Implemented",
                      body = {out.println("<H2>501 Not Implemented: " + method + " method.</H2>")})

                System.out.println("501 Not Implemented: " + method + " method.")
                return
            }
            if (fileRequested endsWith "/")
            {
                fileRequested += DEFAULT_FILE
            }

            var file : File = File(WEB_ROOT, fileRequested)
            var content : String = getContentType(fileRequested)

            if (file.exists())
            {
                if (method equals "GET")
                {
                    val fileIn : FileInputStream

                        var fileData : ByteArray = file.readBytes()

                        try {
                            fileIn = FileInputStream(file)
                            fileIn.read(fileData)
                        }
                        finally {
                            close(fileIn)
                        }

                        print(out = out,
                              pre = "HTTP/1.0 200 OK",
                              title = fileRequested,
                              contentType = content,
                              contentLength = file.length(),
                              body = {dataOut.write(fileData, 0, fileData.size); dataOut.flush();})

                        System.out.println("File " + fileRequested + " of type " + content + " returned.")
                }
            }
            else
            {
                print(out = out,
                      pre = "HTTP/1.0 404 Not Found",
                      title = "File Not Found",
                      body = {out.println("<H2>404 File Not Found: " + file.getPath() + "</H2>")})

                System.out.println("404 File Not Found: " + file)
            }
        //}
        //finally
        //{
            close(input)
            close(out)
            close(dataOut)
            close(connect)

            System.out.println("Connection closed.\n")
        //}
    }

    private fun print(out : PrintWriter, pre : String, contentType: String = "text/html", contentLength : Long = -1.toLong(), title : String, body : () -> Unit)
    {

        out.println(pre)
        out.println("Server: Java HTTP Server 1.0")
        out.println("Date: " + Date())
        out.println("Content-type: " + contentType)

        if (contentLength > -1)
        {
            out.println("Content-length: " + contentLength)
            out.println()
        }
        else
        {
            out.println()
            out.println("<HTML>")
            out.println("<HEAD><TITLE>" + title + "</TITLE></HEAD>")
            out.println("<BODY>")
        }

        out.flush()

        body()

        if (contentLength > -1)
        {
            out.println("</BODY>")
            out.println("</HTML>")
        }
        out.flush()
    }

    private fun getContentType(fileRequested : String) : String {
        when {
            fileRequested endsWith ".htm" || fileRequested endsWith ".html" -> return "text/html"
            fileRequested endsWith ".gif" -> return "image/gif"
            fileRequested endsWith ".jpg" || fileRequested endsWith ".jpeg" -> return "image/jpeg"
            fileRequested endsWith ".class" || fileRequested endsWith ".jar" -> return "applicaton/octet-stream"
            else -> return "text/plain"
        }
    }

    public fun close(stream : Any) : Unit {
        try
        {
            when (stream) {
                is Reader -> stream.close()
                is Writer -> stream.close()
                is InputStream -> stream.close()
                is OutputStream -> stream.close()
                is Socket -> stream.close()
                else -> System.err.println("Unable to close object: " + stream)
            }
        }
        catch (e : Exception)
        {
            System.out.println("Could not close stream " + stream + ". Further info: " + e.printStackTrace());
        }
    }
}

fun main(args : Array<String>)
{
val PORT : Int = 8080

    var serverConnect = ServerSocket(PORT)
    System.out.println("\nListening for connections on port " + PORT + "...\n")
    while (true)
    {
        var socket = serverConnect.accept()

        System.out.println("Connection opened. (" + Date() + ")")

        var thread = Thread(HttpServer(socket))
        thread.start()
    }
}
