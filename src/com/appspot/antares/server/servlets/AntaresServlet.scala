package com.appspot.antares.server.servlets

import java.util.Enumeration
import javax.servlet.http.{HttpServletRequest, HttpServletResponse, HttpServlet}
import java.util.logging.Logger
import com.google.inject.{Singleton}
import java.security.MessageDigest
import java.net.{HttpURLConnection, URL}
import java.util.{List => JList}
import collection.jcl.{Buffer, MapWrapper, Map => JMap}
import java.io.{OutputStream, InputStream, InputStreamReader, OutputStreamWriter}

@Singleton
class AntaresServlet extends HttpServlet {
  
  import AntaresServlet.{headerMap, parameterMap, sha256, readAll, renderResponse}

  private val logger = Logger.getLogger("com.appspot.antares")

  override def doGet(req:HttpServletRequest, resp:HttpServletResponse) {
    val headers:Map[String, List[String]] = headerMap(req)
    val params = parameterMap(req)
    
    val strUrl = params.get("url").get
    if (strUrl == null)
      resp.sendError(500, "URL must be specified")
    
    val requestUri = req.getRequestURI
    logger.info("BaseUrl: %s; UserAgent: %s; URL: %s".format(req.getRequestURI, headers.get("User-Agent"), strUrl))

    val pageId = sha256(strUrl)
    logger.info("PageId: " + pageId)

    val url = new URL(strUrl)
    var connection = url.openConnection.asInstanceOf[HttpURLConnection]
    connection.setDoOutput(true)
    connection.setRequestMethod("GET")

    val responseCode = connection.getResponseCode
    if (responseCode == HttpURLConnection.HTTP_OK) {
      val headers = new MapWrapper[String, JList[String]]() {
        override def underlying = connection.getHeaderFields
      }
      var content = readAll(connection.getInputStream)
      onFetchSuccess(resp, headers, content)
    } else {
      onFetchError(resp, responseCode)
    }

  }

  def onFetchSuccess(resp:HttpServletResponse, headers:JMap[String, JList[String]], content:String) {
    // Forward the headers to the client
    headers.foreach(entry=>{
      val headerName:String = entry._1
      val headerValues = Buffer[String](entry._2)

      headerValues.foreach(headerValue=>{
        resp.setHeader(headerName, headerValue)
      })
    })


    renderResponse(resp, content)
  }

  def onFetchError(resp:HttpServletResponse, responseCode:Int) {
    resp.sendError(responseCode)
  }

}

object AntaresServlet {
  /**
   * Transfer a java.util.Enumeration object to Scala List
   */
  private def enumeration2List[T](enum:Enumeration[T]):List[T] = {
    def helper[T](enum:Enumeration[T], retval:List[T]):List[T] = {
      if (!enum.hasMoreElements)
        retval
      else
        helper(enum, enum.nextElement :: retval) 
    }

    helper(enum, List())
  }


  /**
   * Pack the two lists of the same dimension and construct
   * a Map out of them
   */
  private def pack[K,V](keys:List[K], values:List[V]):Map[K,V] = {
    def helper[K,V](keys:List[K], values:List[V], retval:Map[K,V]):Map[K,V] = {
      if (keys.size == 0 && values.size == 0)
        retval
      else
        helper(keys.tail, values.tail, retval + ((keys.head, values.head)))
    }

    helper(keys, values, Map.empty)
  }

  def sha256(value:String) = {
    val md = MessageDigest.getInstance("SHA-256")
    md.reset
    byteArrayToHex(md.digest(value.getBytes("UTF-8")))
  }

  /**
   * Why the heck Java library doesn't have a convenient method
   * to encode the byte array to hex?????
   * Every other standard library has this feature!
   */
  def hexEncode(in:Array[Byte]) = {
    val sb = new StringBuilder

    def helper(rest:Array[Byte], sb:StringBuilder):StringBuilder = {
      if (rest.size == 0)
        sb
      else {
        val b:Int = rest.headOption.get
        val msb = (b & 0xf0) >> 4
        val lsb = (b & 0x0f)
        sb.append((if (msb < 10) ('0' + msb).asInstanceOf[Char] else ('a' + (msb-10)).asInstanceOf[Char]))
        sb.append((if (lsb < 10) ('0' + lsb).asInstanceOf[Char] else ('a' + (lsb-10)).asInstanceOf[Char]))
        helper(rest.drop(1), sb)
      }
    }

    helper(in, sb)
    sb.toString
  }

  def headerMap(req:HttpServletRequest):Map[String, List[String]] = {
    val headerNames = enumeration2List(req.getHeaderNames.asInstanceOf[Enumeration[String]])
    val headerValues = headerNames.map(headerName=>
      enumeration2List(req.getHeaders(headerName).asInstanceOf[Enumeration[String]]))
    pack[String, List[String]](headerNames, headerValues)
  }

  def parameterMap(req:HttpServletRequest):Map[String, String] = {
    val paramNames = enumeration2List(req.getParameterNames.asInstanceOf[Enumeration[String]])
    val paramValues = paramNames.map(paramName=>req.getParameter(paramName))
    pack[String, String](paramNames, paramValues)
  }

  private def withInputStream(stream:InputStream)(op:InputStreamReader=>Unit) {
    val reader = new InputStreamReader(stream)
    try {
      op(reader)
    } catch {
      case e:Exception=>e.printStackTrace
    } finally {
      reader.close
    }
  }

  private def withOutputStream(stream:OutputStream)(op:OutputStreamWriter=>Unit) {
    val writer = new OutputStreamWriter(stream)
    try {
      op(writer)
    } catch {
      case e:Exception=>e.printStackTrace
    } finally {
      writer.close
    }
  }
  
  /**
   * Read the input stream all at once and return a string
   */
  def readAll(stream:InputStream):String = {
    var reader = new InputStreamReader(stream)
    var buffer = new Array[Char](1024)
    var retBuffer = new StringBuilder

    withInputStream(stream) {
      reader => {
        while (reader.read(buffer) != -1) {
          retBuffer.append(buffer)
        }
      }
    }

    retBuffer.toString
  }

  def renderResponse(resp:HttpServletResponse, content:String) {
    withOutputStream(resp.getOutputStream) {
      writer => {
        writer.write(content)
        writer.flush
      }
    }
  }
}