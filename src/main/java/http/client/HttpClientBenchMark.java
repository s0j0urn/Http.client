package http.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.client.api.Request;

public class HttpClientBenchMark {


	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		String fileName = args[0];
		List<String> urlsList = new ArrayList<String>();
		int urlsListSize=0;

		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {

			urlsList = stream.collect(Collectors.toList());
			urlsListSize = urlsList.size();

		} catch (IOException e) {
			System.out.println("Cannot open file");
			e.printStackTrace();
		}

		String host =  urlsList.get(0).substring(7, urlsList.get(0).indexOf(':', 7));
		int port =  Integer.parseInt(urlsList.get(0).substring(urlsList.get(0).indexOf(':', 7)+1, urlsList.get(0).indexOf('/', 7)));
		//Collect failure error
		List<String> failuresList = new ArrayList<>();
		HttpClient client = new HttpClient();
		//Below settings From stackoverflow answer. Why jetty is slow ? Specially for load tests
		//client.setMaxConnectionsPerDestination(32768);
		client.setMaxConnectionsPerDestination(6);
		client.setMaxRequestsQueuedPerDestination(1024 * 1024);
		//ADD HEADERS
		String user_agent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/48.0.2564.82 Chrome/48.0.2564.82 Safari/537.36";
		client.setUserAgentField(new HttpField(HttpHeader.USER_AGENT, user_agent));
		client.start();

		//AtomicBoolean counted=new AtomicBoolean(false);
		final AtomicInteger success_status_count = new AtomicInteger(0);
		final CountDownLatch latch = new CountDownLatch(urlsListSize);
		
		//ADD HEADERS
		//String accept, accept_language, cache_control,connection,host, pragma,referrer;
		//accept = "*/*";
		//accept_encoding= "gzip, deflate, sdch";
		//accept_language= "en-GB,en-US;q=0.8,en;q=0.6";
		//cache_control = "no-cache";
		//connection = "keep-alive";
		//host="localhost:9090";
		//pragma = "no-cache";
		//referrer = "";

		long start =  System.currentTimeMillis();

