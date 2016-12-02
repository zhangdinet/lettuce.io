package io.projectreactor;

import java.net.URI;

import static org.springframework.http.HttpStatus.FOUND;
import static org.springframework.web.reactive.function.RequestPredicates.GET;
import static org.springframework.web.reactive.function.RouterFunctions.route;
import static org.springframework.web.reactive.function.RouterFunctions.resources;
import static org.springframework.web.reactive.function.ServerResponse.status;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.server.HttpServer;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.reactive.function.RouterFunction;
import org.springframework.web.reactive.function.RouterFunctions;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.adapter.HttpWebHandlerAdapter;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Main Application for the Project Reactor home site.
 */
public class Application {

	public static void main(String... args) throws Exception {
		// TODO See with Arjen if we can expose a method to return a WebHandler since internally web deal with a HttpWebHandlerAdapter
		WebHandler webHandler = (HttpWebHandlerAdapter)RouterFunctions.toHttpHandler(routes());
		HttpHandler httpHandler = WebHttpHandlerBuilder.webHandler(webHandler).filters(new IndexWebFilter()).build();

		HttpServer.create("0.0.0.0")
			.newHandler(new ReactorHttpHandlerAdapter(httpHandler))
				.doOnNext(foo -> System.out.println("Server listening on " + foo.address()))
				.block()
				.onClose()
				.block();
	}

	private static RouterFunction<?> routes() {
		return route(GET("/docs/api/**"), request ->
				status(FOUND).location(URI.create(request.path().replace("/docs/", "/old/"))).build())
			.andRoute(GET("/docs/reference/**"), request ->
				status(FOUND).location(URI.create(request.path().replace("/docs/", "/old/"))).build())
			.andRoute(GET("/docs/raw/**"), request ->
				status(FOUND).location(URI.create(request.path().replace("/docs/", "/old/"))).build())
			.andRoute(GET("/docs/{dir}/api"), request ->
				status(FOUND).location(URI.create(request.path().replace("api", "release"))).build())
			.andRoute(GET("/core/docs/reference/**"), request ->
				status(FOUND).location(URI.create("https://github.com/reactor/reactor-core/blob/master/README.md")).build())
			.andRoute(GET("/core/docs/api/**"), request ->
				status(FOUND).location(URI.create(request.path().replace("/core/docs/","/docs/core/release/"))).build())
			.andRoute(GET("/netty/docs/api/**"), request ->
				status(FOUND).location(URI.create(request.path().replace("/netty/docs/","/docs/netty/release/"))).build())
			.andRoute(GET("/ipc/docs/api/**"), request ->
				status(FOUND).location(URI.create(request.path().replace("/ipc/docs/", "/docs/ipc/release/"))).build())
			.andRoute(GET("/ext/docs/api/**/test/**"), request ->
				status(FOUND).location(URI.create(request.path().replace("/ext/docs/", "/docs/test/release/"))).build())
			.andRoute(GET("/ext/docs/api/**/adapter/**"), request ->
				status(FOUND).location(URI.create(request.path().replace("/ext/docs/", "/docs/adapter/release/"))).build())
			.and(resources("/**", new ClassPathResource("static/")))
			;
	}

	private static class IndexWebFilter implements WebFilter {

		@Override
		public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
			ServerHttpRequest request = exchange.getRequest();
			return request.getURI().getPath().endsWith("/") ?
				chain.filter(exchange.mutate().setRequest(new IndexServerHttpRequestDecorator(request)).build()) :
				chain.filter(exchange);
		}

		static class IndexServerHttpRequestDecorator extends ServerHttpRequestDecorator {

			public IndexServerHttpRequestDecorator(ServerHttpRequest delegate) {
				super(delegate);
			}

			@Override
			public URI getURI() {
				URI uri = super.getURI();
				return UriComponentsBuilder.fromUri(uri).path("index.html").build().toUri();
			}
		}



	}


}
