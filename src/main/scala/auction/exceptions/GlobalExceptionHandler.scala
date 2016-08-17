import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler


trait GlobalExceptionHandler {
  implicit def myExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case _: ArithmeticException =>
        extractUri { uri =>
          complete(HttpResponse(InternalServerError, entity = "Bad numbers, bad result!"))
        }
      case e: Exception =>
        extractUri { uri =>
          complete(HttpResponse(InternalServerError, entity = s"Error while processing request for resource $uri with exception: $e"))
        }
    }

}