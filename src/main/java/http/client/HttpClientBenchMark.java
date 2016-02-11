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


		//Collect failure error
		List<String> failuresList = new ArrayList<>();
		HttpClient client = new HttpClient();
		//Below settings From stackoverflow anwer. Why jetty is slow ? Specially for load tests
		//client.setMaxConnectionsPerDestination(32768);
		client.setMaxConnectionsPerDestination(6);
		client.setMaxRequestsQueuedPerDestination(1024 * 1024);
		client.start();

		//AtomicBoolean counted=new AtomicBoolean(false);
		final AtomicInteger success_status_count = new AtomicInteger(0);
		final CountDownLatch latch = new CountDownLatch(urlsListSize);

		long start =  System.currentTimeMillis();

		for(int i=0 ; i<urlsListSize ; i++ )
		{
			//Lets do asynchronous requests
			AtomicBoolean counted=new AtomicBoolean(false);
			client.newRequest(urlsList.get(i))
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