		for(int i=0 ; i<urlsListSize ; i++ )
		{
			//Lets set the headers for the requests
			Request req_with_headers = client.newRequest(urlsList.get(i));
			//req_with_headers.header(HttpHeader.ACCEPT_ENCODING, "gzip").header(HttpHeader.ACCEPT_ENCODING, "deflate").header(HttpHeader.ACCEPT_ENCODING,"sdch");
			req_with_headers.header("Accept-Encoding", "gzip, deflate, sdch");
			//req_with_headers.header(HttpHeader.ACCEPT_LANGUAGE, "en-GB").header(HttpHeader.ACCEPT_LANGUAGE, "en-US;q=0.8").header(HttpHeader.ACCEPT_LANGUAGE, "en;q=0.6");
			req_with_headers.header("Accept-Language","en-US,en;q=0.8");
			req_with_headers.header(HttpHeader.CACHE_CONTROL, "no_cache");
			req_with_headers.header(HttpHeader.CONNECTION, "keep-alive");
			req_with_headers.header(HttpHeader.HOST,host+":"+String.valueOf(port));
			req_with_headers.header(HttpHeader.PRAGMA, "no-cache");
			//req_with_headers.header(HttpHeader.REFERER, referrer);
			String extension = urlsList.get(i).substring(urlsList.get(i).indexOf('.')+1);

			if(extension.equalsIgnoreCase("html"))
			{
				//accept = "Accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
				//req_with_headers.header(HttpHeader.ACCEPT, "text/html").header(HttpHeader.ACCEPT, "application/xhtml+xml").header(HttpHeader.ACCEPT, "application/xml;q=0.9").header(HttpHeader.ACCEPT,"image/webp").header(HttpHeader.ACCEPT, "*/*;q=0.8");
				req_with_headers.header("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			}
			else if(extension.equalsIgnoreCase("js"))
			{
				//accept = "*/*";
				//req_with_headers.header(HttpHeader.ACCEPT, "*/*");
				req_with_headers.header("Accept", "*/*");
				//referrer="http://localhost:9090/my-gallery/index.html";
				req_with_headers.header(HttpHeader.REFERER, "http://localhost:9090/my-gallery/index.html");
			}
			else if(extension.equalsIgnoreCase("css"))
			{
				//accept = "text/css,*/*;q=0.1";
				//req_with_headers.header(HttpHeader.ACCEPT, "text/css").header(HttpHeader.ACCEPT, "*/*;q=0.1");
				req_with_headers.header("Accept", "text/css, */*;q=0.1");
				//referrer="http://localhost:9090/my-gallery/index.html";
				req_with_headers.header(HttpHeader.REFERER, "http://localhost:9090/my-gallery/index.html");
			}
			else if(extension.equalsIgnoreCase("JSON"))
			{
				//accept = "application/json";
				req_with_headers.header(HttpHeader.ACCEPT, "application/json");
				//referrer="http://localhost:9090/my-gallery/index.html";
				req_with_headers.header(HttpHeader.REFERER, "http://localhost:9090/my-gallery/index.html");
			}
			else if(extension.equalsIgnoreCase("gif")||extension.equalsIgnoreCase("jpg")||extension.equalsIgnoreCase("png"))
			{
				//accept = "image/webp,image/*,*/*;q=0.8";
				//req_with_headers.header(HttpHeader.ACCEPT, "image/webp").header(HttpHeader.ACCEPT,"image/*").header(HttpHeader.ACCEPT, "*/*;q=0.8");
				req_with_headers.header("Accept", "image/webp,image/*,*/*;q=0.8");
				//referrer="http://localhost:9090/my-gallery/index.html"; 
				req_with_headers.header(HttpHeader.REFERER, "http://localhost:9090/my-gallery/index.html");
			} 
				/*Request req_with_headers = client.newRequest(urlsList.get(i)).header(HttpHeader.ACCEPT, accept);
				req_with_headers.header(HttpHeader.ACCEPT_ENCODING, "gzip").header(HttpHeader.ACCEPT_ENCODING, "deflate").header(HttpHeader.ACCEPT_ENCODING,"sdch");
				req_with_headers.header(HttpHeader.ACCEPT_LANGUAGE, "en-GB").header(HttpHeader.ACCEPT_LANGUAGE, "en-US;q=0.8").header(HttpHeader.ACCEPT_LANGUAGE, "en;q=0.6");
				req_with_headers.header(HttpHeader.CACHE_CONTROL, "no_cache");
				req_with_headers.header(HttpHeader.CONNECTION, "keep-alive");
				req_with_headers.header(HttpHeader.HOST,host+String.valueOf(port));
				req_with_headers.header(HttpHeader.PRAGMA, "no-cache");
				req_with_headers.header(HttpHeader.REFERER, referrer);*/
			//Lets do asynchronous requests
			AtomicBoolean counted=new AtomicBoolean(false);
			req_with_headers
			.send(new Response.CompleteListener()
			{
				@Override
				public void onComplete(Result result) {
					// TODO Auto-generated method stub
					if (result.isFailed())
					{
						result.getFailure().printStackTrace();
						failuresList.add("Result failed " + result);
					}
					else if (result.isSucceeded())
					{
						if(result.getResponse().getStatus()==200)
						{
							success_status_count.getAndIncrement();
						}
					}
					if(!counted.getAndSet(true))
					{
						latch.countDown();
						//System.out.println("countdown");
					}


				}

			});

		}
		//boolean val = latch.await(10, TimeUnit.SECONDS);

		latch.await();
		System.out.println(System.currentTimeMillis() - start + "ms" );
		System.out.println("Successful requests completed : " + success_status_count.get());
		client.stop();
	}

}
