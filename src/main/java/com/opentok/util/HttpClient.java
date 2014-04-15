package com.opentok.util;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.ning.http.client.*;
import com.ning.http.client.filter.FilterContext;
import com.ning.http.client.filter.FilterException;
import com.ning.http.client.filter.RequestFilter;

import com.opentok.api.constants.Version;

public class HttpClient extends AsyncHttpClient {
    
    private final String apiUrl;
    // NOTE: do we even need these since we now have the PartnerAuthRequestFilter?
    private final int apiKey;
    private final String apiSecret;

    private HttpClient(Builder builder) {
        super(builder.config);
        this.apiKey = builder.apiKey;
        this.apiSecret = builder.apiSecret;
        this.apiUrl = builder.apiUrl;
    }

    public String createSession(Map<String, Collection<String>> params) {
        Future request = null;
        String responseString = null;
        Response response = null;
        FluentStringsMap paramsString = new FluentStringsMap().addAll(params);

        try {
            request = this.preparePost(this.apiUrl + "/session/create")
                    .setParameters(paramsString)
                    .execute();
        } catch (IOException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        }

        try {
            response = (Response)request.get();
            // TODO: check response code
            responseString = response.getResponseBody();
        } catch (InterruptedException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        } catch (IOException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        }
        return responseString;
    }

//    protected static String makeDeleteRequest(String resource) throws OpenTokException {
//        BoundRequestBuilder get = client.prepareDelete(apiUrl + resource);
//        addCommonHeaders(get);
//
//        try {
//            Response result = get.execute().get();
//            if (result.getStatusCode() < 200 || result.getStatusCode() > 299) {
//                throw new OpenTokRequestException(result.getStatusCode(), "Error response: message: "
//                        + result.getStatusText());
//            }
//            return result.getResponseBody();
//        } catch (Exception e) {
//            throw new OpenTokRequestException(500, e.getMessage());
//        }
//    }
//
//    protected static String makeGetRequest(String resource) throws OpenTokException {
//        BoundRequestBuilder get = client.prepareGet(apiUrl + resource);
//        addCommonHeaders(get);
//
//        try {
//            Response result = get.execute().get();
//            if (result.getStatusCode() < 200 || result.getStatusCode() > 299) {
//                throw new OpenTokRequestException(result.getStatusCode(), "Error response: message: "
//                        + result.getStatusText());
//            }
//            return result.getResponseBody();
//        } catch (Exception e) {
//            throw new OpenTokRequestException(500, e.getMessage());
//        }
//    }
//
//    protected static String makePostRequest(String resource, Map<String, String> headers, Map<String, String> params,
//            String postData) throws OpenTokException {
//        BoundRequestBuilder post = client.preparePost(apiUrl + resource);
//        if (params != null) {
//            for (Entry<String, String> pair : params.entrySet()) {
//                post.addParameter(pair.getKey(), pair.getValue());
//            }
//        }
//
//        if (headers != null) {
//            for (Entry<String, String> pair : headers.entrySet()) {
//                post.addHeader(pair.getKey(), pair.getValue());
//            }
//        }
//
//        addCommonHeaders(post);
//
//        if (postData != null) {
//            post.setBody(postData);
//        }
//
//        try {
//            Response result = post.execute().get();
//
//            if (result.getStatusCode() < 200 || result.getStatusCode() > 299) {
//                throw new OpenTokRequestException(result.getStatusCode(), "Error response: message: " + result.getStatusText());
//            }
//
//            return result.getResponseBody();
//        } catch (Exception e) {
//            throw new OpenTokRequestException(500, e.getMessage());
//        }
//    }
//
//    private static void addCommonHeaders(BoundRequestBuilder get) {
//        get.addHeader("X-TB-PARTNER-AUTH", String.format("%s:%s", apiKey, apiSecret));
//    }

    public static class Builder {
        private final int apiKey;
        private final String apiSecret;
        private String apiUrl;
        private AsyncHttpClientConfig config;

        public Builder(int apiKey, String apiSecret) {
            this.apiKey = apiKey;
            this.apiSecret = apiSecret;
        }

        public Builder apiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
            return this;
        }

        public HttpClient build() {
            this.config = new AsyncHttpClientConfig.Builder()
                    .setUserAgent("Opentok-Java-SDK/"+Version.VERSION)
                    .addRequestFilter(new PartnerAuthRequestFilter(this.apiKey, this.apiSecret))
                    .build();
            // NOTE: not thread-safe, config could be modified by another thread here?
            HttpClient client = new HttpClient(this);
            return client;
        }
    }

    static class PartnerAuthRequestFilter implements RequestFilter {

        private int apiKey;
        private String apiSecret;

        public PartnerAuthRequestFilter(int apiKey, String apiSecret) {
            this.apiKey = apiKey;
            this.apiSecret = apiSecret;
        }

        public FilterContext filter(FilterContext ctx) throws FilterException {
            return new FilterContext.FilterContextBuilder(ctx)
                    .request(new RequestBuilder(ctx.getRequest())
                            .addHeader("X-TB-PARTNER-AUTH", this.apiKey+":"+this.apiSecret)
                            .build())
                    .build();
        }
    }
}
