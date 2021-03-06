import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.HttpClient
import org.apache.http.client.methods._
import org.apache.http.impl.client.{BasicResponseHandler, HttpClientBuilder}
import org.apache.http.message.{BasicNameValuePair, BasicHeader}

object RESTfulClient {
  val Arg_Data_Key = "-d"
  val Arg_Header_Key = "-h"

  var params: Map[String, List[String]] = _
  var url: String = _

  def handleGetRequest() {
    val query = params(Arg_Data_Key).mkString("&")
    val httpGet = new HttpGet(s"$url?$query")
    headers.foreach {
      httpGet.addHeader(_)
    }

    val responseBody = createHttpClient.execute(httpGet, new BasicResponseHandler)
    println(responseBody)
  }

  def handlePutRequest() {
    val httpPut = new HttpPut(url)
    headers.foreach {
      httpPut.addHeader(_)
    }
    httpPut.setEntity(formEntity)
    val responseBody = createHttpClient.execute(httpPut, new BasicResponseHandler)
    println(responseBody)
  }

  def handlePostRequest() {
    val httpPost = new HttpPost(url)
    headers.foreach {
      httpPost.addHeader(_)
    }
    httpPost.setEntity(formEntity)
    val responseBody = createHttpClient.execute(httpPost, new BasicResponseHandler)
    println(responseBody)
  }

  def handleDeleteRequest() {
    val httpDelete = new HttpDelete(url)
    val response = createHttpClient.execute(httpDelete)
    println(response.getStatusLine)
  }

  def handleOptionsRequest() {
    val httpOptions = new HttpOptions(url)
    headers.foreach {
      httpOptions.addHeader(_)
    }
    val response = createHttpClient.execute(httpOptions)
    println(httpOptions.getAllowedMethods(response))
  }

  private def createHttpClient: HttpClient = {
    HttpClientBuilder.create().build()
  }

  private def headers = for (nameValue <- params(Arg_Header_Key)) yield {
    def tokens = splitByEqual(nameValue)
    new BasicHeader(tokens(0), tokens(1))
  }

  private def formEntity = {
    def toJavaList(scalaList: List[BasicNameValuePair]) = {
      java.util.Arrays.asList(scalaList.toArray: _*)
    }

    def formParams = for (nameValue <- params(Arg_Data_Key)) yield {
      def tokens = splitByEqual(nameValue)
      new BasicNameValuePair(tokens(0), tokens(1))
    }

    def formEntity = new UrlEncodedFormEntity(toJavaList(formParams), "UTF-8")
    formEntity
  }

  private def splitByEqual(nameValue: String): Array[String] = nameValue.split("=")

  private def parseArgs(args: Array[String]): Map[String, List[String]] = {

    def nameValuePair(paramName: String) = {
      def values(commaSeparatedValues: String) = commaSeparatedValues.split(",").toList

      val index = args.indexWhere(_ == paramName)
      (paramName, if (index == -1) Nil else values(args(index + 1)))
    }

    Map(nameValuePair(Arg_Data_Key), nameValuePair(Arg_Header_Key))
  }


  def main(args: Array[String]) {
    require(args.size >= 2, "should at least specify an action[get,put,post,delete,options] and url")
    val command = args.head
    params = parseArgs(args)
    url = args.last

    command match {
      case "get"     => handleGetRequest()
      case "put"     => handlePutRequest()
      case "post"    => handlePostRequest()
      case "delete"  => handleDeleteRequest()
      case "options" => handleOptionsRequest()
    }
  }
}