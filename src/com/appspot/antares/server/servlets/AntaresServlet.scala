package com.appspot.antares.server.servlets

import java.util.Enumeration
import javax.servlet.http.{HttpServletRequest, HttpServletResponse, HttpServlet}
import java.util.logging.Logger
import com.google.inject.{Inject, Singleton}

@Singleton
class AntaresServlet @Inject()(private val logger:Logger) extends HttpServlet {

  import AntaresServlet.{headerMap, parameterMap}

  override def doGet(req:HttpServletRequest, resp:HttpServletResponse) {
    val headers:Map[String, List[String]] = headerMap(req)
    val params = parameterMap(req)

    var output = new StringBuilder()

    headers.foreach(entry=>output.append(entry._1 + ":" + entry._2 + "\n"))
 
//    params.foreach((param, value) => output.append(param + ":" + value))

    
    resp.getOutputStream.println(output.toString)
  }

}

object AntaresServlet {

  private def enumeration2List[T](enum:Enumeration[T]):List[T] = {
    def helper[T](enum:Enumeration[T], retval:List[T]):List[T] = {
      if (!enum.hasMoreElements)
        retval
      else
        helper(enum, enum.nextElement :: retval) 
    }

    helper(enum, List())
  }

  def pack[K,V](keys:List[K], values:List[V]):Map[K,V] = {
    def helper[K,V](keys:List[K], values:List[V], retval:Map[K,V]):Map[K,V] = {
      if (keys.size == 0 && values.size == 0)
        retval
      else
        helper(keys.tail, values.tail, retval + ((keys.head, values.head)))
    }

    helper(keys, values, Map.empty)
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
}